#!/usr/bin/env python3
"""Fitness function de ADR-004: evita distribución accidental sin re-decisión."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
ADR_PATH = "docs/adr/ADR-004-distribucion-mensajeria.md"
ALLOWED_COMPOSE_SERVICES = {"db", "api", "k6"}
BANNED_DEPENDENCIES = {
    "Kafka": "spring-kafka",
    "RabbitMQ/AMQP": "spring-boot-starter-amqp",
    "Redis": "spring-boot-starter-data-redis",
    "Pulsar": "spring-pulsar",
    "NATS": "io.nats",
    "Jedis": "redis.clients:jedis",
    "Lettuce": "io.lettuce:lettuce-core",
}
BANNED_INFRASTRUCTURE = re.compile(
    r"(?im)^\s*image:\s*[^\n]*(?:redis|rabbitmq|kafka|confluentinc|pulsar|nats)"
)


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig")


def compose_services(compose: str) -> set[str]:
    services: set[str] = set()
    inside_services = False
    for line in compose.splitlines():
        if line.strip() == "services:":
            inside_services = True
            continue
        if inside_services and line and not line[0].isspace() and not line.lstrip().startswith("#"):
            break
        if inside_services:
            match = re.match(r"^  ([A-Za-z0-9_.-]+):\s*(?:#.*)?$", line)
            if match:
                services.add(match.group(1))
    return services


def find_violations(project_root: Path) -> list[str]:
    build_path = project_root / "backend/build.gradle.kts"
    compose_path = project_root / "docker-compose.yml"
    application_path = project_root / "backend/src/main/resources/application.yml"
    required = (build_path, compose_path, application_path)
    missing = [str(path.relative_to(project_root)) for path in required if not path.is_file()]
    if missing:
        return [f"archivo requerido ausente: {path}" for path in missing]

    build = read_text(build_path)
    compose = read_text(compose_path)
    application = read_text(application_path)
    violations: list[str] = []

    for label, marker in BANNED_DEPENDENCIES.items():
        if marker.lower() in build.lower():
            violations.append(f"dependencia distribuida no aprobada: {label} ({marker})")

    unexpected_services = sorted(compose_services(compose) - ALLOWED_COMPOSE_SERVICES)
    for service in unexpected_services:
        violations.append(f"servicio Compose no aprobado: {service}")

    if BANNED_INFRASTRUCTURE.search(compose):
        violations.append("infraestructura broker/cache distribuida no aprobada en Compose")

    if "MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-false}" not in compose:
        violations.append("Compose debe mantener MESSAGE_CACHE_ENABLED=false por defecto")
    if "enabled: ${MESSAGE_CACHE_ENABLED:false}" not in application:
        violations.append("application.yml debe mantener la caché de mensajes deshabilitada")

    return violations


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Impide distribuir Mensajería o activar su caché sin revisar ADR-004."
    )
    parser.add_argument("--project-root", type=Path, default=PROJECT_ROOT)
    args = parser.parse_args()
    root = args.project_root.resolve()

    try:
        violations = find_violations(root)
    except (OSError, UnicodeError) as error:
        print(f"No se pudo ejecutar la fitness function ADR-004: {error}", file=sys.stderr)
        return 2

    if violations:
        print("ADR-004 violado: la decisión de distribución requiere revisión explícita.", file=sys.stderr)
        print(f"Decisión protegida: {ADR_PATH}", file=sys.stderr)
        for violation in violations:
            print(f"- {violation}", file=sys.stderr)
        return 1

    print(
        "ADR-004 OK: un solo servicio de aplicación, sin broker/cache distribuida "
        "y caché experimental deshabilitada."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
