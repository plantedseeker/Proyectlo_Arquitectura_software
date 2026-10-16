"""Captura el contexto y agrega la evidencia reproducible del Spike 1 S10."""

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


def utc_now() -> dt.datetime:
    return dt.datetime.now(dt.timezone.utc)


def write_json(path: pathlib.Path, value: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(value, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def load_json(path: pathlib.Path) -> dict[str, Any]:
    value = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(value, dict):
        raise ValueError(f"Se esperaba un objeto JSON en {path}")
    return value


def parse_bool(value: str) -> bool:
    normalized = value.strip().lower()
    if normalized not in {"true", "false"}:
        raise ValueError(f"Valor booleano inválido: {value}")
    return normalized == "true"


def capture_context(args: argparse.Namespace) -> None:
    results = args.results_dir.resolve()
    context = {
        "schema_version": 1,
        "experiment": "spike-01-integracion",
        "started_at_utc": utc_now().isoformat(),
        "git": {
            "revision": args.git_revision,
            "branch": args.git_branch,
            "working_tree_clean": parse_bool(args.working_tree_clean),
        },
        "machine": machine_context(),
        "energy_before": energy_context(),
        "deployment_topology": {
            "shared_physical_machine": True,
            "load_generator": "k6 0.54.0 container in Docker Desktop",
            "api": "Spring Boot container in Docker Desktop on the same host",
            "database": "PostgreSQL 16 container in Docker Desktop on the same host",
        },
        "versions": {
            "docker": args.docker_version,
            "docker_compose": args.compose_version,
            "postgresql": args.postgres_version,
            "k6_image": "grafana/k6:0.54.0",
        },
        "test_data": {
            "job_id": args.job_id,
            "expected_company_id": args.company_id,
            "source": "deterministic repository demo data selected before measurement",
        },
        "security": {
            "integration_token_source": args.integration_token_source,
            "integration_token_persisted": False,
        },
        "measurement_plan": {
            "executor": "shared-iterations",
            "virtual_users": 10,
            "measured_requests_per_run": 40,
            "runs": 4,
            "discarded_run": 1,
            "valid_run_numbers": [2, 3, 4],
            "p95_threshold_ms": 500,
            "timebox_minutes": 180,
            "contract_fields": ["jobId", "companyId"],
            "negative_cases_per_run": [
                "missing integration token -> 401",
                "invalid integration token -> 401",
                "unknown job -> 404",
            ],
        },
    }
    write_json(results / "contexto.json", context)
    print(json.dumps(context, ensure_ascii=False, indent=2))


def as_float(value: Any, field: str, run_number: int) -> float:
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        raise ValueError(f"La corrida {run_number} no contiene un número en {field}")
    return float(value)


def rate_is_one(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and abs(float(value) - 1.0) < 1e-12


def rate_is_zero(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and abs(float(value)) < 1e-12


def evaluate_run(run: dict[str, Any], expected_number: int) -> dict[str, Any]:
    if run.get("run") != expected_number:
        raise ValueError(f"Número de corrida inesperado en run-{expected_number}.json")
    expected_discarded = expected_number == 1
    if run.get("discarded") is not expected_discarded:
        raise ValueError(f"Marca discarded incoherente en la corrida {expected_number}")

    latency = run.get("latency_ms")
    if not isinstance(latency, dict):
        raise ValueError(f"Falta latency_ms en la corrida {expected_number}")
    p95 = as_float(latency.get("p95"), "latency_ms.p95", expected_number)
    negative = run.get("negative_checks")
    if not isinstance(negative, dict):
        raise ValueError(f"Falta negative_checks en la corrida {expected_number}")

    structure_passed = (
        run.get("schema_version") == 1
        and run.get("scenario") == "conversation_context"
        and run.get("executor") == "shared-iterations"
        and run.get("virtual_users") == 10
        and run.get("requests") == 40
    )
    functional_passed = (
        rate_is_one(run.get("checks_rate"))
        and rate_is_zero(run.get("failed_request_rate"))
        and rate_is_one(run.get("measured_contract_valid_rate"))
        and rate_is_one(run.get("positive_probe_valid_rate"))
        and rate_is_one(negative.get("missing_token_rejected_rate"))
        and rate_is_one(negative.get("invalid_token_rejected_rate"))
        and rate_is_one(negative.get("missing_job_not_found_rate"))
    )
    p95_passed = p95 <= 500.0
    return {
        "run": expected_number,
        "discarded": expected_discarded,
        "structure_passed": structure_passed,
        "functional_passed": functional_passed,
        "p95_ms": round(p95, 3),
        "p95_threshold_passed": p95_passed,
        "acceptance_passed": structure_passed and functional_passed and p95_passed,
    }


def aggregate(args: argparse.Namespace) -> None:
    results = args.results_dir.resolve()
    context = load_json(results / "contexto.json")
    runs: list[dict[str, Any]] = []
    evaluations: list[dict[str, Any]] = []
    for run_number in range(1, 5):
        run = load_json(results / f"run-{run_number}.json")
        runs.append(run)
        evaluations.append(evaluate_run(run, run_number))

    finished_at = utc_now()
    started_at = dt.datetime.fromisoformat(context["started_at_utc"])
    elapsed_seconds = max(0.0, (finished_at - started_at).total_seconds())
    energy_after = energy_context()
    energy_before = context.get("energy_before", {})
    energy_stable = (
        energy_before.get("condition") not in {None, "unknown"}
        and energy_after.get("condition") == energy_before.get("condition")
    )
    tree_clean = context.get("git", {}).get("working_tree_clean") is True
    within_timebox = elapsed_seconds <= 180 * 60
    structural_validity = all(item["structure_passed"] for item in evaluations)
    experiment_valid = structural_validity and energy_stable and tree_clean and within_timebox

    valid_evaluations = evaluations[1:]
    valid_runs = runs[1:]
    p95_values = [item["p95_ms"] for item in valid_evaluations]
    acceptance_passed = experiment_valid and all(item["acceptance_passed"] for item in valid_evaluations)

    invalid_reasons: list[str] = []
    if not structural_validity:
        invalid_reasons.append("alguna corrida no respetó 10 VU, 40 solicitudes o la estructura fijada")
    if not energy_stable:
        invalid_reasons.append("la condición de energía fue desconocida o cambió durante la medición")
    if not tree_clean:
        invalid_reasons.append("la medición se ejecutó con cambios sin confirmar")
    if not within_timebox:
        invalid_reasons.append("la ejecución excedió el timebox de 180 minutos")

    refutation_reasons: list[str] = []
    if experiment_valid:
        for item in valid_evaluations:
            if not item["functional_passed"]:
                refutation_reasons.append(f"corrida {item['run']}: fallaron checks funcionales o HTTP")
            if not item["p95_threshold_passed"]:
                refutation_reasons.append(f"corrida {item['run']}: p95 {item['p95_ms']} ms > 500 ms")

    if not experiment_valid:
        hypothesis_verdict = "ejecucion_invalida"
        verdict = "ejecucion_invalida"
    elif acceptance_passed:
        hypothesis_verdict = "respaldada"
        verdict = "ajustada"
    else:
        hypothesis_verdict = "refutada"
        verdict = "revertida"

    result = {
        "schema_version": 1,
        "experiment": "spike-01-integracion",
        "category": "Integración Mensajería -> Ofertas",
        "started_at_utc": context["started_at_utc"],
        "finished_at_utc": finished_at.isoformat(),
        "elapsed_seconds": round(elapsed_seconds, 3),
        "verdict": verdict,
        "scope_adjustment": {
            "status": "ajustada",
            "reason": "payload reducido antes de medir a jobId y companyId",
            "evidence": "experimentos/spike-01-integracion/01-enmienda-previa-a-medicion.md",
        },
        "hypothesis_verdict": hypothesis_verdict,
        "experiment_valid": experiment_valid,
        "acceptance_passed": acceptance_passed,
        "invalid_reasons": invalid_reasons,
        "refutation_reasons": refutation_reasons,
        "conditions": {
            **context,
            "energy_after": energy_after,
            "energy_condition_stable": energy_stable,
            "within_180_minute_timebox": within_timebox,
        },
        "valid_runs_summary": {
            "valid_run_count": 3,
            "valid_run_numbers": [2, 3, 4],
            "p95_median_ms": round(statistics.median(p95_values), 3),
            "p95_min_ms": round(min(p95_values), 3),
            "p95_max_ms": round(max(p95_values), 3),
        },
        "run_evaluations": evaluations,
        "runs": runs,
    }
    write_json(results / "resultado.json", result)
    print(json.dumps(result, ensure_ascii=False, indent=2))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)

    capture = subparsers.add_parser("capture-context")
    capture.add_argument("--results-dir", type=pathlib.Path, required=True)
    capture.add_argument("--git-revision", required=True)
    capture.add_argument("--git-branch", required=True)
    capture.add_argument("--working-tree-clean", choices=("true", "false"), required=True)
    capture.add_argument("--docker-version", required=True)
    capture.add_argument("--compose-version", required=True)
    capture.add_argument("--postgres-version", required=True)
    capture.add_argument("--job-id", required=True)
    capture.add_argument("--company-id", required=True)
    capture.add_argument(
        "--integration-token-source",
        choices=("environment", "local-default"),
        required=True,
    )

    aggregate_parser = subparsers.add_parser("aggregate")
    aggregate_parser.add_argument("--results-dir", type=pathlib.Path, required=True)

    args = parser.parse_args()
    if args.command == "capture-context":
        capture_context(args)
    else:
        aggregate(args)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
