# 01 — Contexto del sistema

Estado: **sistema base adoptado y ejecutado localmente**.  
Categoría confirmada por el profesor: **Mensajería y mesa de ayuda**.  
Fuente de confirmación: comunicación del profesor reportada por el equipo el
2026-08-20; la captura o mensaje original debe anexarse a la entrega.

## Sistema base adoptado

UTrabajo es un proyecto Android existente del equipo, no un proyecto creado en
blanco para el curso. Conecta estudiantes y empresas alrededor de ofertas,
postulaciones y conversaciones. La modalidad material es **sistema propio
existente**; queda por transcribir la letra A/B/C usada por la guía del curso,
porque el checklist suministrado no incluye la definición de esas letras.

## Actores y flujos

| Actor | Objetivo | Flujo relevante |
| --- | --- | --- |
| Estudiante | Encontrar oportunidades y comunicarse | Inicia sesión, se postula, abre un chat y envía mensajes |
| Empresa | Publicar ofertas y atender candidatos | Publica, revisa postulaciones y responde conversaciones |
| Equipo de desarrollo | Mantener y medir el sistema | Ejecuta pruebas, migraciones y experimentos reproducibles |
| Profesor/auditor | Evaluar decisiones y evidencia | Revisa PR, dossier, condiciones, datos y resultados |

## Contexto y límites

El sistema medido se ejecuta localmente:

```text
Android (Kotlin + Compose)
        │ HTTP/JSON + Bearer
        ▼
API REST (Spring Boot + Kotlin)
        │ JDBC + Flyway
        ▼
PostgreSQL 16
```

Para el escenario de mensajería, k6, la API y PostgreSQL comparten un único
equipo físico mediante Docker Desktop. La medición cubre el trayecto API + base;
no incluye red móvil ni renderizado de Compose.

Fuera del alcance de esta entrega: despliegue público, alta disponibilidad,
notificaciones push, WebSocket y almacenamiento administrado.

## Línea base operativa

Arranque:

```bash
docker compose up --build -d
curl http://localhost:8080/actuator/health
```

Verificación:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
./backend/gradlew -p backend test
```

El workflow `UTrabajo CI` ejecuta Android y la integración del backend contra
PostgreSQL 16. El checkpoint operativo anterior quedó en verde en la corrida
32345652664. Las instrucciones completas están en el `README.md` raíz.

## Restricciones de contexto

- Presupuesto: herramientas y servicios gratuitos para esta entrega.
- Infraestructura: un computador con Docker Desktop; sin nube obligatoria.
- Tiempo: entregas semanales y fecha fija en semana 4.
- Tecnología: Android existente, API Kotlin/JDK 21 y PostgreSQL 16.
- Datos: únicamente semilla sintética; no se usan conversaciones reales.
- Seguridad: Android nunca recibe credenciales de PostgreSQL y la API valida que
  el usuario sea participante del chat.
