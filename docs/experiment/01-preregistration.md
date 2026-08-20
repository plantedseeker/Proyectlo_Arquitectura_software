# Prerregistro del experimento de rendimiento

Estado: **propuesta lista para sellar; aún no medida**  
Fecha de propuesta: 2026-08-20  
Commit de sellado: **pendiente**

No agregar resultados a este documento. Cualquier cambio posterior de hipótesis o umbral debe crear una nueva versión y explicar el motivo.

## Sistema y fenómeno

UTrabajo es una aplicación móvil. Su cliente Android solicita ofertas a una API REST local; la API consulta PostgreSQL 16 mediante un índice sobre estado y fecha, y limita cada página a 100 filas. El experimento estudia la latencia de esa ruta con un catálogo grande y mayoritariamente activo.

## Hipótesis previa

**H1:** con 10.000 ofertas sintéticas, 90 % activas, descripciones de aproximadamente 1 KiB, páginas de 100 ofertas y 10 solicitudes concurrentes, el percentil 95 de `GET /api/jobs?limit=100&offset=0` será **menor o igual a 500 ms** en ejecución local.

**H0:** bajo las mismas condiciones, alguna de las tres corridas válidas tendrá `p95 > 500 ms`.

Regla de decisión: H1 se considera apoyada solo si las corridas 2, 3 y 4 cumplen el umbral. La primera corrida es calentamiento y se descarta.

## Escenario y métrica

- Fuente: clientes Android que abren la lista de empleos.
- Estímulo: 40 solicitudes por corrida, concurrencia 10.
- Entorno: API y PostgreSQL 16 locales con Docker Compose.
- Respuesta válida: HTTP 200, JSON válido y exactamente 100 ofertas.
- Latencia: reloj monotónico desde antes del envío hasta haber leído y decodificado todo el JSON.
- Métrica principal: percentil 95 por rango más próximo.
- Umbral: `p95 <= 500 ms` en cada corrida válida y cero respuestas inválidas.
- Corridas: cuatro; la primera se marca y descarta.

## Semilla sesgada

- 10.000 ofertas deterministas e idempotentes.
- 90 % activas y 10 % inactivas.
- Cuatro ubicaciones distribuidas uniformemente.
- Descripción cercana a 1 KiB.
- Identificadores derivados de MD5 únicamente para reproducibilidad de datos sintéticos, no para seguridad.
- Sin información personal ni datos de producción.

El sesgo obliga a PostgreSQL a seleccionar entre 9.000 filas activas, pero la paginación evita que la red y el teléfono reciban toda la colección.

## Presión externa que obliga a redecidir

Se reabre la arquitectura si ocurre al menos una condición:

1. el producto exige `p95 <= 250 ms`;
2. el catálogo proyectado supera 50.000 ofertas;
3. se requiere desplegar por Internet con varios nodos de API; o
4. alguna corrida válida incumple 500 ms o produce una respuesta inválida.

Las alternativas a estudiar serían paginación por cursor, caché, índices parciales, pool de conexiones y separación de almacenamiento de archivos. La línea base original se conserva.

## Amenazas a la validez

- La ejecución local no representa latencia móvil ni infraestructura en Internet.
- El cliente del instrumento mide la API completa, pero no el renderizado de Compose.
- Docker, antivirus, energía y procesos de la máquina pueden alterar resultados.
- Se mitiga registrando versión del código, plataforma, CPU lógica, concurrencia, volumen y las cuatro corridas.

## Condición para medir

Crear primero un commit que contenga este prerregistro y copiar su hash en “Commit de sellado”. Después ejecutar el protocolo. No publicar como línea base una corrida hecha antes del sello.
