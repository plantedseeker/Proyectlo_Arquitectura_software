"""Captura contexto y agrega el experimento de crecimiento S11."""

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


LEVELS = [10, 50, 100, 150, 200]
RUNS = 4
ITERATIONS_PER_VU = 4
P95_THRESHOLD_MS = 500.0


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
    k6_image: str,
    grafana_cloud_stack_id: str | None,
    grafana_cloud_project_id: str | None,
) -> None:
    context = {
        "schema_version": 1,
        "captured_at_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "git_revision": git_revision,
        "branch": branch,
        "working_tree_clean_before_measurement": True,
        "machine": machine_context(),
        "energy_before": energy_context(),
        "deployment_topology": {
            "shared_physical_machine": True,
            "load_generator": "k6 container in Docker Desktop",
            "api": "Spring Boot container in Docker Desktop",
            "database": "PostgreSQL 16.14 container in Docker Desktop",
        },
        "software_versions": {
            "docker": docker_version,
            "docker_compose": compose_version,
            "postgresql": postgresql_version,
        },
        "instrument": {
            "name": "grafana/k6",
            "image": k6_image,
            "executor": "shared-iterations",
            "levels_vus": LEVELS,
            "iterations_per_vu": ITERATIONS_PER_VU,
            "runs_per_level": RUNS,
            "discarded_run_per_level": 1,
            "grafana_cloud_streaming": grafana_cloud_stack_id is not None,
            "grafana_cloud_stack_id": grafana_cloud_stack_id,
            "grafana_cloud_project_id": grafana_cloud_project_id,
        },
        "baseline_reference": {
            "scenario_id": "MSG-PERF-01",
            "historical_git_revision": "c7c0edd66f49ed28fb694cf9c1970bab03c4b640",
            "historical_p95_median_ms": 9.109,
            "historical_vus": 10,
            "historical_requests_per_run": 40,
        },
    }
    if context["energy_before"]["condition"] == "unknown":
        raise RuntimeError("No se pudo registrar la condición de energía")
    write_json(output / "contexto.json", context)


def parse_percent(value: str | None) -> float | None:
    if not value:
        return None
    try:
        return float(value.strip().rstrip("%"))
    except ValueError:
        return None


def aggregate_telemetry(path: pathlib.Path) -> dict[str, Any]:
    if not path.exists():
        return {"sample_count": 0, "containers": {}}
    peaks: dict[str, dict[str, Any]] = {}
    samples = 0
    for line in path.read_text(encoding="utf-8-sig").splitlines():
        if not line.strip():
            continue
        record = json.loads(line)
        samples += 1
        for container in record.get("containers", []):
            name = container.get("Name", "unknown")
            peak = peaks.setdefault(
                name,
                {"max_cpu_percent": None, "max_memory_percent": None, "sample_count": 0},
            )
            peak["sample_count"] += 1
            cpu = parse_percent(container.get("CPUPerc"))
            memory = parse_percent(container.get("MemPerc"))
            if cpu is not None:
                peak["max_cpu_percent"] = max(peak["max_cpu_percent"] or cpu, cpu)
            if memory is not None:
                peak["max_memory_percent"] = max(peak["max_memory_percent"] or memory, memory)
    return {"sample_count": samples, "containers": peaks}


def run_degraded(run: dict[str, Any]) -> bool:
    statuses = run["response_status_counts"]
    non_200 = sum(float(value) for key, value in statuses.items() if str(key) != "200")
    return (
        float(run["latency_ms"]["p95"]) > P95_THRESHOLD_MS
        or float(run["failed_request_rate"]) > 0
        or float(run["valid_page_rate"]) < 1
        or non_200 > 0
    )


