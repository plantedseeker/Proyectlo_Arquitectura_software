#!/usr/bin/env python3
"""Genera SVG reproducibles a partir de la evidencia JSON de S11."""

from __future__ import annotations

import html
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parent
GROWTH_RESULT = ROOT / "resultados/20261016T224331Z/resultado.json"
CACHE_RESULT = ROOT / "comparacion-cache/20261016T230407Z/resultado-comparacion.json"
OUTPUT_DIR = ROOT / "graficas"

WIDTH = 1200
HEIGHT = 690
COLORS = {
    "ink": "#172033",
    "muted": "#526071",
    "grid": "#d9e1ea",
    "panel": "#ffffff",
    "background": "#f4f7fb",
    "blue": "#2563eb",
    "cyan": "#0891b2",
    "green": "#15803d",
    "orange": "#c2410c",
    "red": "#b91c1c",
}


class Svg:
    def __init__(self, title: str, description: str) -> None:
        self.parts = [
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{WIDTH}" height="{HEIGHT}" viewBox="0 0 {WIDTH} {HEIGHT}" role="img">',
            f"<title>{html.escape(title)}</title>",
            f"<desc>{html.escape(description)}</desc>",
            "<style>text{font-family:Segoe UI,Arial,sans-serif}</style>",
            f'<rect width="{WIDTH}" height="{HEIGHT}" fill="{COLORS["background"]}"/>',
        ]

    def rect(self, x: float, y: float, width: float, height: float, *, fill: str, rx: float = 0) -> None:
        self.parts.append(
            f'<rect x="{x:.1f}" y="{y:.1f}" width="{width:.1f}" height="{height:.1f}" rx="{rx:.1f}" fill="{fill}"/>'
        )

    def line(self, x1: float, y1: float, x2: float, y2: float, *, stroke: str, width: float = 1, dash: str | None = None) -> None:
        dash_attr = f' stroke-dasharray="{dash}"' if dash else ""
        self.parts.append(
            f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{stroke}" stroke-width="{width:.1f}"{dash_attr}/>'
        )

    def text(self, x: float, y: float, value: str, *, size: int = 16, fill: str | None = None, weight: int = 400, anchor: str = "start") -> None:
        self.parts.append(
            f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" fill="{fill or COLORS["ink"]}" font-weight="{weight}" text-anchor="{anchor}">{html.escape(value)}</text>'
        )

    def polyline(self, points: list[tuple[float, float]], *, stroke: str, width: float = 3) -> None:
        encoded = " ".join(f"{x:.1f},{y:.1f}" for x, y in points)
        self.parts.append(
            f'<polyline points="{encoded}" fill="none" stroke="{stroke}" stroke-width="{width:.1f}" stroke-linejoin="round" stroke-linecap="round"/>'
        )

    def circle(self, x: float, y: float, radius: float, *, fill: str, stroke: str = "#ffffff", width: float = 2) -> None:
        self.parts.append(
            f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{radius:.1f}" fill="{fill}" stroke="{stroke}" stroke-width="{width:.1f}"/>'
        )

    def finish(self) -> str:
        return "\n".join([*self.parts, "</svg>", ""])


def load_json(path: Path) -> dict:
    with path.open(encoding="utf-8-sig") as stream:
        return json.load(stream)


def decimal(value: float, digits: int = 3) -> str:
    return f"{value:.{digits}f}".replace(".", ",")


def scale(value: float, minimum: float, maximum: float, output_min: float, output_max: float) -> float:
    if maximum == minimum:
        return output_min
    ratio = (value - minimum) / (maximum - minimum)
    return output_min + ratio * (output_max - output_min)


def draw_panel(svg: Svg, x: float, y: float, width: float, height: float, title: str, subtitle: str) -> None:
    svg.rect(x, y, width, height, fill=COLORS["panel"], rx=14)
    svg.text(x + 24, y + 36, title, size=20, weight=700)
    svg.text(x + 24, y + 61, subtitle, size=13, fill=COLORS["muted"])


