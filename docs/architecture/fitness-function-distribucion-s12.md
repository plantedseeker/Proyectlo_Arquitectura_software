# Segunda fitness function — decisión de distribución S12

## Decisión protegida

ADR-004 propone mantener Mensajería en el monolito modular y conservar la caché
experimental deshabilitada. La restricción evita que un cambio cotidiano
introduzca silenciosamente microservicios, broker o caché distribuida sin
reabrir la decisión.

## Regla ejecutable

`scripts/check_distribution_decision.py` exige simultáneamente:

1. solo existen en Compose `db`, `api` y el generador experimental `k6`;
2. no aparecen imágenes de Redis, RabbitMQ, Kafka, Pulsar o NATS;
3. el backend no incorpora clientes/dependencias de esos brokers o cachés;
4. `MESSAGE_CACHE_ENABLED` permanece en `false` por defecto tanto en Compose
   como en `application.yml`.

El script no afirma que esas tecnologías sean incorrectas. Obliga a que su
introducción venga acompañada de nueva evidencia y una revisión explícita de
ADR-004.

## Umbral y fallo de CI

- **Umbral:** cero violaciones.
- **Cumple:** salida 0 y mensaje `ADR-004 OK`.
- **Viola:** una o más infracciones, salida 1 y listado de causas.
- **Error del instrumento:** archivos ausentes o ilegibles, salida 2.
- **Qué rompe CI:** cualquier salida distinta de cero en el job backend.

## Verificación local

```powershell
python .\scripts\check_distribution_decision.py
python -m unittest discover -s .\scripts\tests -v
```

Prueba negativa controlada sin modificar el repositorio:

```powershell
python -m unittest scripts.tests.test_check_distribution_decision -v
```

Los tests construyen repositorios temporales y comprueban que activar la caché,
agregar Kafka o introducir un servicio `broker` son rechazados.

## Relación con la primera fitness function

- `DatabaseIsolationArchitectureTests` protege ADR-002 con ArchUnit: los
  controladores no acceden a JDBC/SQL, la persistencia queda limitada a los
  paquetes autorizados y no hay ciclos entre paquetes superiores.
- `check_architecture.py` conserva una comprobación de fuente complementaria.
- `check_distribution_decision.py` protege ADR-004: no cambia silenciosamente
  la topología ni la infraestructura de distribución.

Son restricciones distintas y ambas deben pasar.

## Procedimiento de re-decisión

Si el profesor introduce una restricción nueva o la evidencia futura activa un
disparador de ADR-004:

1. conservar el fallo de CI como evidencia;
2. actualizar alternativas, costo y ADR;
3. ratificar o ajustar en comité;
4. modificar la fitness function en el mismo cambio que registra la decisión;
5. añadir una prueba que represente la nueva regla.
