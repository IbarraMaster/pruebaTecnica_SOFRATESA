# Manual de instalación

Guía para levantar y probar la solución completa desde una máquina limpia
(probado en Windows con Git Bash; los comandos de Docker/curl son iguales
en Linux/Mac).

## 1. Programas requeridos

| Programa | Versión / notas |
|---|---|
| Docker Desktop (o Docker Engine + Compose plugin) | Docker 24+, `docker compose` v2 (comando con espacio, no `docker-compose`) |
| Git | cualquier versión reciente |
| Android Studio | reciente, con Android SDK Platform 37 instalado y al menos un emulador (AVD) creado. Trae su propio JDK embebido (JBR), no hace falta instalar Java aparte. |
| Git Bash (en Windows) | para correr `scripts/generar-certificados.sh` (usa `openssl`, incluido en Git Bash) |
| Conexión a internet | solo la primera vez: para descargar imágenes Docker, dependencias npm, dependencias Gradle y el JDK 17 que Gradle aprovisiona automáticamente |

No hace falta instalar el Android SDK/JDK "a mano": Android Studio y Gradle
lo resuelven solos la primera vez que abres el proyecto.

## 2. Clonar el repositorio

```bash
git clone https://github.com/IbarraMaster/pruebaTecnica_SOFRATESA.git
cd pruebaTecnica_SOFRATESA
```

## 3. Configurar variables de entorno

```bash
cp .env.example .env
```

Edita `.env` y define valores propios (no dejar los de ejemplo en un
entorno real; para esta prueba local los de ejemplo funcionan igual):

```
POSTGRES_USER=sofratesa
POSTGRES_PASSWORD=<elige una contraseña>
POSTGRES_DB=sofratesa
AUTH_DATABASE_URL=postgresql://sofratesa:<misma contraseña>@postgres:5432/sofratesa?schema=public
RECORDS_DATABASE_URL=postgresql://sofratesa:<misma contraseña>@postgres:5432/sofratesa?schema=records
JWT_SECRET=<cadena aleatoria larga>
JWT_EXPIRES_IN=60m
```

## 4. Generar los certificados TLS locales

```bash
bash scripts/generar-certificados.sh
```

Esto crea `proxy/certs/sofratesa.{crt,key}` (autofirmado, válido para
`sofratesa.local`, `localhost`, `127.0.0.1` y `10.0.2.2`) **y** copia el
`.crt` a `android/app/src/debug/res/raw/sofratesa_ca.pem`, que es lo que
hace que la app Android (solo en la variante debug) confíe en este proxy.
Ninguno de los dos se versiona en git.

> Si vas a probar en un **dispositivo físico** en vez de un emulador, antes
> de este paso edita la línea `-addext "subjectAltName=..."` en
> `scripts/generar-certificados.sh` y agrega `,IP:<IP-LAN-de-tu-PC>` (la IP
> de tu computadora en la red Wi-Fi/LAN donde también estará el celular).
> Luego corre el script.

## 5. Construir e iniciar los contenedores

```bash
docker compose up -d --build
```

Levanta 4 contenedores: `postgres`, `auth-service`, `records-service` y
`proxy` (Caddy, único con el puerto `443` publicado al host). El esquema de
base de datos y el usuario de prueba se crean solos (migraciones Prisma +
seed) al arrancar `auth-service`.

## 6. Verificar que los servicios responden

```bash
curl -k https://localhost/health-auth
curl -k https://localhost/health-registros
```

Ambos deben responder `{"status":"ok"}`. El `-k` es porque el certificado
es autofirmado (esto es solo para probar por terminal; la app Android no
necesita `-k`, confía en la CA real vía `network_security_config.xml`).

Credenciales del usuario de prueba (creado automáticamente):

- **Usuario:** `tecnico1`
- **Contraseña:** `Tecnico123!`

```bash
curl -k -X POST https://localhost/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"tecnico1","password":"Tecnico123!"}'
```

Debe devolver un `token` y `expira_en`.

La documentación OpenAPI de cada servicio está en:
`https://localhost/openapi-auth.yaml` y `https://localhost/openapi-registros.yaml`.

## 7. Compilar la aplicación Android

1. Abre Android Studio → **Open** → selecciona la carpeta `android/` del
   repo (no la raíz del repo).
2. Espera el **Gradle Sync** (la primera vez puede tardar varios minutos:
   descarga el JDK 17, el SDK y las dependencias).
