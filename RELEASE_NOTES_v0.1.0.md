# Network Analyzer v0.1.0 — versión de pruebas

Primera versión Android del analizador de redes. **Versión preliminar; no apta todavía para Google Play.**

## Funciones
- Integra completa la utilidad **ConnectivityAndInternetAccess** (basada en el Gist del autor), sin abreviar sus comprobaciones pasivas y activas de conectividad.
- Estado de Wi-Fi, red móvil, Ethernet, VPN, validación de Internet y portales cautivos.
- Pruebas indicativas de descarga (hasta 5 MB) y subida (hasta 1 MB) y tiempos de establecimiento TCP con Cloudflare, bajo consentimiento explícito.
- Información de señal Wi-Fi y velocidades estimadas del enlace, diferenciadas de la velocidad real.
- Escaneo voluntario limitado a IP privadas de la Wi-Fi local y puertos TCP 80/443; la conexión se asocia a la red Wi-Fi física incluso cuando hay una VPN predeterminada.
- Historial local de mediciones y exportación CSV.

## Instalación, privacidad y limitaciones
- APK **debug** firmado para pruebas, no con una clave comercial estable; no se garantiza su actualización sobre versiones firmadas con otra clave.
- Android 8 (API 26) o posterior.
- Los tests de velocidad comunican la IP y métricas al servidor de Cloudflare y pueden consumir datos móviles; la aplicación solicita consentimiento antes de iniciarlos.
- Las mediciones HTTP cortas **no son comparables directamente** con un benchmark de velocidad profesional; los fallos TCP no representan necesariamente pérdida de paquetes.
- El descubrimiento LAN mediante puertos 80 y 443 no detecta todos los dispositivos.
- No se adjunta AAB de producción, pendiente de firma duradera y pruebas instrumentadas.
