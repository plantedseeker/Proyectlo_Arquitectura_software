# Evidencia de la primera restricción arquitectónica en CI

## Decisión protegida

[`ADR-001`](../adr/ADR-001-limites-modulo-mensajeria.md): los controladores del
backend no acceden directamente a JDBC/SQL; delegan en servicios.

## Restricción ejecutable

`scripts/check_architecture.py` inspecciona los fuentes del paquete `controller` y
falla si encuentra referencias a:

- `org.springframework.jdbc`;
- `JdbcClient`;
- `java.sql`;
- `javax.sql`.

El workflow `UTrabajo CI` ejecuta las pruebas de backend en cada `push` a `main`
y en cada pull request a `main`.

## Prueba local esperada

```powershell
python .\scripts\check_architecture.py
```

## Violación deliberada — pendiente de ejecutar en GitHub

Procedimiento seguro:

1. crear una rama temporal desde el commit que contiene la prueba;
2. añadir a `ChatController.kt` una referencia prohibida a `JdbcClient`;
3. abrir un PR y conservar la URL de la corrida roja;
4. comprobar que el fallo menciona `ADR-001` y el archivo infractor;
5. retirar la violación en la misma rama y conservar la corrida verde;
6. cerrar el PR sin fusionar la violación.

### Registro real

- Rama temporal: `PENDIENTE`
- Commit con violación: `PENDIENTE`
- URL de CI roja: `PENDIENTE`
- Mensaje de fallo observado: `PENDIENTE`
- Commit que retira la violación: `PENDIENTE`
- URL de CI verde: `PENDIENTE`
- PR cerrado sin fusionar violación: `PENDIENTE`

No se inventarán enlaces ni resultados. Esta sección solo cambia después de
ejecutar la demostración en GitHub.
