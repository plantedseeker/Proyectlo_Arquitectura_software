# Evidencia de la primera restricción arquitectónica en CI

## Decisión protegida

[`ADR-002`](../adr/ADR-002-limites-modulos-dependencias.md): los controladores del
backend no acceden directamente a JDBC/SQL; delegan en servicios.

## Restricción ejecutable

La regla principal está implementada como pruebas ArchUnit en
[`DatabaseIsolationArchitectureTests.kt`](../../backend/src/test/kotlin/com/tab/utrabajo/api/architecture/DatabaseIsolationArchitectureTests.kt).
Importa el bytecode real del backend y exige simultáneamente:

- que los controladores no dependan de APIs JDBC/SQL;
- que las APIs de persistencia solo aparezcan en `service`, `auth` o `config`;
- que los paquetes superiores del backend no formen ciclos.

Como defensa complementaria temprana,
[`scripts/check_architecture.py`](../../scripts/check_architecture.py) inspecciona
los fuentes del paquete `controller` y falla si encuentra referencias a:

- `org.springframework.jdbc`;
- `JdbcClient`;
- `java.sql`;
- `javax.sql`.

El workflow `UTrabajo CI` ejecuta tanto ArchUnit como el comprobador ligero en
cada `push` a `main` y en cada pull request a `main`.

## Prueba local esperada

```powershell
.\backend\gradlew.bat -p backend test --tests "*DatabaseIsolationArchitectureTests"
python .\scripts\check_architecture.py
```

El primer comando requiere JDK 21. El script de Python no reemplaza ArchUnit:
sirve como señal rápida y mantiene un mensaje de error directamente asociado a
ADR-002.

## Alcance honesto

El backend actual no tiene todavía un paquete de repositorio por bounded
context. Por eso la regla protege la frontera que sí existe —HTTP frente a
persistencia— y no afirma aislar tablas por contexto. Si el código se divide en
repositorios o puertos, el ADR y la regla deberán evolucionar en el mismo PR.

## Demostración negativa controlada en GitHub — pendiente

Procedimiento seguro:

1. crear una rama temporal desde el commit que contiene la prueba;
2. añadir a `ChatController.kt` una referencia prohibida a `JdbcClient`;
3. abrir un PR y conservar la URL de la corrida roja;
4. comprobar que el fallo menciona `ADR-002` y el archivo infractor;
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

No se inventarán enlaces ni resultados. La prueba ArchUnit ya pasa en local y
queda integrada en CI; esta sección solo cambia después de ejecutar y conservar
la demostración roja/verde en GitHub.