def aggregate(output: pathlib.Path) -> None:
    context = read_json(output / "contexto.json")
    seed = read_json(output / "verificacion-semilla.json")
    if not seed.get("matches_preregistered_seed"):
        raise ValueError("La semilla no coincide con el prerregistro")

    all_runs: list[dict[str, Any]] = []
    level_summaries: list[dict[str, Any]] = []
    for level in LEVELS:
        level_runs = []
        for run_number in range(1, RUNS + 1):
            path = output / f"run-vu{level}-{run_number}.json"
            run = read_json(path)
            expected = level * ITERATIONS_PER_VU
            if run["run"] != run_number or run["load_level_vus"] != level:
                raise ValueError(f"Identidad inesperada en {path}")
            if run["expected_requests"] != expected or run["requests"] != expected:
                raise ValueError(f"Cantidad de solicitudes inválida en {path}")
            run["resource_telemetry"] = aggregate_telemetry(
                output / f"telemetry-vu{level}-{run_number}.jsonl"
            )
            run["degraded"] = run_degraded(run)
            level_runs.append(run)
            all_runs.append(run)

        valid = level_runs[1:]
        degraded_count = sum(1 for run in valid if run["degraded"])
        p95s = [float(run["latency_ms"]["p95"]) for run in valid]
        throughputs = [float(run["throughput_rps"]) for run in valid]
        level_summaries.append(
            {
                "load_level_vus": level,
                "requests_per_run": level * ITERATIONS_PER_VU,
                "valid_run_numbers": [2, 3, 4],
                "degraded_valid_runs": degraded_count,
                "sustained_degradation": degraded_count >= 2,
                "p95_median_ms": round(statistics.median(p95s), 3),
                "p95_min_ms": round(min(p95s), 3),
                "p95_max_ms": round(max(p95s), 3),
                "throughput_median_rps": round(statistics.median(throughputs), 3),
                "throughput_min_rps": round(min(throughputs), 3),
                "throughput_max_rps": round(max(throughputs), 3),
            }
        )

    energy_after = energy_context()
    if energy_after["condition"] != context["energy_before"]["condition"]:
        raise ValueError("La condición de energía cambió durante el experimento")

    first_degraded = next(
        (level["load_level_vus"] for level in level_summaries if level["sustained_degradation"]),
        None,
    )
    result = {
        "schema_version": 1,
        "category": "Mensajería y mesa de ayuda",
        "scenario_id": "MSG-PERF-GROWTH-01",
        "captured_at_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "measured_git_revision": context["git_revision"],
        "hypothesis": "supported" if first_degraded is None else "refuted",
        "first_sustained_degradation_vus": first_degraded,
        "highest_tested_vus": max(LEVELS),
        "interpretation_boundary": (
            "No se observó degradación sostenida hasta 200 VU; no se extrapola más allá."
            if first_degraded is None
            else f"La primera degradación sostenida apareció en {first_degraded} VU."
        ),
        "thresholds": {
            "p95_ms": P95_THRESHOLD_MS,
            "failed_request_rate": 0,
            "valid_page_rate": 1,
            "sustained_rule": "al menos 2 de 3 corridas válidas degradadas",
        },
        "conditions": {**context, "energy_after": energy_after, "seed": seed},
        "level_summaries": level_summaries,
        "runs": all_runs,
        "limitation_classification": "pending_evidence_analysis",
        "selected_tactic": None,
    }
    write_json(output / "resultado.json", result)
    print(json.dumps(result, ensure_ascii=False, indent=2))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True, type=pathlib.Path)
    parser.add_argument("--capture-context", action="store_true")
    parser.add_argument("--git-revision")
    parser.add_argument("--branch")
    parser.add_argument("--docker-version")
    parser.add_argument("--compose-version")
    parser.add_argument("--postgresql-version")
    parser.add_argument("--k6-image", default="grafana/k6:0.54.0")
    parser.add_argument("--grafana-cloud-stack-id")
    parser.add_argument("--grafana-cloud-project-id")
    args = parser.parse_args()
    output = args.output.resolve()
    if args.capture_context:
        required = (
            args.git_revision,
            args.branch,
            args.docker_version,
            args.compose_version,
            args.postgresql_version,
            args.k6_image,
        )
        if not all(required):
            parser.error("faltan revision, rama o versiones al capturar contexto")
        capture_context(
            output,
            args.git_revision,
            args.branch,
            args.docker_version,
            args.compose_version,
            args.postgresql_version,
            args.k6_image,
            args.grafana_cloud_stack_id,
            args.grafana_cloud_project_id,
        )
    else:
        aggregate(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
