# Spike 1 — integración Mensajería → Ofertas

Experimento acotado de Semana 10 para evaluar un contrato REST síncrono entre los contextos lógicos Ofertas y Mensajería.

## Cadena temporal

1. `00-preregistro.md`: hipótesis y condiciones originales, commit `804d2d2`.
2. `01-enmienda-previa-a-medicion.md`: reducción justificada del payload antes de compilar y medir.
3. implementación experimental y ejecutor reproducible;
4. resultados fechados bajo `resultados/`;
5. veredicto y ADR-003 posteriores a la medición.

## Contrato medido

~~~http
GET /internal/integration/v1/jobs/{jobId}/conversation-context
X-Integration-Token: <credencial interna>
~~~

Respuesta mínima:

~~~json
{
  "jobId": "uuid",
  "companyId": "uuid"
}
~~~

La existencia queda expresada mediante `200` o `404`. El endpoint es experimental: no reemplaza el acceso actual usado por `createOrGetChat`.

## Ejecución

Requisitos:

- Docker Desktop con Linux Engine;
- PowerShell;
- Git y Python;
- árbol Git limpio para una medición válida.

Desde la raíz:

~~~powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-spike-01-integracion.ps1
~~~

El ejecutor:

- levanta PostgreSQL 16 y reconstruye la API;
- selecciona una oferta de los datos demo deterministas;
- registra commit, rama, máquina, energía y versiones;
- ejecuta k6 0.54.0 cuatro veces con `shared-iterations`, 10 VU y 40 solicitudes medidas;
- descarta la corrida 1 como calentamiento;
- evalúa individualmente las corridas 2, 3 y 4;
- conserva resultados aunque un threshold refute la hipótesis;
- no escribe el valor del token en la evidencia;
- genera hashes SHA-256.

Los casos funcionales se ejecutan en cada corrida antes de las 40 solicitudes medidas:

- contrato exacto con dos campos;
- token ausente → `401`;
- token inválido → `401`;
- oferta inexistente → `404`.

Para depuración puede usarse `-AllowDirty`, pero el agregador marcará esa ejecución como inválida y no debe emplearse en el ADR.

## Resultados

Cada ejecución crea `resultados/<fecha-UTC>/` con:

- `contexto.json`;
- `run-1.json` a `run-4.json`;
- métricas crudas `run-N.ndjson`;
- salida `run-N.log`;
- `resultado.json`;
- `SHA256SUMS.txt`.

El veredicto de alcance será **ajustada** por la reducción previa del payload. `resultado.json` informa además, sin ocultar corridas, si la hipótesis funcional y de rendimiento quedó respaldada o refutada.
