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

## 2026-09-09 — #005 · Tarea 2: código de inicio de sesión escrito

**Cambio:** se añade el inicio de sesión con Google usando **Credential Manager**, que
es el método vigente. El antiguo `GoogleSignInClient` está descontinuado y no se usa.

**Librerías fijadas**, todas comprobando antes su `minCompileSdk` (regla de la #004):

| Librería | Versión | Exige |
|---|---|---|
| `androidx.credentials` | 1.6.0 | API 35 ✅ |
| `credentials-play-services-auth` | 1.6.0 | API 35 ✅ |
| `play-services-auth` | 22.0.0 | API 1 ✅ |
| `googleid` | 1.2.0 | ninguno ✅ |

**Decisiones de diseño que conviene no perder:**

1. **Identificarse y autorizar son dos pasos distintos.** Credential Manager dice
   *quién* es la usuaria; `AuthorizationClient` concede *qué puede hacer la app* en su
   Drive. Iniciar sesión NO da permiso de escritura. La app hace ambos al entrar.
2. **No se guarda ningún token en el celular.** Caducan en una hora; se vuelven a pedir
   al abrir la app, en silencio mientras el permiso siga concedido. Solo se guarda el
   correo de la cuenta, que es lo que evita volver a pedir login (Validación A).
3. **El ID del cliente Web se lee de `local.properties`**, igual que la clave de Gemini.
   Si falta, la app **no se cae**: muestra en pantalla qué falta y dónde configurarlo.

**Bloqueo actual:** falta que el dueño del proyecto cree el proyecto en Google Cloud y
pase el **ID del cliente Web**. Hasta entonces la Validación A no puede empezar.

**Resultado:** ⏳ Compila, instala y arranca. A la espera del ID.

---

## 2026-09-09 — #006 · Tarea 1 aprobada; Tarea 3 escrita

**Tarea 1:** el dueño del proyecto dio el visto bueno al diseño el 2026-09-09.
Queda aprobada en su parte de diseño; falta la mirada en el celular real.

**Tarea 3 — decisiones que conviene no perder:**

1. **Las cuentas las hace Kotlin, no la IA.** El saldo se calcula en
   `VentaInterpretada.crear`, no lo devuelve Gemini. Un modelo de lenguaje no garantiza
   dar el mismo resultado dos veces ante la misma resta, y estas son las cuentas del
   negocio. Las instrucciones que recibe Gemini dicen literalmente *«NO calcules saldos
   ni restas»*. La temperatura va en 0 por lo mismo.
2. **Sin librerías HTTP nuevas.** Se usa `HttpURLConnection` y el `org.json` que Android
   ya trae. Con dos llamadas REST en toda la app, añadir OkHttp sería peso y
   mantenimiento sin ganancia.
3. **El nombre del modelo de Gemini es configurable** (`GEMINI_MODELO` en
   `local.properties`). Los nombres de modelo cambian con el tiempo; si el de por
   defecto desaparece, se cambia sin tocar código. El error 404 de la API lo dice
   explícitamente en pantalla.
4. **Los 15 casos de la Validación B son datos, no un documento.** Están en
   `ia/BancoDePruebas.kt` y hay una pantalla que los ejecuta contra la IA real y compara
   caso por caso. Solo existe en la versión de depuración.

**Dos fallos corregidos que habrían roto todo:**

- El manifiesto **no declaraba permiso de internet**. Ninguna llamada de red habría
  funcionado, ni a Gemini ni a Sheets. Añadido, junto con `ACCESS_NETWORK_STATE` que
  necesitará la Tarea 8.
- El texto escrito **ya no se borra** al interpretar, para poder corregir un registro
  incompleto sin volver a escribirlo (punto 6).

**Bloqueo actual:** falta la clave de Gemini (Google AI Studio) para poder ejecutar la
Validación B.

**Resultado:** ⏳ Compila e instala. A la espera de la clave.

---

## 2026-09-09 — #007 · Tarea 4: guardado en Google Sheets

**Decisiones que conviene no perder:**

1. **`insertDataOption=INSERT_ROWS`.** Es lo que garantiza el punto 5: inserta filas
   nuevas al final sin tocar nada de lo existente, en una hoja que la dueña sigue
   editando a mano.
2. **Ante la duda, no se escribe.** Si la comprobación del identificador falla (por
   ejemplo, se cae la red justo ahí), la app **no** escribe la fila y devuelve
   «sin conexión» para reintentar. Un duplicado en las cuentas del negocio es peor que
   un reintento.
3. **Rescate de la hoja tras reinstalar.** Si el celular ya no tiene guardado el ID de
   la hoja, la app la busca en Drive por su nombre antes de crear una nueva. Con el
   permiso `drive.file` esa búsqueda solo devuelve archivos creados por la propia app,
   así que no ve nada más del Drive. Sin este rescate, reinstalar habría creado una hoja
   nueva y dejado la anterior huérfana.
4. **El identificador se genera antes de enviar**, no después de que Sheets responda.
   Es lo que hará funcionar la Tarea 8: sin conexión el ID ya existe y el reintento lo
   reconoce.
5. **Se unificó el estado de pantalla** de las Tareas 3 y 4 en `EstadoDeGuardado`. Para
   la dueña «interpretar» y «guardar» son una sola acción: escribió y pulsó un botón.
6. **El código HTTP se centralizó** en `util/Http.kt`, ahora que hay cuatro llamadas
   distintas. Un fallo de red devuelve código 0, distinto de un error del servidor, para
   que la Tarea 8 sepa cuándo encolar.

**Tropiezo corregido durante el desarrollo:** al reorganizar `MainActivity` se insertó
un bloque en mitad de una línea, partiendo una declaración de estado. Detectado
revisando el archivo antes de compilar, y reparado.

**Bloqueo actual:** la Validación C no puede empezar sin el ID del cliente Web (Tarea 2),
porque escribir en la hoja necesita el permiso que da ese login.

**Resultado:** ⏳ Compila e instala. A la espera del login.

---

## 2026-09-09 — #008 · Tarea 5: confirmación en pantalla e historial real

**Decisiones que conviene no perder:**

1. **Archivo JSON en vez de Room.** Aquí hay una sola lista de unas pocas ventas al día,
   sin consultas ni relaciones. Room habría traído un generador de código, más
   dependencias y migraciones que mantener, a cambio de nada útil. La prioridad fijada
   para el proyecto es que el código sea simple.
2. **Escritura segura.** El historial se escribe a un archivo temporal y se renombra. Si
   el celular se apaga a mitad de escritura, el historial anterior queda intacto en vez
   de a medias.
3. **Solo se anota como guardada cuando Sheets lo confirma.** Si el guardado falla del
   todo, no se anota nada. Si falla por conexión, se anota como `PENDIENTE`, que es el
   estado que reintentará la Tarea 8. Es la Regla 2 aplicada también al historial.
4. **El historial guarda la venta completa, no solo el resumen.** La Tarea 8 debe poder
   reintentar el envío **sin volver a preguntarle a la IA**: repreguntar gastaría cuota
   gratuita y podría dar una lectura distinta de la que ya se le mostró a la dueña.

**Efecto secundario aceptado a conciencia:** los intentos incompletos quedan registrados
en el historial. Si la dueña escribe algo incompleto, lo corrige y lo guarda, quedarán
**dos entradas** (la incompleta y la guardada). Se eligió esto frente a borrar la
incompleta al corregir, porque adivinar qué corrección corresponde a qué intento fallido
lleva a borrar lo que no se debía. Si al usarlo resulta molesto, se cambia.

**Limpieza:** se borraron los datos de ejemplo (`DatosDeEjemplo.kt`, `VentaReciente.kt`)
y el aviso de «vista de diseño». La pantalla ya guarda de verdad.

**Resultado:** ⏳ Compila e instala. La Validación D no puede empezar sin el login.

---

## 2026-09-09 — #009 · Tarea 6: historial de errores

**Decisiones que conviene no perder:**

1. **El reintento reutiliza el identificador del intento fallido**, no genera uno nuevo.
   Si aquella escritura sí llegó a Google pero la respuesta se perdió por el camino
   —la conexión que se corta justo ahí—, el reintento consulta la hoja, encuentra ese ID
   y responde «ya estaba». Con un ID nuevo, ese mismo escenario habría creado una
   segunda fila: es exactamente el fallo que la Regla 2 existe para evitar.
2. **«Incompleto» y «error» van a sitios distintos.** Incompleto va al historial de
   ventas (falta un dato, la dueña lo corrige); error va al historial de errores (algo
   se rompió: la IA, la red, el permiso). Mezclarlos haría que un problema técnico se
   viera igual que un mensaje mal escrito, cuando lo que hay que hacer es distinto.
3. **Los incidentes de tipo «no era una venta» no ofrecen reintentar.** Dar el mismo
   texto daría el mismo resultado; la pantalla explica que hay que reescribir el mensaje.
4. **Archivo separado del historial de ventas.** Son dos cosas distintas para la dueña,
   y mezclarlas haría que un error enterrara las ventas buenas.

**Limpieza:** la navegación pasó de banderas booleanas sueltas a un enum de tres
pantallas, que ya empezaban a enredarse. La escritura segura de archivos JSON se extrajo
a `util/ArchivoJson.kt`, compartida por los dos almacenes.

**Resultado:** ⏳ Compila e instala. Sin login no se puede provocar un fallo real de
Sheets para validarla.

---

