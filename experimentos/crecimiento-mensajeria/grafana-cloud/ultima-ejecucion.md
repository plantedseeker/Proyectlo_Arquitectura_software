# Paneles Grafana Cloud — ejecución pendiente

La integración está preparada para el Stack ID `1889948` y para descubrir su
proyecto predeterminado, pero los enlaces se generan únicamente después de una
reejecución real. Desde la raíz del
repositorio, con Docker Desktop abierto y el árbol Git limpio:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-messaging-growth.ps1 -GrafanaCloud
```

El runner reemplazará este archivo con los enlaces directos a los 20 paneles y
conservará los resultados locales, logs, contexto y huellas de la reejecución.
