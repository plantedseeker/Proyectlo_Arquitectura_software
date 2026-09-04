# Plan de contribuciones y PR — semanas 5 a 8

Objetivo: producir contribuciones reales y defendibles, no repartir autoría de
forma artificial. Cada integrante debe revisar, entender y aprobar su cambio
antes de autorizar el commit desde su propia cuenta.

## Orden recomendado

### PR 1 — C4 y localización de la línea base

- Rama sugerida: `docs/c4-s5-s6`
- Responsable propuesto: Camilo Andrés Romero Palencia (`plantedseeker`).
- Archivos principales: `dossier/05-c4-contexto.md` a
  `09-c4-trazabilidad-localizacion.md`, defensa y revisión par.
- Validación personal: recorrer las cajas a código, verificar procedencia del
  ZIP y defender qué entró y qué quedó fuera de S4.

### PR 2 — Experimento de causa y ADR de paginación

- Rama sugerida: `experiment/s7-localizacion-adr2`
- Responsable propuesto: Santiago Jaramillo Sánchez.
- Cuenta GitHub: `sanwolk`.
- Archivos principales: `experimentos/localizacion-s7/`, comparación de
  alternativas y `ADR-002`.
- Validación personal: abrir Docker, ejecutar el instrumento, revisar el JSON y
  completar la interpretación sin confundirla con datos.

### PR 3 — Límites, ADR-001 y protección CI

- Rama sugerida: `architecture/s8-adr1-ci`
- Responsable propuesto: Juan Carlos Barragán Arévalo.
- Cuenta GitHub: `juan`.
- Archivos principales: `ADR-001`, diseño modular, crítica IA, mini-comité,
  `scripts/check_architecture.py` y workflow CI.
- Validación personal: ejecutar la regla, abrir el PR de violación deliberada,
  conservar corrida roja/verde y explicar costos de las alternativas.

## Regla para usar las cuentas

El dueño de cada cuenta debe estar presente, revisar su diff y autorizar
personalmente el inicio de sesión/código de dispositivo. No se cambian
`user.name` o `user.email` para atribuir trabajo a alguien ausente y no se usan
tokens compartidos. La autoría indica quién comprendió y asumió esa
contribución.

## Criterio de cierre por PR

1. diff revisado por el responsable;
2. comando de verificación ejecutado;
3. CI en verde, salvo el PR temporal cuya finalidad explícita sea probar rojo;
4. al menos otra persona revisa el PR;
5. enlace registrado en `dossier/10-checklist-semanas-5-8.md`;
6. merge sin reescribir la historia de otra persona.

## Datos que faltan antes de crear los PR

- resultado real de MSG-LOC-01;
- decisión del equipo sobre los dos ADR;
- revisión de otro equipo y mini-comité cuando ocurran.
