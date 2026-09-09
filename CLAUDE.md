# CLAUDE.md

Guía para Claude Code trabajando en este repositorio.

## Qué es esto

**Butterfly** 🦋 es una app Android nativa (Kotlin + Jetpack Compose) para un negocio de
venta de ropa. La dueña escribe la venta en **un solo cuadro de texto libre**, tal como
escribiría un mensaje de WhatsApp (*"venta de blusa: maria, talla m, 50 mil, credito,
abono 20 mil"*), presiona **"Guardar venta"**, y la app hace el resto: interpreta el
texto con Google Gemini y agrega una fila a una Google Sheet.

Reemplaza el método anterior de autoenviarse un mensaje de WhatsApp. **No usa WhatsApp
ni ningún servidor propio.**

## Restricción que manda sobre todo lo demás

**No se puede asociar ninguna tarjeta de crédito o débito a ningún servicio.** Todo debe
funcionar en niveles gratuitos que no pidan facturación: OAuth de Google Cloud, la API
de Sheets/Drive, y Gemini vía Google AI Studio. Si una solución requiere habilitar
facturación, no sirve — hay que buscar otra.

## Cómo se trabaja aquí: una tarea a la vez

El desarrollo está dividido en tareas en la carpeta **`tareas/`**, una por archivo, cada
una con su propia validación. **Empieza por `tareas/README.md`**, que tiene el índice, el
mapa de dependencias y el estado de cada tarea.

No construyas la app completa de una vez. Cada tarea se entrega, se valida, y **se espera
la aprobación explícita del usuario antes de pasar a la siguiente**.

### Las cuatro reglas de dependencia

1. **Ninguna tarea se aprueba aislada.** Si al validar aparece un error o se cambia el
   diseño, todas las tareas posteriores que dependan de ella se revisan otra vez,
   informando explícitamente qué hubo que ajustar en cada una.
2. **Idempotencia obligatoria.** Cada venta lleva un identificador único generado al
   escribirla, incluso sin conexión. Solo se marca como "sincronizada" tras confirmar que
   la fila quedó en Sheets. Al reintentar se verifica ese ID para no duplicar filas.
3. **Bitácora.** Todo error corregido se anota en **`BITACORA.md`** (raíz): qué se cambió,
   por qué, y qué otras tareas se revisaron. Es un requisito del proyecto, no opcional.
4. **No se avanza sobre una dependencia rota.** Se corrige, se revalida, y solo entonces
   se retoman las tareas dependientes.

## Compilar y ejecutar

⚠️ **El JDK 25 del sistema no sirve.** El plugin de Android (AGP 8.x) no lo soporta. Hay
que usar el JDK 21 que trae Android Studio:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # instala en el emulador o celular conectado
```

Herramientas del SDK (no están en el `PATH`):

```sh
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"   # adb
adb devices
```

**Dónde se prueba** (decisión del usuario, 2026-09-09):

- **Pruebas preliminares, durante el desarrollo** → emulador `Medium_Phone API 36.1`.
- **Prueba final de cada validación (A–G)** → **celular Android físico real**.
  Ninguna validación se marca como aprobada solo con el emulador.

El emulador no reproduce el ahorro de batería del fabricante (recordatorios), la pérdida
real de señal (modo offline), ni la instalación de un APK de origen desconocido. El
celular necesita "Depuración por USB" activada; los pasos están en `tareas/tarea-0.md`.

No hay `cmdline-tools` instalado; `sdkmanager` no está disponible desde la terminal.

## Estructura y versiones

```
butterfly_app/
├── tareas/              ← el plan: una tarea por archivo, empieza por README.md
├── BITACORA.md          ← Regla 3: registro de todo lo que se corrigió
├── gradle/libs.versions.toml   ← TODAS las versiones de dependencias viven aquí
├── local.properties     ← sdk.dir + GEMINI_API_KEY (fuera del control de versiones)
└── app/src/main/java/com/butterfly/app/
```

| | |
|---|---|
| AGP | 8.13.2 |
| Gradle | 8.14.3 |
| Kotlin | 2.2.21 |
| compileSdk / targetSdk | 36 (es la única plataforma instalada) |
| minSdk | 26 (Android 8) |
| Interfaz | Jetpack Compose + Material 3 |

Al agregar una dependencia, declárala en **`gradle/libs.versions.toml`**, no directamente
en `app/build.gradle.kts`.

## Claves y secretos

La clave de Gemini se lee de `local.properties` (excluido de git) y llega al código como
`BuildConfig.GEMINI_API_KEY`. **Nunca la escribas dentro del código fuente.** Si el
archivo no tiene la clave, la app compila igual y debe fallar de forma controlada.

Lo mismo aplica al archivo de firma del APK (`*.jks`): ya está en `.gitignore`.

## Reglas de la Google Sheet

La hoja tiene **14 columnas**: Fecha, Nombre del cliente, Teléfono, Producto, Talla,
Precio, Modalidad de pago, Abono recibido, Saldo pendiente, Cuotas, Fecha próximo pago,
Estado de entrega, Mensaje original, **ID de registro**.

- **La app SOLO agrega filas al final. Nunca sobrescribe ni borra filas existentes.** La
  dueña sigue editando la hoja a mano desde su celular y sus cambios no se pueden perder.
- La columna 14 `ID de registro` existe para cumplir la Regla 2 (evitar duplicados).
- **Nunca inventes datos.** Si falta nombre, producto o precio, el registro se marca como
  "incompleto". Si la IA o Sheets fallan, el incidente va al historial de errores de la
  app y **no se crea ninguna fila**.

## Idioma

El usuario trabaja en español. La documentación del proyecto (`tareas/`, `BITACORA.md`,
el instructivo final) va en español, igual que los textos de la interfaz y los nombres de
las funciones y variables nuevas.
