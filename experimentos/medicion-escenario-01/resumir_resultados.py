"""Captura el entorno y agrega las cuatro corridas producidas por k6."""

from __future__ import annotations

import argparse
import datetime as dt
import json
import pathlib
import statistics
import sys


ROOT = pathlib.Path(__file__).resolve().parents[2]
RESULTS = pathlib.Path(__file__).resolve().parent / "resultados"
sys.path.insert(0, str(ROOT / "experiments" / "postgresql"))

from run_experiment import energy_context, machine_context  # noqa: E402


def write_json(path: pathlib.Path, value: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def capture_context(git_revision: str) -> None:
    context = {
        "captured_at_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "git_revision": git_revision,
        "machine": machine_context(),
        "energy_before": energy_context(),
        "deployment_topology": {
            "shared_physical_machine": True,
            "load_generator": "k6 container in Docker Desktop",
            "api": "Spring Boot container in Docker Desktop",
            "database": "PostgreSQL 16 container in Docker Desktop",
        },
        "load_tool": {
            "name": "k6",
            "container_image": "grafana/k6:0.54.0",
        },
    }
    if context["energy_before"]["condition"] == "unknown":
        raise RuntimeError("No se pudo registrar la condición de energía")
    write_json(RESULTS / "contexto.json", context)
    print(json.dumps(context, ensure_ascii=False, indent=2))


def aggregate() -> None:
    context_path = RESULTS / "contexto.json"
    if not context_path.exists():
        raise FileNotFoundError("Falta resultados/contexto.json")
    context = json.loads(context_path.read_text(encoding="utf-8"))
    runs = []
    for run_number in range(1, 5):
        path = RESULTS / f"run-{run_number}.json"
        run = json.loads(path.read_text(encoding="utf-8"))
        if run["run"] != run_number:
            raise ValueError(f"Número de corrida inesperado en {path}")
        if run["requests"] != 40:
            raise ValueError(f"La corrida {run_number} no contiene 40 solicitudes medidas")
        if not run["thresholds_passed"] or run["checks_rate"] != 1 or run["valid_page_rate"] != 1:
            raise ValueError(f"La corrida {run_number} contiene errores o incumple umbrales")
        runs.append(run)

    valid = runs[1:]
    p95_values = [float(run["p95_ms"]) for run in valid]
    energy_after = energy_context()
    if energy_after["condition"] != context["energy_before"]["condition"]:
        raise ValueError("La condición de energía cambió durante el experimento")

    result = {
        "schema_version": 1,
        "category": "Mensajería y mesa de ayuda",
        "scenario_id": "MSG-PERF-01",
        "captured_at_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "decision": "supported" if all(value <= 500 for value in p95_values) else "not_supported",
        "hypothesis_threshold_p95_ms": 500,
        "conditions": {
            **context,
            "energy_after": energy_after,
            "seed": {
                "conversations": 1000,
                "small_conversations_10_messages": 900,
                "medium_conversations_1000_messages": 90,
                "large_conversations_10000_messages": 9,
                "extreme_conversations_100000_messages": 1,
                "total_messages": 289000,
            },
            "operation": "GET latest 50 messages from the 100000-message conversation",
            "requests_per_run": 40,
            "virtual_users": 10,
            "runs": 4,
            "discarded_run": 1,
        },
        "valid_runs_summary": {
            "valid_run_count": 3,
            "valid_run_numbers": [2, 3, 4],
            "p95_median_ms": round(statistics.median(p95_values), 3),
            "p95_min_ms": round(min(p95_values), 3),
            "p95_max_ms": round(max(p95_values), 3),
        },
        "runs": runs,
    }
    write_json(RESULTS / "resultado.json", result)
    print(json.dumps(result, ensure_ascii=False, indent=2))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--capture-context", action="store_true")
    parser.add_argument("--git-revision")
    args = parser.parse_args()
    if args.capture_context:
        if not args.git_revision:
            parser.error("--git-revision es obligatorio al capturar contexto")
        capture_context(args.git_revision)
    else:
        aggregate()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
