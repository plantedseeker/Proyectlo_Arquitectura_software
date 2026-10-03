# Modelo de dominio S9 — subdominios y bounded contexts

> Estado: **propuesta sustentada en el código As-Is**. Este documento no declara microservicios ni bases separadas. Los contextos son fronteras lógicas de responsabilidad dentro del monolito modular actual.

## Propósito

El Módulo 5 pide identificar fronteras del dominio y justificar qué responsabilidad protege cada una. Por eso este mapa no se deriva mecánicamente de las tablas: contrasta controladores, servicios, esquema relacional y flujos ya verificados en S5–S8.

## Subdominios identificados

| Subdominio / contexto | Tipo propuesto | Responsabilidad que protege | Conceptos que posee | Lo que no le pertenece |
| --- | --- | --- | --- | --- |
| **Identidad y Acceso** | Genérico | Autenticar usuarios, resolver rol/principal y administrar sesiones | usuario autenticado, rol, sesión, credenciales | ofertas, postulaciones, mensajes |
| **Perfiles** | Soporte | Mantener información académica/laboral y documentos de estudiantes/empresas | perfil, habilidades, CV, avatar, datos de empresa | autenticación, disponibilidad de ofertas, historial de chat |
| **Ofertas** | Núcleo | Gestionar publicación y vigencia de oportunidades laborales | oferta, requisitos, empresa propietaria, estado activo | postulación del estudiante, conversación |
| **Postulaciones** | Núcleo | Gestionar la relación estudiante–oferta y su estado | postulación, fecha, estado | contenido editable de la oferta, mensajes |
| **Mensajería** | Soporte crítico para la categoría del curso | Gestionar conversaciones entre estudiante y empresa asociadas a una oferta | chat, mensaje, participante, orden e historial | reglas de publicación, perfil completo, autenticación |

La clasificación núcleo/soporte/genérico es una propuesta de modelado, no una afirmación de que existan cinco despliegues independientes.

## Evidencia As-Is

### Identidad y Acceso

- AuthController expone registro, login, sesión actual y logout.
- AuthService resuelve usuarios y sesiones.
- SecurityConfig exige autenticación para las rutas no públicas.

### Perfiles

- ProfileController gestiona perfil, información laboral, habilidades y archivos.
- FileStorageService aísla la persistencia local de documentos.
- Las tablas principales son company_profile y student_skill, con datos comunes en app_user.

### Ofertas

- JobController gestiona lectura, creación, actualización y eliminación.
- UTrabajoService.createJob/updateJob/deleteJob concentra reglas de rol y persistencia.
- Las tablas principales son job_offer y job_requirement.

### Postulaciones

- ApplicationController expone listar, aplicar y cancelar.
- UTrabajoService.apply consulta actualmente job_offer.active antes de insertar application.
- Esto revela una dependencia real de **Postulaciones → Ofertas**.

### Mensajería

- ChatController expone chats, creación/obtención, lectura de mensajes y envío.
- UTrabajoService.createOrGetChat consulta actualmente job_offer.company_id.
- UTrabajoService.chats consulta título de la oferta y nombres de participantes.
- requireChatParticipant protege lectura y escritura.
- Esto revela dependencias reales de **Mensajería → Ofertas** y **Mensajería → Identidad**.

## Frontera elegida para el Módulo 5

Para conservar continuidad con la categoría **Mensajería y mesa de ayuda**, el Spike 1 estudiará la frontera:

~~~text
Mensajería → Ofertas
~~~

Pregunta arquitectónica:

> ¿Cómo debe obtener Mensajería la información mínima de una oferta necesaria para crear una conversación: mediante un contrato síncrono explícito o mediante información replicada por eventos?

El sistema actual resuelve esa necesidad con acceso JDBC directo a la tabla job_offer desde el servicio compartido. El Módulo 5 hará explícito el contrato y comparará mecanismos de integración.

## Qué no se afirma

- No se afirma que cada contexto sea un microservicio.
- No se afirma que deba existir una base de datos por contexto.
- No se afirma que eventos asíncronos sean superiores por defecto.
- No se renombra cada tabla como bounded context.
- La decisión final de integración queda condicionada al preregistro y resultado del Spike 1.
