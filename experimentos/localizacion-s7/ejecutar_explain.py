#!/usr/bin/env python3
"""Captura planes PostgreSQL para localizar el costo de paginación de mensajes."""

from __future__ import annotations

import argparse
import json
import os
import platform
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


CHAT_ID = "3fb4ce25-b840-4685-d2ea-53f9a4bdedc6"
PAGE_SIZE = 50
DEEP_OFFSET = 50_000
PROJECT_ROOT = Path(__file__).resolve().parents[2]


def run(command: list[str], *, cwd: Path = PROJECT_ROOT) -> str:
    completed = subprocess.run(
        command,
        cwd=cwd,
        check=False,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if completed.returncode != 0:
        raise RuntimeError(
            f"Comando falló ({completed.returncode}): {' '.join(command)}\n"
            f"{completed.stderr.strip()}"
        )
    return completed.stdout.strip()


def git_revision() -> str:
    """Lee HEAD sin exigir que el usuario de Windows sea dueño del worktree."""
    return run(
        [
            "git",
            "-c",
            f"safe.directory={PROJECT_ROOT}",
            "-C",
            str(PROJECT_ROOT),
            "rev-parse",
            "HEAD",
        ]
    )


def psql(compose: str, sql: str) -> str:
    return run(
        [
            compose,
            "exec",
            "-T",
            "db",
            "psql",
            "-X",
            "-q",
            "-t",
            "-A",
            "-v",
            "ON_ERROR_STOP=1",
            "-U",
            "utrabajo",
            "-d",
            "utrabajo",
            "-c",
            sql,
        ]
    )


def sql_literal(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def explain(compose: str, query: str) -> dict[str, Any]:
    raw = psql(compose, f"EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) {query}")
    parsed = json.loads(raw)
    if not isinstance(parsed, list) or len(parsed) != 1:
        raise RuntimeError("PostgreSQL no devolvió el plan JSON esperado.")
    return parsed[0]


def walk_plan(node: dict[str, Any]) -> list[dict[str, Any]]:
    nodes = [node]
    for child in node.get("Plans", []):
        nodes.extend(walk_plan(child))
    return nodes


def summarize(plan: dict[str, Any]) -> dict[str, Any]:
    root = plan["Plan"]
    nodes = walk_plan(root)
    indexes = sorted({node["Index Name"] for node in nodes if "Index Name" in node})
    return {
        "planning_time_ms": plan.get("Planning Time"),
        "execution_time_ms": plan.get("Execution Time"),
        "returned_rows": root.get("Actual Rows"),
        "root_node": root.get("Node Type"),
        "node_types": sorted({node.get("Node Type", "unknown") for node in nodes}),
        "indexes": indexes,
        "shared_hit_blocks": int(root.get("Shared Hit Blocks", 0)),
        "shared_read_blocks": int(root.get("Shared Read Blocks", 0)),
        "temp_read_blocks": int(root.get("Temp Read Blocks", 0)),
        "temp_written_blocks": int(root.get("Temp Written Blocks", 0)),
    }


def paginated_query(*, offset: int | None = None, cursor: tuple[str, str] | None = None) -> str:
    predicate = f"chat_id = '{CHAT_ID}'::uuid"
    suffix = ""
    if cursor is not None:
        sent_at, message_id = cursor
        predicate += (
            f" AND (sent_at, id) < ({sql_literal(sent_at)}::timestamptz, "
            f"{sql_literal(message_id)}::uuid)"
        )
    else:
        suffix = f" OFFSET {offset or 0}"
    return f"""
        SELECT id, chat_id, sender_id, body, sent_at
        FROM (
            SELECT id, chat_id, sender_id, body, sent_at
            FROM message
            WHERE {predicate}
            ORDER BY sent_at DESC, id DESC
            LIMIT {PAGE_SIZE}{suffix}
        ) recent
        ORDER BY sent_at ASC, id ASC
    """


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--compose", default="docker-compose")
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("experimentos/localizacion-s7/resultados/localizacion.json"),
    )
    args = parser.parse_args()

    message_count = int(
        psql(
            args.compose,
            f"SELECT count(*) FROM message WHERE chat_id = '{CHAT_ID}'::uuid;",
        )
    )
    if message_count != 100_000:
        raise RuntimeError(
            f"Semilla inválida: se esperaban 100000 mensajes y se encontraron {message_count}."
        )

    cursor_raw = psql(
        args.compose,
        f"""
        SELECT sent_at::text || '|' || id::text
        FROM message
        WHERE chat_id = '{CHAT_ID}'::uuid
        ORDER BY sent_at DESC, id DESC
        OFFSET {DEEP_OFFSET - 1} LIMIT 1;
        """,
    )
    cursor_parts = cursor_raw.split("|", maxsplit=1)
    if len(cursor_parts) != 2:
        raise RuntimeError("No se pudo obtener el cursor determinista.")
    cursor = (cursor_parts[0].strip(), cursor_parts[1].strip())

    participant_id = psql(
        args.compose,
        "SELECT id::text FROM app_user WHERE email = 'estudiante@utrabajo.local';",
    ).strip()
    if not participant_id:
        raise RuntimeError("No se encontró el participante sintético del chat.")

    queries = {
        "participant_authorization": f"""
            SELECT count(*)
            FROM chat
            WHERE id = '{CHAT_ID}'::uuid
              AND (student_id = {sql_literal(participant_id)}::uuid
                   OR company_id = {sql_literal(participant_id)}::uuid)
        """,
        "recent_offset_0": paginated_query(offset=0),
        "deep_offset_50000": paginated_query(offset=DEEP_OFFSET),
        "keyset_after_49999": paginated_query(cursor=cursor),
    }
    scenarios: dict[str, Any] = {}
    for name, query in queries.items():
        plan = explain(args.compose, query)
        scenarios[name] = {"query": " ".join(query.split()), "summary": summarize(plan), "plan": plan}

    deep_ms = scenarios["deep_offset_50000"]["summary"]["execution_time_ms"]
    keyset_ms = scenarios["keyset_after_49999"]["summary"]["execution_time_ms"]
    ratio = round(deep_ms / keyset_ms, 3) if keyset_ms else None

    payload = {
        "schema_version": 1,
        "scenario_id": "MSG-LOC-01",
        "category": "Mensajería y mesa de ayuda",
        "captured_at_utc": datetime.now(timezone.utc).isoformat(),
        "git_revision": git_revision(),
        "conditions": {
            "host": platform.node(),
            "platform": platform.platform(),
            "processor": platform.processor(),
            "logical_cpus": os.cpu_count(),
            "manufacturer": os.environ.get("S7_MACHINE_MANUFACTURER", "not_available"),
            "model": os.environ.get("S7_MACHINE_MODEL", "not_available"),
            "physical_memory_gib": os.environ.get("S7_MEMORY_GIB", "not_available"),
            "energy_condition": os.environ.get("S7_ENERGY_CONDITION", "not_available"),
            "battery_percent": os.environ.get("S7_BATTERY_PERCENT", "not_available"),
            "active_power_scheme": os.environ.get("S7_POWER_SCHEME", "not_available"),
            "postgres_version": psql(args.compose, "SHOW server_version;"),
            "shared_physical_machine": True,
            "load_generator": "Python script on host",
            "database": "PostgreSQL container in local Docker Desktop",
            "api_included": False,
        },
        "controls": {
            "chat_id": CHAT_ID,
            "message_count": message_count,
            "page_size": PAGE_SIZE,
            "deep_offset": DEEP_OFFSET,
            "participant_id": participant_id,
            "cursor": {"sent_at": cursor[0], "id": cursor[1]},
        },
        "derived_comparison": {"deep_offset_to_keyset_execution_time_ratio": ratio},
        "scenarios": scenarios,
    }

    output = args.output if args.output.is_absolute() else PROJECT_ROOT / args.output
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(payload, ensure_ascii=False, indent=2))
    print(f"\nResultado guardado en {output}")


if __name__ == "__main__":
    main()
