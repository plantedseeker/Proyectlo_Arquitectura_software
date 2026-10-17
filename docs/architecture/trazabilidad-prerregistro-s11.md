# Trazabilidad y desviación formal del prerregistro S11

## Evidencia que no se modifica

El protocolo del experimento se confirmó antes de medir en el commit
[`73895be`](../../experimentos/crecimiento-mensajeria/00-preregistro.md). El
archivo original permanece bajo `/experimentos` y fijó:

- hipótesis falsable;
- escalones 10, 50, 100, 150 y 200 VU;
- cuatro corridas por escalón y descarte de la primera;
- umbral p95 de 500 ms, cero fallos y páginas válidas;
- criterios de degradación, invalidación y atribución;
- separación entre límite del sistema, generador y equipo compartido.

El commit de resultados `8bc6059` es posterior. Esta secuencia demuestra que
las condiciones y criterios no se acomodaron después de observar los datos.

## Desviación frente al material oficial recibido después

La presentación detallada de Módulo 6 pide registrar en `/docs`, antes de medir,
una apuesta por **el componente que fallará primero y el escalón exacto**. El
prerregistro existente quedó en `/experimentos` y formuló otra predicción:
ausencia de degradación sostenida hasta 200 VU, acompañada de una lista de
fallos candidatos sin priorizar un componente ni un escalón de quiebre.

Esta diferencia se registra como desviación formal. No se mueve ni reescribe el
prerregistro original porque hacerlo después de medir destruiría la
temporalidad que se quiere demostrar.

## Efecto sobre la interpretación

- El experimento sigue siendo válido para contrastar su hipótesis original.
- No se afirma que se acertó una predicción de componente o escalón que nunca
  fue formulada.
- El resultado válido permanece: no apareció degradación sostenida hasta
  200 VU y la atribución de recursos fue inconclusa por la brevedad de las
  ráfagas.
- Una repetición futura deberá registrar en `/docs` la apuesta exacta exigida y
  conservarla en un commit anterior a cualquier resultado nuevo.

## Regla para la siguiente medición

```text
/docs/apuesta-crecimiento-<id>.md → commit → ejecución → resultados → análisis
```

La apuesta deberá nombrar un único componente candidato, un escalón exacto y
las señales que permitirían refutarla. Si el equipo no puede justificar esa
precisión, deberá declararlo antes de medir, no después.
