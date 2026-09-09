# Tarea 0 — Convertir el proyecto a Android + Gradle

**Estado:** ⬜ Pendiente de aprobación
**Depende de:** nada
**Bloquea a:** todas las demás tareas
**Validación asociada:** ninguna de la lista A–G (es trabajo de base)

## Por qué existe esta tarea

El directorio `butterfly_app` **hoy no es un proyecto Android**. Es un proyecto Kotlin
de escritorio (JVM) creado por IntelliJ, con un solo archivo `src/Main.kt` que contiene
la plantilla de ejemplo del IDE. En este estado:

- No hay Gradle ni Maven, así que **no se puede generar un APK**.
- No es un repositorio git, así que no hay historial de cambios.
- El `PATH` apunta a **JDK 25**, que el plugin de Android (AGP) no soporta.

Nada de lo demás se puede construir encima de esto. Por eso es la Tarea 0.

## Objetivo

Dejar un proyecto Android que compile, se instale en el emulador y arranque, aunque
todavía no haga nada útil.

## Alcance

**Sí incluye:**
- Estructura de proyecto Android con Gradle (Kotlin DSL).
- `minSdk 26` (Android 8) — ajustable según el celular de la dueña.
- `compileSdk` / `targetSdk` 36 (es la única plataforma instalada en esta máquina).
- Compilar con el **JDK 21** que trae Android Studio, no con el JDK 25 del sistema.
- Jetpack Compose para la interfaz.
- `git init` + primer commit.
- Crear `BITACORA.md` en la raíz (exigido por la **Regla 3** del punto 10).
- Actualizar `CLAUDE.md`, que hoy describe el proyecto viejo de escritorio.
- Un archivo local para claves (Gemini) **fuera** del control de versiones.

**No incluye:** ninguna pantalla real, ningún login, ninguna llamada de red.

## Cambio destructivo que requiere tu aprobación

El archivo actual `src/Main.kt` (la plantilla `println("Hello, Kotlin!")` de IntelliJ)
**se elimina**, junto con la configuración de módulo de escritorio. No hay nada que
perder ahí, pero lo digo explícitamente porque es irreversible sin git.

## Validación

Reparto acordado el 2026-09-09: **las pruebas preliminares se hacen en el emulador para
avanzar rápido; la prueba final de cada validación se hace en el celular físico real.**

1. `./gradlew assembleDebug` termina sin errores y produce un archivo `.apk`.
2. El APK arranca sin cerrarse. Se muestra primero en el emulador (para no depender del
   cable) y se confirma la instalación **en el celular real** en cuanto esté conectado.
3. `git log` muestra el primer commit.
4. Existen `BITACORA.md` y un `CLAUDE.md` actualizado.

## Qué hace falta del lado del celular

Para instalar desde el computador hay que activar, una sola vez, en el celular:

1. **Ajustes → Acerca del teléfono** → tocar 7 veces en **"Número de compilación"**
   para activar las Opciones de desarrollador.
2. **Ajustes → Sistema → Opciones de desarrollador** → activar **"Depuración por USB"**.
3. Conectar el celular por cable al computador y aceptar el aviso
   **"¿Permitir depuración por USB?"** que aparece en la pantalla del celular.

Con eso, `adb devices` debe mostrar el equipo y ya se puede instalar.

## Decisiones abiertas

- **Versión de Android del celular.** Al conectarlo se lee automáticamente y se ajusta
  `minSdk` si hace falta. Por ahora queda en `minSdk 26` (Android 8).
