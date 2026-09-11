# Mini-comité S8 — soporte de máximo 5 diapositivas

Este archivo define el contenido de la presentación. No registra que el comité ya ocurrió.

## Diapositiva 1 — Problema y drivers

**Título:** ¿Qué decisión arquitectónica debemos proteger?

- UTrabajo mantiene Android + API Spring Boot + PostgreSQL.
- Categoría observada: mensajería.
- Drivers: seguridad/autorización, modularidad, costo operativo, rendimiento y reversibilidad.
- Evidencia S4: p95 mediano de 9,109 ms para últimos 50 mensajes en escenario local.

**Mensaje clave:** no existe evidencia que justifique distribuir mensajería, pero sí conviene proteger límites internos.

## Diapositiva 2 — Alternativas comparadas

**Título:** Tres estilos evaluados

| Alternativa | Ventaja | Costo/Riesgo | Decisión |
| --- | --- | --- | --- |
| Monolito sin regla | mínimo trabajo inicial | deuda y acoplamiento invisible | Descartar |
| Monolito modular + regla | bajo costo y límites verificables | mantener prueba | **Seleccionar** |
| Microservicio + broker | escalado independiente | red, secretos, broker, observabilidad, mayor superficie | Diferir |

**Mensaje clave:** se elige la alternativa menos compleja que satisface la evidencia actual.

## Diapositiva 3 — Decisión y mapa modular

**Título:** Monolito modular con dependencias explícitas

```text
Android presentation
  → Android data / Retrofit
    → Backend controller
      → Backend service / auth
        → JDBC
          → PostgreSQL
```

- ADR-001: mantener mensajería dentro del monolito modular.
- ADR-002: formalizar dependencias permitidas/prohibidas.
- `controller → JDBC` directo queda prohibido.

**Mensaje clave:** el despliegue no cambia; se fortalece la estructura interna.

## Diapositiva 4 — Seguridad y restricción ejecutable

**Título:** ¿Cómo evitamos saltos de capa?

- Bearer + `requireChatParticipant` antes de consultar/escribir mensajes.
- `scripts/check_architecture.py` falla si un controlador importa JDBC/SQL.
- GitHub Actions ejecuta la regla en PR hacia `main`.
- La restricción no reemplaza pruebas funcionales ni revisión de autorización.

**Mensaje clave:** una decisión arquitectónica queda convertida en una condición automática verificable.

## Diapositiva 5 — Evidencia, consecuencias y siguiente paso

**Título:** Evidencia y reversibilidad

- S7: `OFFSET 0` 0,120 ms; `OFFSET 50000` 102,050 ms; cursor 0,123 ms.
- ADR-003 mantiene `LIMIT/OFFSET` para uso actual y deja migración reversible a cursor.
- Microservicio, Redis y broker se difieren por falta de presión medida.
- Próximo paso humano: mini-comité y veredicto real sobre ADR-001/002/003.

**Cierre sugerido:** “La arquitectura elegida no busca más tecnología; busca el mínimo diseño verificable que responda a los drivers actuales y permita evolucionar con evidencia.”