def draw_line_chart(
    svg: Svg,
    *,
    panel_x: float,
    panel_y: float,
    panel_width: float,
    panel_height: float,
    x_values: list[float],
    medians: list[float],
    minima: list[float],
    maxima: list[float],
    y_ticks: list[float],
    color: str,
    unit: str,
) -> None:
    left = panel_x + 66
    right = panel_x + panel_width - 28
    top = panel_y + 94
    bottom = panel_y + panel_height - 62
    y_max = max(y_ticks)

    for tick in y_ticks:
        y = scale(tick, 0, y_max, bottom, top)
        svg.line(left, y, right, y, stroke=COLORS["grid"])
        svg.text(left - 10, y + 5, f"{tick:g}", size=12, fill=COLORS["muted"], anchor="end")

    x_positions = [scale(index, 0, len(x_values) - 1, left, right) for index in range(len(x_values))]
    points: list[tuple[float, float]] = []
    for x, label, median, low, high in zip(x_positions, x_values, medians, minima, maxima, strict=True):
        y_median = scale(median, 0, y_max, bottom, top)
        y_low = scale(low, 0, y_max, bottom, top)
        y_high = scale(high, 0, y_max, bottom, top)
        svg.line(x, y_high, x, y_low, stroke=color, width=2)
        svg.line(x - 6, y_high, x + 6, y_high, stroke=color, width=2)
        svg.line(x - 6, y_low, x + 6, y_low, stroke=color, width=2)
        points.append((x, y_median))
        svg.text(x, bottom + 24, str(int(label)), size=12, fill=COLORS["muted"], anchor="middle")

    svg.polyline(points, stroke=color)
    for (x, y), median in zip(points, medians, strict=True):
        svg.circle(x, y, 5, fill=color)
        svg.text(x, y - 12, decimal(median), size=11, weight=600, anchor="middle")

    svg.text((left + right) / 2, bottom + 48, "Usuarios virtuales (VU)", size=13, fill=COLORS["muted"], anchor="middle")
    svg.text(left, top - 14, f"Unidad: {unit}. Barras: mínimo–máximo; línea: mediana.", size=11, fill=COLORS["muted"])


def generate_growth_chart(growth: dict) -> str:
    levels = growth["level_summaries"]
    vus = [item["load_level_vus"] for item in levels]
    p95 = [item["p95_median_ms"] for item in levels]
    p95_min = [item["p95_min_ms"] for item in levels]
    p95_max = [item["p95_max_ms"] for item in levels]
    throughput = [item["throughput_median_rps"] for item in levels]
    throughput_min = [item["throughput_min_rps"] for item in levels]
    throughput_max = [item["throughput_max_rps"] for item in levels]

    svg = Svg(
        "Crecimiento S11: latencia p95 y throughput",
        "Curvas construidas desde resultado.json para los escalones 10, 50, 100, 150 y 200 VU.",
    )
    svg.text(54, 52, "Experimento de crecimiento S11", size=30, weight=700)
    svg.text(54, 80, "Tres corridas válidas por escalón; la primera corrida se descartó como calentamiento.", size=15, fill=COLORS["muted"])

    draw_panel(
        svg,
        42,
        106,
        548,
        474,
        "Latencia p95",
        "Primera señal creciente; umbral 500 ms fuera de escala",
    )
    draw_line_chart(
        svg,
        panel_x=42,
        panel_y=106,
        panel_width=548,
        panel_height=474,
        x_values=vus,
        medians=p95,
        minima=p95_min,
        maxima=p95_max,
        y_ticks=[0, 20, 40, 60, 80],
        color=COLORS["blue"],
        unit="ms",
    )
    draw_panel(svg, 610, 106, 548, 474, "Throughput", "La capacidad observada continuó creciendo")
    draw_line_chart(
        svg,
        panel_x=610,
        panel_y=106,
        panel_width=548,
        panel_height=474,
        x_values=vus,
        medians=throughput,
        minima=throughput_min,
        maxima=throughput_max,
        y_ticks=[0, 500, 1000, 1500, 2000, 2500],
        color=COLORS["cyan"],
        unit="solicitudes/s",
    )

    svg.rect(42, 600, 1116, 56, fill="#e8f5ec", rx=12)
    svg.text(64, 626, "Resultado: no hubo degradación sostenida hasta 200 VU; no se extrapola más allá.", size=16, fill=COLORS["green"], weight=700)
    svg.text(64, 648, f'Fuente: resultado.json · commit medido {growth["measured_git_revision"][:8]} · cero fallos y páginas válidas.', size=12, fill=COLORS["muted"])
    return svg.finish()


