#!/usr/bin/env python3
"""Restricción ejecutable de ADR-002 para los controladores del backend."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONTROLLER_DIR = (
    PROJECT_ROOT / "backend/src/main/kotlin/com/tab/utrabajo/api/controller"
)
ADR_PATH = "docs/adr/ADR-002-limites-modulos-dependencias.md"
FORBIDDEN_PATTERNS = {
    "Spring JDBC": re.compile(r"\borg\.springframework\.jdbc(?:\.|\b)"),
    "JdbcClient": re.compile(r"\bJdbcClient\b"),
    "java.sql": re.compile(r"\bjava\.sql(?:\.|\b)"),
    "javax.sql": re.compile(r"\bjavax\.sql(?:\.|\b)"),
}


def find_violations(controller_dir: Path) -> list[str]:
    if not controller_dir.is_dir():
        raise FileNotFoundError(f"No existe el directorio de controladores: {controller_dir}")

    violations: list[str] = []
    for source_file in sorted(controller_dir.rglob("*.kt")):
        source = source_file.read_text(encoding="utf-8")
        for line_number, line in enumerate(source.splitlines(), start=1):
            for label, pattern in FORBIDDEN_PATTERNS.items():
                if pattern.search(line):
                    relative = source_file.relative_to(PROJECT_ROOT) if source_file.is_relative_to(PROJECT_ROOT) else source_file
                    violations.append(f"{relative}:{line_number}: referencia prohibida a {label}")
    return violations


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Impide que controladores Kotlin accedan directamente a JDBC/SQL."
    )
    parser.add_argument("--controller-dir", type=Path, default=DEFAULT_CONTROLLER_DIR)
    args = parser.parse_args()

    try:
        violations = find_violations(args.controller_dir.resolve())
    except (OSError, UnicodeError) as error:
        print(f"No se pudo ejecutar la restricción ADR-002: {error}", file=sys.stderr)
        return 2

    if violations:
        print("ADR-002 violado: los controladores deben delegar en servicios.", file=sys.stderr)
        print(f"Decisión protegida: {ADR_PATH}", file=sys.stderr)
        for violation in violations:
            print(f"- {violation}", file=sys.stderr)
        return 1

    print("ADR-002 OK: ningún controlador referencia JDBC/SQL directamente.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
