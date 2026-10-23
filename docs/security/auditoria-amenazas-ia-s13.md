# Auditoría crítica de amenazas propuestas por IA — S13

## Regla

La IA puede ampliar la superficie de búsqueda, pero una amenaza solo se prioriza si existe en el sistema una **capacidad, activo, frontera o flujo** que permita sostenerla con evidencia.

Referencia: OWASP Top 10:2025, https://top10.owasp.org/2025/

## Aceptadas/priorizadas

- **Broken Access Control**: chats y documentos tienen datos privados; existe autorización explícita de chats y una ruta de uploads pública que requiere revisión.
- **Security Misconfiguration**: la configuración local contiene HTTP, datos demo y defaults deliberadamente locales.
- **Authentication Failures**: no se observó rate limiting de login y las sesiones duran por defecto 168 h.
- **Security Logging and Alerting Failures**: health/info no equivalen a auditoría ni alertas de seguridad.
- **Cryptographic Failures**: principalmente como condición de un futuro despliegue por Internet sin TLS.
- **Supply Chain**: dependencias, Actions e imágenes Docker forman parte del proceso de build/runtime.

## Aceptadas pero reducidas de prioridad

- **Injection**: existe impacto potencial, pero las consultas observadas usan parámetros y la columna dinámica está restringida por allowlist.
- **Exceptional Conditions**: existen handlers para casos conocidos; falta convertir 5xx repetidos en señal operativa.
- **Software/Data Integrity**: Git/Flyway/CI/hashes reducen el riesgo, aunque no sustituyen controles de supply chain.

## Rechazadas o no demostradas

### SSRF crítico

No se encontró una función donde el servidor acepte una URL arbitraria del usuario y realice una solicitud remota con ella. Se reabre si aparece importación por URL, webhook configurable, proxy o fetch server-side.

### XXE

El flujo inspeccionado usa JSON/multipart y no procesa XML.

### XSS server-side como riesgo principal

La API no renderiza plantillas HTML; el cliente principal es Android Compose. Se revisará si se introduce WebView/HTML o contenido enriquecido.

### “CSRF crítico porque Spring lo deshabilita”

La API es stateless y autentica mediante Bearer enviado explícitamente, no por cookie automática del navegador. Si el mecanismo de autenticación cambia, esta conclusión deja de aplicar.

### “Extraer microservicios para mejorar seguridad”

No existe evidencia de que separar procesos elimine T01–T10; sí agregaría secretos, red, autorización servicio-a-servicio y más operación.

## Cambios que obligan a repetir el threat model

- despliegue público por Internet;
- autenticación basada en cookies;
- WebView o contenido HTML;
- procesamiento XML;
- servidor haciendo fetch de URLs de usuario;
- broker/eventos o microservicios;
- almacenamiento de objetos público;
- nuevos tipos de documento;
- datos reales en vez de semilla sintética.
