# Guía de Configuración para Google Play Store

## Paso 1: Generar la Keystore (Llave de Firma)

Ejecuta este comando en tu terminal local (NO en GitHub):

```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias uber-speed
```

**Información que te pedirá:**
- Contraseña del keystore (guárdala en un lugar seguro)
- Nombre y apellido
- Nombre de la unidad organizativa
- Nombre de la organización
- Nombre de la ciudad o localidad
- Nombre del estado o provincia
- Código de país de dos letras
- Contraseña del alias (puede ser la misma que la del keystore)

⚠️ **CRÍTICO:** Guarda el archivo `release-key.jks` y las contraseñas en un lugar MUY SEGURO. Si los pierdes, NO podrás actualizar tu app en Play Store.

## Paso 2: Configurar GitHub Secrets

1. Ve a tu repositorio en GitHub
2. Clic en **Settings** > **Secrets and variables** > **Actions**
3. Clic en **New repository secret** para cada uno de los siguientes:

### a) KEYSTORE_BASE64

Convierte tu keystore a base64:

```bash
base64 -i release-key.jks | pbcopy  # En Mac
# O
base64 -w 0 release-key.jks  # En Linux, luego copia manualmente
```

- **Name:** `KEYSTORE_BASE64`
- **Value:** [El texto base64 generado]

### b) KEYSTORE_PASSWORD

- **Name:** `KEYSTORE_PASSWORD`
- **Value:** [La contraseña que usaste para el keystore]

### c) KEY_ALIAS

- **Name:** `KEY_ALIAS`
- **Value:** `uber-speed`

### d) KEY_PASSWORD

- **Name:** `KEY_PASSWORD`
- **Value:** [La contraseña del alias]

## Paso 3: Generar el AAB

### Opción A: Automático vía GitHub Actions

Una vez configurados los secrets:
1. Haz un push a la rama `main`
2. Ve a **Actions** en GitHub
3. Espera a que termine el build
4. Descarga `app-release-bundle` de la sección **Artifacts**
5. Descomprime y tendrás `app-release.aab`

### Opción B: Manual en tu computadora

```bash
cd uber-speed-android
export KEYSTORE_FILE=../release-key.jks
export KEYSTORE_PASSWORD=tu_contraseña_keystore
export KEY_ALIAS=uber-speed
export KEY_PASSWORD=tu_contraseña_alias
./gradlew bundleRelease
```

El AAB estará en: `app/build/outputs/bundle/release/app-release.aab`

## Paso 4: Subir a Play Store

1. Ve a [Google Play Console](https://play.google.com/console)
2. Crea una nueva aplicación
3. Completa toda la información requerida
4. Ve a **Producción** > **Crear nueva versión**
5. Sube el archivo `app-release.aab`
6. Completa la información de versión y envía para revisión

## Notas de Seguridad

- **NUNCA** subas el archivo `.jks` al repositorio
- **NUNCA** pongas contraseñas directamente en `build.gradle`
- El archivo `.gitignore` ya está configurado para ignorar `*.jks`
- Los secrets de GitHub están encriptados y solo se usan durante el build

## Versionamiento

Para actualizar la app, modifica en `app/build.gradle`:

```gradle
versionCode 2  // Incrementa este número con cada publicación
versionName "1.1"  // Versión visible para usuarios
```
