from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path


SCRIPTS_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS_DIR))

from check_distribution_decision import find_violations  # noqa: E402


class DistributionDecisionFitnessTests(unittest.TestCase):
    def create_project(
        self,
        root: Path,
        *,
        build: str = 'dependencies { implementation("org.springframework.boot:spring-boot-starter-webmvc") }',
        compose: str | None = None,
        application: str = "app:\n  message-cache:\n    enabled: ${MESSAGE_CACHE_ENABLED:false}\n",
    ) -> None:
        compose = compose or """services:
  db:
    image: postgres:16.14-alpine
  api:
    environment:
      MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-false}
  k6:
    image: grafana/k6:0.54.0
volumes:
  postgres_data:
"""
        (root / "backend/src/main/resources").mkdir(parents=True)
        (root / "backend/build.gradle.kts").write_text(build, encoding="utf-8")
        (root / "docker-compose.yml").write_text(compose, encoding="utf-8")
        (root / "backend/src/main/resources/application.yml").write_text(
            application, encoding="utf-8"
        )

    def test_current_modular_monolith_passes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.create_project(root)
            self.assertEqual([], find_violations(root))

    def test_cache_enabled_by_default_fails(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.create_project(
                root,
                compose="""services:
  db:
    image: postgres:16.14-alpine
  api:
    environment:
      MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-true}
  k6:
    image: grafana/k6:0.54.0
""",
                application="app:\n  message-cache:\n    enabled: ${MESSAGE_CACHE_ENABLED:true}\n",
            )
            violations = find_violations(root)
            self.assertTrue(any("Compose" in item for item in violations))
            self.assertTrue(any("application.yml" in item for item in violations))

    def test_new_broker_service_and_dependency_fail(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.create_project(
                root,
                build='dependencies { implementation("org.springframework.kafka:spring-kafka") }',
                compose="""services:
  db:
    image: postgres:16.14-alpine
  api:
    environment:
      MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-false}
  k6:
    image: grafana/k6:0.54.0
  broker:
    image: apache/kafka:latest
""",
            )
            violations = find_violations(root)
            self.assertTrue(any("Kafka" in item for item in violations))
            self.assertTrue(any("broker" in item for item in violations))
            self.assertTrue(any("infraestructura" in item for item in violations))


if __name__ == "__main__":
    unittest.main()
