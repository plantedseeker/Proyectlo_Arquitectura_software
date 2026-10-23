# Evidencia de fitness function en CI — S14

## Objetivo

Demostrar de forma controlada que la fitness function de ADR-004 no es solo documentación: una violación real debe romper GitHub Actions antes de poder fusionarse.

## Regla bajo prueba

Instrumento:

~~~text
scripts/check_distribution_decision.py
~~~

Regla relevante:

~~~text
MESSAGE_CACHE_ENABLED debe permanecer false por defecto
~~~

Umbral:

~~~text
0 violaciones
~~~

## Violación deliberada

Rama:

~~~text
architecture/s14-despliegue-observabilidad
~~~

PR:

~~~text
#27 — architecture(s14): despliegue, observabilidad, ADR-005 y fitness CI
~~~

Commit de violación:

~~~text
52382ccb4f8137f2f7238f33a7f5d9a5512f7bb0
~~~

Cambio intencional y temporal:

~~~yaml
MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-true}
~~~

en lugar del valor permitido:

~~~yaml
MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-false}
~~~

No se introdujo como mejora funcional. Su único propósito fue comprobar la restricción.

## Resultado rojo

GitHub Actions:

- workflow: **UTrabajo CI**
- run: **#60**
- run id: **40039705894**
- URL: https://github.com/plantedseeker/Proyectlo_Arquitectura_software/actions/runs/40039705894
- job: **backend**
- step: **Enforce ADR-004 distribution decision**
- resultado del step: **failure**
- pasos posteriores de backend: skipped por la violación.

El log del job registró literalmente:

~~~text
ADR-004 violado: la decisión de distribución requiere revisión explícita.
Decisión protegida: docs/adr/ADR-004-distribucion-mensajeria.md
- Compose debe mantener MESSAGE_CACHE_ENABLED=false por defecto
Process completed with exit code 1.
~~~

El job Android no es la evidencia causal: la prueba relevante es que backend se detuvo exactamente en la fitness function.

## Interpretación

~~~text
decisión ADR-004
      ↓
regla executable
      ↓
cache default = true
      ↓
violación detectada
      ↓
CI ROJO
~~~

Esto demuestra que un cambio cotidiano no puede habilitar silenciosamente la táctica experimental que ADR-004 mantiene apagada.

## Corrección aplicada antes de merge

Commit de corrección:

~~~text
a2cdf943f206a186785af0f6110cad42c4d917f1
~~~

Se restauró:

~~~yaml
MESSAGE_CACHE_ENABLED: ${MESSAGE_CACHE_ENABLED:-false}
~~~

Después de la corrección, el run #62 mostró el step **Enforce ADR-004 distribution decision = success** y el job backend completo = **success**. El estado final del PR debe conservar CI completo en verde antes del merge.

La violación roja queda preservada en el historial del PR/run, pero **no llega a main**.

## Estado

- [x] violación introducida deliberadamente;
- [x] CI la detectó en la fitness function correcta;
- [x] corrección aplicada;
- [x] fitness function y job backend verdes después de corregir (run #62);
- [ ] workflow completo verde y PR fusionado sin la violación — se verifica en el cierre del PR #27.
