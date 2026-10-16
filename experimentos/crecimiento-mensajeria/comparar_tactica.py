"""Captura contexto y agrega la comparación antes/después de la táctica S11."""

from __future__ import annotations

import argparse
import datetime as dt
import json
import pathlib
import statistics
import sys
from typing import Any


ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "experiments" / "postgresql"))

from run_experiment import energy_context, machine_context  # noqa: E402


RUNS = 4
LOAD_LEVEL_VUS = 200
ITERATIONS_PER_VU = 4
EXPECTED_REQUESTS = LOAD_LEVEL_VUS * ITERATIONS_PER_VU
EXPECTED_BASELINE_REVISION = "73895be48e03a35731c53d77a45e471808313168"


def write_json(path: pathlib.Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def read_json(path: pathlib.Path) -> Any:
    return json.loads(path.read_text(encoding="utf-8-sig"))


def capture_context(
    output: pathlib.Path,
    git_revision: str,
    branch: str,
    docker_version: str,
    compose_version: str,
    postgresql_version: str,
) -> None:
    energy_before = energy_context()
    if energy_before["condition"] == "unknown":
        raise RuntimeError("No se pudo registrar la condición de energía")
    write_json(
        output / "contexto.json",
        {
            "schema_version": 1,
            "captured_at_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
            "git_revision": git_revision,
            "branch": branch,
            "working_tree_clean_before_measurement": True,
            "machine": machine_context(),
            "energy_before": energy_before,
            "software_versions": {
                "docker": docker_version,
                "docker_compose": compose_version,
                "postgresql": postgresql_version,
                "k6_image": "grafana/k6:0.54.0",
            },
            "comparison": {
                "baseline_result": "resultados/20261016T224331Z/resultado.json",
                "baseline_git_revision": EXPECTED_BASELINE_REVISION,
                "load_level_vus": LOAD_LEVEL_VUS,
                "iterations_per_vu": ITERATIONS_PER_VU,
                "requests_per_run": EXPECTED_REQUESTS,
                "runs": RUNS,
                "discarded_run": 1,
            },
            "tactic": {
                "name": "local_latest_message_page_cache",
                "enabled": True,
                "ttl_ms": 1000,
                "maximum_entries": 1000,
                "cached_offset": 0,
            },
        },
    )


def all_statuses_are_200(run: dict[str, Any]) -> bool:
    statuses = run["response_status_counts"]
    non_200 = sum(float(value) for key, value in statuses.items() if str(key) != "200")
    return float(statuses.get("200", 0)) == EXPECTED_REQUESTS and non_200 == 0


def percent_change(after: float, before: float) -> float:
    return round(((after - before) / before) * 100, 3)


def aggregate(output: pathlib.Path, baseline_path: pathlib.Path) -> None:
    context = read_json(output / "contexto.json")
    baseline = read_json(baseline_path)
    seed = read_json(output / "verificacion-semilla.json")
    cache_before = read_json(output / "cache-stats-before.json")
    cache_after = read_json(output / "cache-stats-after.json")

    if baseline["measured_git_revision"] != EXPECTED_BASELINE_REVISION:
        raise ValueError("El resultado base no corresponde al commit prerregistrado")
    if not seed.get("matches_preregistered_seed"):
        raise ValueError("La semilla no coincide con el prerregistro")
    if not cache_before.get("enabled") or cache_before.get("hits") != 0:
        raise ValueError("La caché no quedó habilitada y reiniciada antes de medir")

    baseline_level = next(
        level for level in baseline["level_summaries"]
        if level["load_level_vus"] == LOAD_LEVEL_VUS
    )
    runs: list[dict[str, Any]] = []
    for run_number in range(1, RUNS + 1):
        path = output / f"run-vu{LOAD_LEVEL_VUS}-{run_number}.json"
        run = read_json(path)
        if run["run"] != run_number or run["load_level_vus"] != LOAD_LEVEL_VUS:
            raise ValueError(f"Identidad inesperada en {path}")
        if run["expected_requests"] != EXPECTED_REQUESTS or run["requests"] != EXPECTED_REQUESTS:
            raise ValueError(f"Cantidad de solicitudes inválida en {path}")
        runs.append(run)

    valid_runs = runs[1:]
    after_p95 = round(
        statistics.median(float(run["latency_ms"]["p95"]) for run in valid_runs), 3
    )
    after_throughput = round(
        statistics.median(float(run["throughput_rps"]) for run in valid_runs), 3
    )
    before_p95 = float(baseline_level["p95_median_ms"])
    before_throughput = float(baseline_level["throughput_median_rps"])

    correctness = all(
        bool(run["integrity_valid"])
        and float(run["failed_request_rate"]) == 0
        and float(run["valid_page_rate"]) == 1
        and all_statuses_are_200(run)
        for run in valid_runs
    )
    hits_observed = int(cache_after.get("hits", 0)) > 0
    lower_p95 = after_p95 < before_p95
    throughput_not_lower = after_throughput >= before_throughput
    criteria = {
        "correctness_preserved": correctness,
        "cache_hits_observed": hits_observed,
        "median_p95_lower_than_baseline": lower_p95,
        "median_throughput_not_lower_than_baseline": throughput_not_lower,
    }

    if not correctness or not hits_observed:
        verdict = "invalid_or_functionally_unsupported"
    elif lower_p95 and throughput_not_lower:
        verdict = "supported"
    elif lower_p95 or throughput_not_lower:
        verdict = "partially_supported"
    else:
        verdict = "not_supported"

    energy_after = energy_context()
    if energy_after["condition"] != context["energy_before"]["condition"]:
        raise ValueError("La condición de energía cambió durante la comparación")

    result = {
        "schema_version": 1,
        "scenario_id": "MSG-PERF-CACHE-01",
        "captured_at_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "measured_git_revision": context["git_revision"],
        "baseline_git_revision": baseline["measured_git_revision"],
        "verdict": verdict,
        "criteria": criteria,
        "before": {
            "source": str(baseline_path.relative_to(ROOT)).replace("\\", "/"),
            "valid_run_numbers": baseline_level["valid_run_numbers"],
            "p95_median_ms": before_p95,
            "throughput_median_rps": before_throughput,
        },
        "after": {
            "valid_run_numbers": [2, 3, 4],
            "p95_median_ms": after_p95,
            "throughput_median_rps": after_throughput,
            "p95_change_percent": percent_change(after_p95, before_p95),
            "throughput_change_percent": percent_change(after_throughput, before_throughput),
        },
        "cache_statistics_before": cache_before,
        "cache_statistics_after": cache_after,
        "conditions": {**context, "energy_after": energy_after, "seed": seed},
        "runs": runs,
        "interpretation_boundary": (
            "Comparación local de una conversación caliente y un solo nodo de API; "
            "no demuestra beneficio para múltiples chats, varios nodos ni caché distribuida."
        ),
    }
    write_json(output / "resultado-comparacion.json", result)
    print(json.dumps(result, ensure_ascii=False, indent=2))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True, type=pathlib.Path)
    parser.add_argument("--capture-context", action="store_true")
    parser.add_argument("--baseline", type=pathlib.Path)
    parser.add_argument("--git-revision")
    parser.add_argument("--branch")
    parser.add_argument("--docker-version")
    parser.add_argument("--compose-version")
    parser.add_argument("--postgresql-version")
    args = parser.parse_args()
    output = args.output.resolve()
    if args.capture_context:
        required = (
            args.git_revision,
            args.branch,
            args.docker_version,
            args.compose_version,
            args.postgresql_version,
        )
        if not all(required):
            parser.error("faltan revisión, rama o versiones al capturar contexto")
        capture_context(
            output,
            args.git_revision,
            args.branch,
            args.docker_version,
            args.compose_version,
            args.postgresql_version,
        )
    else:
        if args.baseline is None:
            parser.error("--baseline es obligatorio al agregar resultados")
        aggregate(output, args.baseline.resolve())
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
