# Bitácora de cambios — Butterfly 🦋

Registro único exigido por la **Regla 3** del punto 10 de la especificación.

Cada vez que se detecta un error o se cambia algo del diseño, se anota aquí:
**qué se modificó**, **por qué**, y **qué otras tareas se revisaron como consecuencia**
(Regla 1). Nada de esto se deja solo en la conversación.

Formato de cada entrada:

- **Fecha** · **Origen** (qué lo motivó) · **Cambio** · **Tareas revisadas** · **Resultado**

---

## 2026-09-09 — #001 · Se agrega la columna 14 «ID de registro» a la Google Sheet

**Origen:** conflicto detectado al planificar. La **Regla 2** exige un identificador
único por registro para evitar filas duplicadas al reintentar un envío, pero las 13
columnas de la especificación original (punto 5) no tenían dónde guardarlo. Si el ID
vivía solo en el celular, un envío interrumpido a mitad de camino podía duplicar la
fila, porque no había forma de preguntarle a la hoja si el registro ya existía.

**Cambio:** la hoja pasa de 13 a **14 columnas**. La nueva columna, al final, se llama
`ID de registro`. La dueña puede ignorarla u ocultarla; no interfiere con su edición
manual.

**Decisión aprobada por:** el dueño del proyecto, el 2026-09-09.

**Tareas revisadas como consecuencia (Regla 1):**

| Tarea | Qué se revisó | Ajuste |
|---|---|---|
| [4](tareas/tarea-4.md) | Guardado en Sheets | Deja de ser «decisión abierta» y pasa a ser parte del alcance confirmado |
| [8](tareas/tarea-8.md) | Modo offline | Se levanta el bloqueo: la Validación E ya se puede garantizar |
| [6](tareas/tarea-6.md) | Historial de errores | El reintento desde esa pantalla se apoya en el mismo ID; sin cambios de diseño |
| [7](tareas/tarea-7.md) | Recordatorios | Ya vinculaba los recordatorios al ID; sin cambios |

**Resultado:** ✅ Aplicado. Ninguna tarea quedó bloqueada.

---

## 2026-09-09 — #002 · Tarea 0: el proyecto se convierte de Kotlin de escritorio a Android

**Origen:** el directorio `butterfly_app` no era un proyecto Android sino un proyecto
Kotlin de escritorio (JVM) generado por IntelliJ, con un solo `src/Main.kt` de plantilla.
En ese estado **no podía producir un APK**, que es el entregable del punto 12.

**Cambio:**

- Eliminados `src/Main.kt`, `butterfly_app.iml` y la carpeta `.idea/` del proyecto de
  escritorio. *(Respaldo previo guardado fuera del proyecto antes de borrar.)*
- Creado un proyecto Android con Gradle (Kotlin DSL) y Jetpack Compose.
- `minSdk 26` (Android 8), `compileSdk` y `targetSdk` 36.
- Se compila con el **JDK 21** que trae Android Studio. El JDK 25 del sistema **no**
  sirve: el plugin de Android no lo soporta.
- Las versiones de todas las dependencias viven en un solo archivo,
  `gradle/libs.versions.toml`.
- La clave de Gemini se lee de `local.properties`, que está excluido del control de
  versiones. Si falta, la app compila igual.
- Repositorio git inicializado.

**Tareas revisadas como consecuencia:** ninguna. Es la primera tarea; no hay nada
construido encima todavía.

**Resultado:** ✅ Compila, instala y arranca. Pendiente el visto bueno del usuario.

---

## 2026-09-09 — #003 · Dónde se prueba: preliminares en emulador, finales en celular real

**Origen:** decisión del dueño del proyecto durante la Tarea 0.

**Cambio:** cada tarea se prueba en dos momentos.

| Momento | Dónde | Para qué |
|---|---|---|
| **Pruebas preliminares** | Emulador `Medium_Phone API 36.1` | Iterar rápido durante el desarrollo, sin cable |
| **Prueba final de cada validación (A–G)** | **Celular Android físico real** | Es lo único que cuenta para dar una tarea por aprobada |

