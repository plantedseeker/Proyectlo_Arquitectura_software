# Responsabilidades y fronteras de contexto — UTrabajo

## Criterio

Una frontera existe cuando protege lenguaje, reglas y decisiones que no deberían ser modificadas arbitrariamente por el contexto vecino. En el monolito actual varias fronteras están implementadas dentro de UTrabajoService; S9 las hace explícitas sin inventar separación física.

## Identidad y Acceso

**Protege:** quién es el actor y qué rol tiene.

**Entradas principales:** credenciales, token de sesión.

**Salidas útiles a otros contextos:** userId, role, identidad autenticada.

**No debe decidir:** si una oferta está activa, si existe una postulación o el contenido de un chat.

## Perfiles

**Protege:** información descriptiva y documental de estudiantes y empresas.

**Entradas:** cambios de perfil, habilidades, CV, documentos empresariales.

**Salidas:** datos de perfil y rutas de archivos autorizadas.

**No debe decidir:** creación de ofertas, postulación, pertenencia a chats.

## Ofertas

**Protege:** ciclo de vida de una oportunidad laboral.

**Entradas:** publicar, actualizar o retirar una oferta por la empresa propietaria.

**Salidas relevantes para integración:** identificador de oferta, empresa propietaria, estado de disponibilidad y título.

**No debe decidir:** si un estudiante ya se postuló ni el contenido de una conversación.

## Postulaciones

**Protege:** relación entre estudiante y oferta.

**Entradas:** solicitud de postulación o cancelación.

**Necesita de Ofertas:** saber si la oferta existe y está activa; mostrar datos mínimos de la oferta.

**No debe modificar:** la oferta ni el perfil del estudiante.

## Mensajería

**Protege:** conversación, autorización por participante y secuencia de mensajes.

**Entradas:** crear/obtener chat, leer historial, enviar mensaje.

**Necesita de Ofertas:** empresa propietaria y referencia/título de la oferta asociada.

**Necesita de Identidad:** usuario autenticado y rol.

**No debe modificar:** datos de la oferta, requisitos, CV o credenciales.

## Frontera crítica seleccionada: Mensajería → Ofertas

### Estado As-Is

UTrabajoService.createOrGetChat realiza:

~~~sql
SELECT company_id FROM job_offer WHERE id = :id
~~~

y posteriormente crea o recupera el chat.

UTrabajoService.chats también hace JOIN job_offer para mostrar el título.

La relación existe y funciona, pero está expresada como dependencia de esquema compartido, no como contrato de contexto.

### Información mínima que Mensajería necesita

Para **crear una conversación**:

- jobId;
- companyId;
- existencia de la oferta.

Para **listar conversaciones** también usa actualmente jobTitle.

No necesita modificar salario, requisitos, ubicación ni estado de publicación.

### Riesgo arquitectónico

Si Mensajería comienza a conocer cada vez más columnas de job_offer, la frontera lógica se degrada aunque el sistema siga siendo un monolito. El contrato de integración de S9 busca reducir ese conocimiento a información mínima y explícita.

## Regla de defensa

Ante cualquier flecha del Context Map debe poder responderse:

1. quién consume;
2. quién provee;
3. qué información cruza;
4. en qué dirección;
5. por qué esa información no pertenece al consumidor;
6. qué mecanismo de integración se está evaluando.
