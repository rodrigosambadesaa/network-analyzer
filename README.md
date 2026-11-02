# Network Analyzer — Android

Análisis de conectividad, rendimiento y diagnóstico de red en una app Android independiente.

## Módulos incluidos
- **Diagnóstico de conectividad**: integra sin abreviar la clase `ConnectivityAndInternetAccess` sincronizada desde [el Gist de Rodrigo](https://gist.github.com/rodrigosambadesaa/729cca29a031fef4e2f15751863b655f), con estado Android, validación, VPN, portales cautivos y diagnósticos activos multiprotocolo.
- **Rendimiento**: descarga de 5 MB, subida de 1 MB y conexiones TCP repetidas contra Cloudflare `speed.cloudflare.com`, iniciadas voluntariamente y con aviso de privacidad.
- **Wi-Fi**: tipo de transporte, RSSI, frecuencia y velocidades de enlace/estimadas por Android (no confundir con prueba de Internet).
- **LAN**: escaneo voluntario limitado a una única red privada IPv4 /24; conexión TCP a puertos 80 y 443, no garantiza detectar todos los dispositivos. Nunca se explora Internet pública.
- **Historial**: hasta 100 mediciones guardadas localmente, visualización y exportación CSV.

## Privacidad y limitaciones
No recopila datos en un servidor propio, no tiene analítica ni anuncios. El historial se guarda únicamente en el dispositivo. La prueba de velocidad envía peticiones a Cloudflare; este proveedor ve la IP pública y puede procesar los metadatos conforme a su política. Antes de iniciar la prueba se muestra consentimiento. Pueden producirse costes de datos móviles. Las pruebas tienen límites estrictos de tamaño.

Una prueba corta de un flujo HTTP **no equivale** a la metodología multiparalela de los servicios profesionales de velocidad. El 'jitter' mostrado es variación absoluta entre tiempos de conexión TCP, **no jitter RTP ni IPDV**; los fallos TCP **no equivalen** a pérdida de paquetes. El descubrimiento LAN solo prueba puertos 80 y 443 y requiere permiso del propietario de la red.

Los límites y requisitos de Android 17 para `ACCESS_LOCAL_NETWORK` deberán adaptarse al pasar el `targetSdk` de 36 a 37. En este prototipo solo se solicitan `INTERNET`, `ACCESS_NETWORK_STATE` y `ACCESS_WIFI_STATE`, sin permisos de ubicación.

## Compilación
JDK 17, Android SDK 36, Gradle 8.13, Android Gradle Plugin 8.13.0:
```sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:bundleRelease
```
**No distribuir AAB sin configurar primero la firma de release de Google Play.**

## Licencia y atribución
Se conserva la atribución MIT de la biblioteca original de conectividad (Emil Davtyan, str4d y colaboradores). El uso de Cloudflare no implica afiliación. La biblioteca está copiada con sus referencias originales y sin eliminar funcionalidades.

### Escaneo LAN bajo Wi-Fi y VPN

Se obtiene la IPv4 de la conexión Wi-Fi mediante `ConnectivityManager.getLinkProperties()` y `LinkAddress` (no mediante `WifiInfo.getIpAddress()`, obsoleto), incluso si una VPN es la red predeterminada. Solo se permite escanear un bloque privado `/24` contenido íntegramente en la subred de la interfaz; las subredes `/25`–`/32` se rechazan expresamente para evitar exploraciones fuera de la subred. Las conexiones TCP se enlazan explícitamente a la red física Wi-Fi mediante `Network.getSocketFactory()`, evitando que la ruta VPN predeterminada desvíe las pruebas. No se escanean IP públicas, datos móviles ni direcciones de otras redes.