Con esto se avanza rápido, pero **ninguna validación se marca como aprobada solo con el
emulador**.

**Cuatro cosas que el emulador no puede probar, y que por eso obligan al celular real:**

- El **ahorro de batería del fabricante**, que puede retrasar o matar los recordatorios
  de pago (Validación G).
- La **pérdida real de señal** en la calle, que no es lo mismo que el modo avión
  simulado (Validación E).
- La instalación de un **APK de origen desconocido** desde el celular (Tarea 9).
- El **uso real durante una semana** por parte de la dueña (Validación F).

**Tareas revisadas como consecuencia (Regla 1):**

| Tarea | Ajuste |
|---|---|
| [0](tareas/tarea-0.md) | Arranque se muestra en emulador; instalación se confirma en el celular real. Se agregan los pasos para activar «Depuración por USB» |
| [1](tareas/tarea-1.md) | Diseño se itera en emulador; una mirada final en el celular real antes de cerrar |
| [2](tareas/tarea-2.md) | Validación A: prueba final obligatoria en celular real, con la cuenta de Google de la dueña |
| [3](tareas/tarea-3.md) | Los 15 mensajes de ejemplo se pueden correr en emulador; sin cambios |
| [4](tareas/tarea-4.md) | Validación C: prueba final en celular real, con la dueña editando la hoja a la vez |
| [7](tareas/tarea-7.md) | Validación G: obligatoriamente en celular real (ahorro de batería) |
| [8](tareas/tarea-8.md) | Validación E: obligatoriamente en celular real (señal real) |
| [10](tareas/tarea-10.md) | Validación F: por definición, celular real de la dueña |

**Resultado:** ✅ Aplicado.

---

## 2026-09-09 — #004 · La compilación falló: las librerías más nuevas exigen API 37

**Origen:** primera compilación de la Tarea 0. Falló en `:app:checkDebugAarMetadata`
con 28 problemas, todos la misma causa.

**Qué pasó:** al escribir el proyecto elegí las versiones **más recientes** de cada
librería. Resultó que las de 2026 (Compose 1.12.0, `androidx.core` 1.19.0,
`lifecycle` 2.11.0) exigen compilar contra **API 37**, y en esta máquina solo está
instalada la plataforma **android-36**. Además, AGP 8.13.2 no soporta compilar contra 37.

**Las dos salidas posibles, y por qué elegí la segunda:**

| Opción | Costo |
|---|---|
| Subir a `compileSdk 37` + AGP 9.x | Descargar otra plataforma del SDK (~80 MB) con una conexión lenta, y saltar a una versión mayor de AGP cuyo comportamiento no conozco bien |
| **Bajar las librerías a versiones que acepten API 36** | Sin descargas de SDK; se queda en terreno conocido |

**Cambio:** se fijan versiones compatibles con API 36 en `gradle/libs.versions.toml`.
Verificado leyendo el `aar-metadata.properties` de cada librería, no adivinando:

| Librería | Antes | Ahora | Mínimo que exige |
|---|---|---|---|
| Compose (ui, foundation, animation…) | 1.12.0 | 1.11.0 | API 35 ✅ |
| androidx.core | 1.19.0 | 1.18.0 | API 36 ✅ |

**Lección que queda para el resto del proyecto:** en este proyecto **no se usa
automáticamente la versión más nueva de una librería**. Se usa la más nueva que sea
compatible con `compileSdk 36`. Cuando haga falta agregar una dependencia (Sheets,
Room, WorkManager…), hay que comprobar su `minCompileSdk` antes de fijarla.

**Tareas revisadas como consecuencia (Regla 1):** ninguna construida todavía; la
Tarea 1 (pantalla) no usa ninguna API que cambie entre Compose 1.11 y 1.12.

**Resultado:** ✅ Corregido. `BUILD SUCCESSFUL` con las versiones bajadas; APK de 11 MB
generado e instalado en el emulador.

---

