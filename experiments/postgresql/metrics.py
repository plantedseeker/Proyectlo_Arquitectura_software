"""Cálculos pequeños y testeables para el experimento de latencia."""

from __future__ import annotations

import math
import statistics
from typing import Iterable


def percentile(values: Iterable[float], percentile_value: float) -> float:
    ordered = sorted(values)
    if not ordered:
        raise ValueError("se necesita al menos una medición")
    if not 0 <= percentile_value <= 100:
        raise ValueError("el percentil debe estar entre 0 y 100")
    rank = max(1, math.ceil(percentile_value / 100 * len(ordered)))
    return ordered[rank - 1]


def summarize(latencies_ms: Iterable[float]) -> dict[str, float | int]:
    values = list(latencies_ms)
    if not values:
        raise ValueError("se necesita al menos una medición")
    return {
        "samples": len(values),
        "min_ms": round(min(values), 3),
        "mean_ms": round(statistics.fmean(values), 3),
        "p50_ms": round(percentile(values, 50), 3),
        "p95_ms": round(percentile(values, 95), 3),
        "max_ms": round(max(values), 3),
    }