def draw_bar_pair(
    svg: Svg,
    *,
    x: float,
    y: float,
    width: float,
    height: float,
    title: str,
    unit: str,
    before: float,
    after: float,
    change: float,
) -> None:
    draw_panel(svg, x, y, width, height, title, f"Cambio mediano: {decimal(change)} %")
    chart_top = y + 104
    chart_bottom = y + height - 78
    maximum = max(before, after) * 1.18
    bar_width = 112
    positions = [x + width * 0.34, x + width * 0.68]

    for position, label, value, color in zip(
        positions,
        ["Antes", "Después"],
        [before, after],
        [COLORS["muted"], COLORS["blue"]],
        strict=True,
    ):
        bar_height = scale(value, 0, maximum, 0, chart_bottom - chart_top)
        svg.rect(position - bar_width / 2, chart_bottom - bar_height, bar_width, bar_height, fill=color, rx=8)
        svg.text(position, chart_bottom - bar_height - 14, f"{decimal(value)} {unit}", size=15, weight=700, anchor="middle")
        svg.text(position, chart_bottom + 28, label, size=14, fill=COLORS["muted"], weight=600, anchor="middle")

    svg.line(x + 54, chart_bottom, x + width - 42, chart_bottom, stroke=COLORS["grid"], width=2)


def generate_cache_chart(comparison: dict) -> str:
    svg = Svg(
        "Comparación antes y después de la caché local",
        "Comparación de p95 y throughput medianos a 200 VU construida desde resultado-comparacion.json.",
    )
    svg.text(54, 52, "Táctica S11: comparación antes/después", size=30, weight=700)
    svg.text(54, 80, "Caché local TTL de un segundo, mismo escalón de 200 VU y tres corridas válidas.", size=15, fill=COLORS["muted"])

    draw_bar_pair(
        svg,
        x=42,
        y=112,
        width=548,
        height=430,
        title="Latencia p95 mediana",
        unit="ms",
        before=comparison["before"]["p95_median_ms"],
        after=comparison["after"]["p95_median_ms"],
        change=comparison["after"]["p95_change_percent"],
    )
    draw_bar_pair(
        svg,
        x=610,
        y=112,
        width=548,
        height=430,
        title="Throughput mediano",
        unit="req/s",
        before=comparison["before"]["throughput_median_rps"],
        after=comparison["after"]["throughput_median_rps"],
        change=comparison["after"]["throughput_change_percent"],
    )

    svg.rect(42, 564, 1116, 92, fill="#fff4e6", rx=12)
    svg.text(64, 593, "Veredicto: partially_supported; la caché permanece deshabilitada por defecto.", size=16, fill=COLORS["orange"], weight=700)
    svg.text(64, 618, "El p95 bajó 4,908 %, pero el throughput también bajó 1,866 %; los rangos se solapan.", size=14, fill=COLORS["ink"])
    svg.text(64, 641, f'Fuente: resultado-comparacion.json · commit medido {comparison["measured_git_revision"][:8]} · corrección preservada.', size=12, fill=COLORS["muted"])
    return svg.finish()


def main() -> None:
    growth = load_json(GROWTH_RESULT)
    comparison = load_json(CACHE_RESULT)
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    (OUTPUT_DIR / "01-crecimiento-p95-throughput.svg").write_text(
        generate_growth_chart(growth), encoding="utf-8"
    )
    (OUTPUT_DIR / "02-cache-antes-despues.svg").write_text(
        generate_cache_chart(comparison), encoding="utf-8"
    )
    print(f"Gráficas generadas en {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
