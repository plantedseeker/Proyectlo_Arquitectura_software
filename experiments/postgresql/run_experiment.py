"""Mide la consulta paginada de ofertas sobre la API y PostgreSQL locales."""

from __future__ import annotations

import argparse
import concurrent.futures
import datetime as dt
import json
import os
import pathlib
import platform
import subprocess
import time
import urllib.error
import urllib.request

from metrics import summarize


def request_json(url: str, *, method: str = "GET", payload: dict | None = None, token: str | None = None):
    body = json.dumps(payload).encode() if payload is not None else None
    headers = {"Accept": "application/json"}
    if body is not None:
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, data=body, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.status, json.loads(response.read().decode())


def login(base_url: str) -> str:
    status, body = request_json(
        f"{base_url}/api/auth/login",
        method="POST",
        payload={"email": "estudiante@utrabajo.local", "password": "UTrabajo1!"},
    )
    if status != 200 or not body.get("token"):
        raise RuntimeError("no fue posible autenticar la cuenta demo")
    return body["token"]


def measure_once(base_url: str, token: str) -> tuple[float, int]:
    start = time.perf_counter_ns()
    status, body = request_json(f"{base_url}/api/jobs?limit=100&offset=0", token=token)
    elapsed_ms = (time.perf_counter_ns() - start) / 1_000_000
    if status != 200 or not isinstance(body, list) or len(body) != 100:
        raise RuntimeError(f"respuesta inesperada: status={status}, elementos={len(body) if isinstance(body, list) else 'n/a'}")
    return elapsed_ms, len(body)


def run(base_url: str, token: str, requests_count: int, concurrency: int) -> dict:
    started = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
        samples = list(pool.map(lambda _: measure_once(base_url, token), range(requests_count)))
    duration = time.perf_counter() - started
    latencies = [sample[0] for sample in samples]
    return {
        **summarize(latencies),
        "duration_s": round(duration, 3),
        "throughput_requests_s": round(requests_count / duration, 3),
        "items_per_response": samples[0][1],
    }


def git_revision() -> str:
    try:
        return subprocess.check_output(["git", "rev-parse", "HEAD"], text=True, stderr=subprocess.DEVNULL).strip()
    except (OSError, subprocess.CalledProcessError):
        return "unknown"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--runs", type=int, default=4)
    parser.add_argument("--requests", type=int, default=40)
    parser.add_argument("--concurrency", type=int, default=10)
    parser.add_argument("--output", type=pathlib.Path)
    args = parser.parse_args()
    if args.runs < 4:
        parser.error("se requieren al menos 4 corridas: una de calentamiento y tres medidas")

    base_url = args.base_url.rstrip("/")
    token = login(base_url)

    # Comprobación del instrumento: autentica, exige exactamente 100 elementos y
    # verifica que el reloj de alta resolución avanza antes de medir.
    t0 = time.perf_counter_ns()
    time.sleep(0.001)
    if time.perf_counter_ns() <= t0:
        raise RuntimeError("el reloj de medición no avanzó")
    measure_once(base_url, token)

    runs = [run(base_url, token, args.requests, args.concurrency) for _ in range(args.runs)]
    measured = runs[1:]
    result = {
        "schema_version": 1,
        "captured_at_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "hypothesis_threshold_p95_ms": 500,
        "decision": "supported" if all(item["p95_ms"] <= 500 for item in measured) else "not_supported",
        "conditions": {
            "base_url": base_url,
            "synthetic_jobs": 10000,
            "active_distribution": "90%",
            "page_size": 100,
            "requests_per_run": args.requests,
            "concurrency": args.concurrency,
            "runs": args.runs,
            "discarded_run": 1,
            "platform": platform.platform(),
            "python": platform.python_version(),
            "logical_cpus": os.cpu_count(),
            "git_revision": git_revision(),
        },
        "runs": [{"run": index + 1, "discarded": index == 0, **item} for index, item in enumerate(runs)],
    }
    output = args.output or pathlib.Path("docs/experiment/results/baseline.json")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
