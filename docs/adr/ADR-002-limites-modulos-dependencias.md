# ADR-002 — Límites de módulos y dependencias permitidas

- Fecha: 2026-09-11
- Estado: **Propuesta completa; pendiente de ratificación por el equipo y mini-comité**
- Categoría: Modularidad y mantenibilidad
- Protege: separación de responsabilidades, seguridad, trazabilidad y evolución del backend

## Contexto

UTrabajo mantiene un único despliegue backend Spring Boot, pero el código ya muestra una dirección de dependencias clara: los controladores reciben HTTP, delegan en servicios o autenticación y estos acceden a persistencia mediante JDBC. El C3 y la trazabilidad del repositorio permiten verificar esa estructura.

El riesgo principal es que un cambio futuro salte capas —por ejemplo, que un controlador consulte `JdbcClient` directamente— y mezcle protocolo HTTP, reglas de negocio, autorización y persistencia. Esa degradación puede ocurrir sin cambiar el despliegue y sería difícil de detectar solo con revisión visual.

## Decisión

Mantener los módulos lógicos actuales y hacer explícitas las dependencias permitidas y prohibidas.

```text
Android presentation
    ↓
Android data / Retrofit
    ↓ HTTP + Bearer
Backend controller
    ↓
Backend service / auth
    ↓
Persistencia JDBC
    ↓
PostgreSQL 16
```

### Dependencias permitidas

| Origen | Puede depender de |
| --- | --- |
| Android `presentation` | `data.UTrabajoRepository`, modelos de UI |
| Android `data` | Retrofit/HTTP, modelos de datos, SharedPreferences |
| Backend `controller` | servicios, modelos y seguridad web |
| Backend `auth` | JDBC, configuración de seguridad y modelos necesarios |
| Backend `service` | JDBC, almacenamiento y modelos |
| Persistencia Flyway | PostgreSQL |
| `FileStorageService` | sistema de archivos configurado |

### Dependencias prohibidas directamente

- Android no accede a PostgreSQL ni usa credenciales de base de datos.
- Android `presentation` no conoce JDBC ni clases internas del backend.
- Backend `controller` no importa `JdbcClient`, `org.springframework.jdbc`, `java.sql` ni `javax.sql`.
- Persistencia no depende de HTTP o de la navegación Android.
- No se introduce una dependencia hacia un microservicio, broker o Redis que no exista en el sistema real.

## Regla ejecutable

La primera restricción automática de S8 protege el límite más crítico y fácil de comprobar:

```text
controller !→ JDBC/SQL directo
```

`scripts/check_architecture.py` inspecciona el paquete de controladores y falla si encuentra referencias a APIs JDBC/SQL prohibidas. `.github/workflows/ci.yml` ejecuta la regla en cada pull request hacia `main` y en `push` a `main`.

La regla es deliberadamente mínima. No afirma detectar todas las formas de acoplamiento; su objetivo es convertir una decisión arquitectónica concreta en una condición verificable.

## Alternativas consideradas

### A. Mantener límites solo como convención

- **Beneficio:** cero mantenimiento de pruebas.
- **Costo:** una violación puede entrar al código sin señal automática.
- **Seguridad:** aumenta la posibilidad de que autorización y acceso a datos se dispersen.
- **Decisión:** descartada.

### B. Límites explícitos + restricción CI — seleccionada

- **Beneficio:** bajo costo, feedback temprano y trazabilidad directa hacia el ADR.
- **Costo:** mantener la regla y actualizarla cuando el diseño cambie legítimamente.
- **Seguridad:** ayuda a preservar el punto de autorización/negocio antes de persistencia.
- **Reversibilidad:** alta; cualquier ajuste se hace mediante otro cambio documentado.

### C. Separar físicamente los módulos en microservicios

- **Beneficio potencial:** frontera de despliegue más fuerte.
- **Costo:** red, contratos, secretos, observabilidad, consistencia y operación distribuidas.
- **Evidencia:** S4/S7 no muestran una presión que justifique esa complejidad.
- **Decisión:** diferida.

## Consecuencias

### Positivas

- La estructura declarada en C3 queda protegida por una regla concreta.
- Los controladores se mantienen enfocados en HTTP y delegación.
- Autorización y reglas de dominio siguen localizables.
- La arquitectura puede evolucionar sin distribuir el sistema prematuramente.

### Negativas

- `UTrabajoService` continúa siendo amplio y puede necesitar división interna futura.
- La regla actual cubre solo el límite controller/JDBC.
- Nuevos módulos requerirán actualizar documentación y pruebas si cambian las dependencias válidas.

## Reversibilidad y disparadores de revisión

Revisar este ADR si se divide `UTrabajoService` en servicios internos, se extrae un dominio a otro proceso, se introducen repositorios/puertos de persistencia o la regla actual produce falsos positivos que revelen un cambio legítimo de arquitectura.

No se elimina una restricción solo para hacer pasar CI: cualquier cambio de frontera debe quedar justificado en un PR y en el ADR correspondiente.

## Evidencia

- [`Mapa modular`](../architecture/modulos-y-limites.md)
- [`Decisión de estilo S7`](../architecture/08-decision-estilo-arquitectonico.md)
- [`ADR-001`](ADR-001-limites-modulo-mensajeria.md)
- [`C3 backend`](../../dossier/07-c4-componentes-backend.md)
- [`Trazabilidad`](../../dossier/09-c4-trazabilidad-localizacion.md)
- [`Regla ejecutable`](../../scripts/check_architecture.py)
- [`Workflow CI`](../../.github/workflows/ci.yml)

## Confirmación requerida

El equipo debe cambiar el estado a `Aceptada`, `Ajustada` o `Reconsiderada` únicamente después del mini-comité real y del registro del veredicto.