"""Mide la consulta paginada de ofertas sobre la API y PostgreSQL locales."""

from __future__ import annotations

import argparse
import concurrent.futures
import ctypes
import datetime as dt
import json
import os
import pathlib
import platform
import subprocess
import time
import urllib.error
import urllib.request

from metrics import summarize, summarize_valid_runs


def _windows_registry_value(path: str, name: str) -> str | None:
    try:
        import winreg

        with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, path) as key:
            value, _ = winreg.QueryValueEx(key, name)
        return str(value).strip() or None
    except (ImportError, OSError):
        return None


def _physical_memory_gib() -> float | None:
    if platform.system() == "Windows":
        class MemoryStatusEx(ctypes.Structure):
            _fields_ = [
                ("length", ctypes.c_ulong),
                ("memory_load", ctypes.c_ulong),
                ("total_physical", ctypes.c_ulonglong),
                ("available_physical", ctypes.c_ulonglong),
                ("total_page_file", ctypes.c_ulonglong),
                ("available_page_file", ctypes.c_ulonglong),
                ("total_virtual", ctypes.c_ulonglong),
                ("available_virtual", ctypes.c_ulonglong),
                ("available_extended_virtual", ctypes.c_ulonglong),
            ]

        status = MemoryStatusEx()
        status.length = ctypes.sizeof(status)
        if ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(status)):
            return round(status.total_physical / (1024**3), 1)
        return None

    try:
        page_size = os.sysconf("SC_PAGE_SIZE")
        pages = os.sysconf("SC_PHYS_PAGES")
        return round(page_size * pages / (1024**3), 1)
    except (AttributeError, OSError, ValueError):
        return None


def machine_context() -> dict:
    manufacturer = None
    model = None
    processor = platform.processor().strip() or None
    if platform.system() == "Windows":
        bios_path = r"HARDWARE\DESCRIPTION\System\BIOS"
        cpu_path = r"HARDWARE\DESCRIPTION\System\CentralProcessor\0"
        manufacturer = _windows_registry_value(bios_path, "SystemManufacturer")
        model = _windows_registry_value(bios_path, "SystemProductName")
        processor = _windows_registry_value(cpu_path, "ProcessorNameString") or processor

    memory_gib = _physical_memory_gib()
    parts = [part for part in (manufacturer, model, processor) if part]
    if memory_gib is not None:
        parts.append(f"{memory_gib} GiB RAM")
    return {
        "machine_signature": "; ".join(parts) or platform.node() or "unknown",
        "manufacturer": manufacturer,
        "model": model,
        "processor": processor,
        "physical_memory_gib": memory_gib,
        "logical_cpus": os.cpu_count(),
        "operating_system": platform.platform(),
        "architecture": platform.machine(),
    }


def energy_context() -> dict:
    condition = "unknown"
    battery_percent = None
    active_power_scheme = None

    if platform.system() == "Windows":
        class SystemPowerStatus(ctypes.Structure):
            _fields_ = [
                ("ac_line_status", ctypes.c_ubyte),
                ("battery_flag", ctypes.c_ubyte),
                ("battery_life_percent", ctypes.c_ubyte),
                ("system_status_flag", ctypes.c_ubyte),
                ("battery_life_time", ctypes.c_ulong),
                ("battery_full_life_time", ctypes.c_ulong),
            ]

        status = SystemPowerStatus()
        if ctypes.windll.kernel32.GetSystemPowerStatus(ctypes.byref(status)):
            condition = {0: "battery", 1: "plugged_in"}.get(status.ac_line_status, "unknown")
            if status.battery_life_percent != 255:
                battery_percent = int(status.battery_life_percent)

        try:
            completed = subprocess.run(
                ["powercfg", "/getactivescheme"],
                check=True,
                capture_output=True,
                text=True,
                errors="replace",
            )
            active_power_scheme = completed.stdout.strip() or None
        except (OSError, subprocess.CalledProcessError):
            pass

    return {
        "condition": condition,
        "battery_percent": battery_percent,
        "active_power_scheme": active_power_scheme,
        "captured_during_measurement": True,
    }


def topology_context(topology: str) -> dict:
    if topology == "same-machine":
        return {
            "shared_physical_machine": True,
            "load_generator": "Python process on the Windows host",
            "api": "Docker Desktop container on the same host",
            "database": "PostgreSQL 16 Docker Desktop container on the same host",
        }
    return {
        "shared_physical_machine": False,
        "description": "Distributed deployment declared by the experiment operator",
    }


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
    parser.add_argument("--git-revision")
    parser.add_argument(
        "--deployment-topology",
        choices=("same-machine", "distributed"),
        required=True,
        help="declara si generador, API y PostgreSQL comparten la máquina física",
    )
    parser.add_argument(
        "--energy-condition",
        choices=("plugged_in", "battery"),
        help="sobrescribe la detección automática cuando el sistema no la expone",
    )
    parser.add_argument("--power-profile", help="perfil de energía si no puede detectarse automáticamente")
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

    machine = machine_context()
    energy = energy_context()
    if args.energy_condition:
        energy["condition"] = args.energy_condition
        energy["condition_source"] = "operator_argument"
    else:
        energy["condition_source"] = "operating_system"
    if args.power_profile:
        energy["active_power_scheme"] = args.power_profile
        energy["power_profile_source"] = "operator_argument"
    else:
        energy["power_profile_source"] = "operating_system"
    if energy["condition"] == "unknown":
        parser.error("no se detectó la condición de energía; use --energy-condition")

    runs = [run(base_url, token, args.requests, args.concurrency) for _ in range(args.runs)]
    run_results = [{"run": index + 1, "discarded": index == 0, **item} for index, item in enumerate(runs)]
    measured = run_results[1:]
    result = {
        "schema_version": 2,
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
            "python": platform.python_version(),
            "git_revision": args.git_revision or git_revision(),
            "machine": machine,
            "energy": energy,
            "deployment_topology": topology_context(args.deployment_topology),
            "instrument_scope": {
                "measured_component": "HTTP API and PostgreSQL response path",
                "android_client_included": False,
                "catalog_category": "pending_teacher_confirmation",
                "claim_limit": "This result does not measure Android rendering or end-to-end mobile latency",
            },
        },
        "valid_runs_summary": summarize_valid_runs(measured),
        "runs": run_results,
    }
    output = args.output or pathlib.Path("docs/experiment/results/baseline-s4-audit.json")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