3. **URL del backend** — ya viene configurada por defecto para emulador:
   - Por defecto: `https://10.0.2.2/` (alias que el emulador usa para
     llegar al `localhost` de tu PC). No requiere cambios para probar en
     emulador.
   - Para **dispositivo físico**: agrega esta línea a `android/gradle.properties`:
     ```
     BACKEND_BASE_URL=https://<IP-LAN-de-tu-PC>/
     ```
     (la misma IP que agregaste al certificado en el paso 4). El celular y
     la PC deben estar en la misma red Wi-Fi, y el firewall de Windows debe
     permitir conexiones entrantes al puerto 443.
4. **CA local**: no requiere instalación manual. El certificado ya quedó
   embebido como recurso (`app/src/debug/res/raw/sofratesa_ca.pem`, ver
   paso 4) y `network_security_config.xml` lo declara como *trust anchor*
   — pero **solo en builds debug** (la variante que se usa en esta prueba).

## 8. Instalar el APK

Con un emulador corriendo (o un dispositivo físico con "Depuración USB"
activada y conectado):

- Desde Android Studio: botón **Run ▶** con el módulo `app` seleccionado.
- O por línea de comandos, desde `android/`:
  ```bash
  ./gradlew.bat assembleDebug
  ```
  El APK queda en `android/app/build/outputs/apk/debug/app-debug.apk`;
  instálalo con `adb install app-debug.apk`.

## 9. Recorrido de prueba completo

1. Abre la app con el dispositivo/emulador **con conectividad**. Debe
   mostrar "Conectado".
2. Inicia sesión con `tecnico1` / `Tecnico123!`.
3. Activa **modo avión** en el dispositivo/emulador.
4. Llena el formulario (código de activo, tipo de actividad, observación) y
   guarda. Debe aparecer en la lista con estado **PENDIENTE**.
5. Desactiva el modo avión. En pocos segundos el registro debe pasar solo a
   **SINCRONIZADO** (sincronización automática al recuperar conectividad),
   y arriba se muestra un resumen ("N enviado(s), M fallido(s)").
6. Verifica en PostgreSQL que el registro quedó guardado:
   ```bash
   docker compose exec postgres psql -U sofratesa -d sofratesa \
     -c "SELECT id_registro, codigo_activo, capturado_en, recibido_en FROM records.registros ORDER BY recibido_en DESC LIMIT 5;"
   ```
   `capturado_en` (momento offline) debe ser anterior a `recibido_en`
   (momento en que llegó al servidor).
7. Prueba también **"Sincronizar ahora"** (botón manual) y **"Cerrar
   sesión"** (debe volver a pedir login).

## 10. Detener, reiniciar y limpiar

```bash
docker compose down          # detiene y elimina los contenedores (conserva el volumen de datos)
docker compose down -v       # además borra el volumen: siguiente arranque parte de cero
docker compose up -d         # vuelve a levantar todo
```

## 11. Problemas frecuentes

**Login falla con "Sin conexión con el servidor" y en el logcat aparece
`Trust anchor for certification path not found`.**
El proxy (Caddy) carga el certificado en memoria al iniciar y **no lo
relee solo porque el archivo cambió en disco**. Si regeneraste el
certificado (paso 4) con el proxy ya corriendo, reinícialo:
```bash
docker compose restart proxy
```

**Falla `docker compose up --build` en `auth-service`/`records-service` con
un error de Prisma sobre migraciones.**
Asegúrate de que `.env` tenga `AUTH_DATABASE_URL` y `RECORDS_DATABASE_URL`
con la **misma** contraseña que `POSTGRES_PASSWORD`.

**Error de Gradle/KSP tipo `unexpected jvm signature V` al compilar la
app.** Ya está resuelto en el repo (Room 2.8.4 + toolchain fijado a JDK 17
en `android/gradle/gradle-daemon-jvm.properties`); si aparece de nuevo,
verifica que Android Studio esté usando el JDK indicado por ese archivo
(File → Settings → Build Tools → Gradle → Gradle JDK → debería decir algo
como "from gradle-daemon-jvm.properties" o similar) y no un JDK 21/25
manual.

**La app dice "Sin conexión" aunque el emulador tenga red.**
Verifica que no esté en modo avión ni con el Wi-Fi del emulador apagado.
La detección usa `NET_CAPABILITY_INTERNET` (hay interfaz de red), no
requiere que Android valide acceso real a internet — así que no depende de
que la PC host tenga internet, solo de que la interfaz de red del
emulador/dispositivo esté activa.

**Dispositivo físico no llega al backend.**
Confirma misma red Wi-Fi que la PC, que el certificado se haya regenerado
incluyendo la IP LAN de la PC (paso 4), que `BACKEND_BASE_URL` en
`android/gradle.properties` use esa misma IP, y que el firewall de Windows
permita conexiones entrantes al puerto 443 (puede pedir autorizar Docker
Desktop/Caddy la primera vez).
