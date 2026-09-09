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

## 2026-09-09 — #010 · Tarea 7: recordatorios de pago

**El problema de diseño que decidió el enfoque:** ¿de dónde saca la app que una deuda ya
se pagó? La dueña **edita la hoja a mano** — ese es todo el punto del sistema. Si marca
un saldo en $0 desde su celular, la app no se entera por sí sola. Un recordatorio basado
en los datos del teléfono seguiría avisando de una deuda ya cobrada.

**Decisión:** los recordatorios leen **la hoja**, no el archivo local. La hoja es la
fuente de verdad. Cada vez que la app se abre con permiso, cuadra los avisos programados
con lo que la hoja dice hoy: lo que ya no debe nada, se cancela.

**Otras decisiones que conviene no perder:**

1. **Alarmas inexactas, a propósito.** Android 12+ exige un permiso especial para las
   alarmas al minuto exacto, y aquí no aporta nada: para «hoy toca cobrarle a María» da
   igual que el aviso llegue a las 8:00 o a las 8:20. Pedir ese permiso habría añadido
   un paso más de configuración a cambio de nada.
2. **Un solo aviso agrupado**, nunca uno por clienta. Varios avisos el mismo día
   convertirían la app en algo molesto que se termina silenciando.
3. **Respaldo cuando no hay red.** Si al amanecer no se puede leer la hoja, se avisa con
   la última copia guardada de las deudas. Puede estar algo desactualizada, pero un aviso
   de más es mejor que olvidar un cobro.
4. **Autorización silenciosa.** El barrido corre sin pantalla abierta, así que no puede
   pedirle nada a la dueña. Se usa el cliente de autorización por `Context`: si el
   permiso sigue concedido devuelve el token sin mostrar nada; si haría falta
   intervención, se recurre al respaldo.
5. **Reprogramación tras reiniciar.** Android borra todas las alarmas al reiniciar el
   celular. Sin `BOOT_COMPLETED`, los recordatorios habrían desaparecido en silencio.

**Resultado:** ⏳ Compila e instala. La Validación G necesita el login (para leer saldos
de la hoja) y, obligatoriamente, el celular real: el emulador no reproduce el ahorro de
batería del fabricante.

---

## 2026-09-09 — #011 · Tarea 8: modo offline y sincronización

**El problema de fondo:** Gemini también necesita internet. Sin conexión no se puede
interpretar el texto, así que **se guarda el mensaje tal como se escribió** y la
interpretación se hace al sincronizar. Lo que importa es que el mensaje no se pierda.

**Decisiones que conviene no perder:**

1. **Se comprueba la conexión ANTES de llamar a la IA.** Antes de esta tarea, escribir
   sin internet acababa en el historial de errores como «la IA no respondió», que era
   un diagnóstico equivocado y dejaba la venta sin registrar. Ahora va a la cola.
2. **WorkManager, no un escucha de red.** Es lo único que cumple las tres condiciones a
   la vez: espera a que haya internet, sobrevive a que se cierre la app y sobrevive a
   que se reinicie el celular. Un `BroadcastReceiver` de conectividad no sobrevive a lo
   último, y desde Android 7 ese aviso implícito ya no llega estando la app cerrada.
   Versión 2.11.2, comprobado su `minCompileSdk` (35) según la regla de la #004.
3. **`ExistingWorkPolicy.KEEP`.** Si ya hay un trabajo esperando conexión, se deja el
   que está: haría exactamente lo mismo y amontonarlos solo gasta batería.
4. **`Result.retry()` en vez de reintentar en bucle.** WorkManager espera cada vez un
   poco más, que es lo correcto cuando el problema es que no hay red.
5. **Las pendientes se suben de la más antigua a la más reciente**, para que la hoja
   quede en orden cronológico y no al revés.
6. **No se vuelve a preguntar a la IA si ya se había interpretado.** Una venta que se
   interpretó pero no llegó a la hoja conserva sus datos: repreguntar gastaría cuota
   gratuita y podría dar una lectura distinta de la que ya se le mostró a la dueña.
7. **El resumen se actualiza al sincronizar.** Una venta escrita sin conexión aparece
   en el historial con el texto crudo; cuando se sube, pasa a mostrar el resumen real.

**Sobre la Regla 2:** cada pendiente conserva el identificador que se le generó al
escribirla, y `HojaDeVentas.guardar` lo comprueba en la hoja antes de escribir. Esa es
la garantía de que la Validación E («se sincroniza sola sin duplicarse») se cumple
incluso si el envío se corta a la mitad.

**Corrección de mensaje:** el aviso de sin conexión decía «la venta no se ha guardado
todavía», lo que sonaba a que se había perdido. Ahora dice que quedó guardada en el
celular y que se subirá sola.

**Resultado:** ⏳ Compila e instala. La Validación E necesita el login y, obligatoriamente,
el celular real: el emulador no reproduce la pérdida de señal de la calle.

---

## 2026-09-09 — #012 · Tarea 9: APK firmado e instructivo

**Hecho:**

- Almacén de firma `butterfly-firma.jks` creado, válido hasta 2054. La contraseña vive
  en `keystore.properties`, fuera del control de versiones junto con el `.jks`.
- `assembleRelease` produce un APK firmado de 10 MB, verificado con `apksigner`,
  instalado y arrancado.
- `INSTRUCTIVO.md` escrito en lenguaje llano, cubriendo los seis puntos del punto 12 de
  la especificación más el respaldo mensual.

**Decisiones que conviene no perder:**

1. **Minificación desactivada a propósito.** Puede romper en silencio el código que usa
   reflexión (las librerías de Google), y este proyecto todavía no tiene pruebas que lo
   detecten. Activarla es una decisión para cuando la app lleve tiempo funcionando.
2. **La firma es opcional en el build.** Si `keystore.properties` no existe, la versión
   de release simplemente no se firma, en vez de fallar la compilación. Así el proyecto
   se puede compilar sin tener las claves a mano.
3. **El instructivo nombra las variantes reales del menú de Android** («Permitir desde
   esta fuente» / «Instalar apps desconocidas» / «Fuentes desconocidas»), porque el
   nombre cambia según la marca del celular y una guía con un solo nombre falla en la
   mitad de los equipos.
4. **Incluye el paso de Play Protect**, que aparece al instalar un APK de fuera de la
   tienda y no estaba previsto en la especificación.
5. **Incluye cómo quitar la restricción de batería**, con las marcas concretas que la
   aplican. Sin eso, los recordatorios de la Tarea 7 pueden no llegar nunca.

**⚠️ Hallazgo importante — la huella de release:** el APK instalable se firma con un
almacén distinto al de depuración, así que tiene **otra huella SHA-1**. Hay que
registrar **las dos** en Google Cloud, o el login funcionará en las pruebas y **fallará
justo en el APK que se instala en el celular**. Ambas quedaron anotadas en `tarea-2.md`.

**⚠️ Estado real del APK generado:** compila, se firma, se instala y arranca — pero
`local.properties` está vacío, así que **muestra la pantalla de «falta configurar» y no
puede hacer nada más**. Para que sea el APK definitivo hay que poner la clave de Gemini
y el ID del cliente Web y volver a ejecutar `assembleRelease`.

**Resultado:** ⏳ Entregables listos. El APK no es utilizable hasta configurar las claves.

---

## 2026-09-09 — #013 · Tarea 10: preparada + revisión previa del código

**La Tarea 10 no se puede ejecutar todavía**, y no por falta de código: la Validación F
exige siete días de calendario con la app funcionando en el celular de la dueña. No hay
forma de adelantarla.

Lo que sí se hizo:

### 1. `SEGUIMIENTO-SEMANA.md`

La hoja de control de la semana: 10 casillas obligatorias antes del día 1, registro
diario, qué vigilar, y el criterio de aprobación. Incluye una tabla para anotar los
mensajes que la IA entienda mal — esos casos valen más que los 15 inventados, porque son
reales.

### 2. Revisión del código antes de que entre en uso diario

Se encontraron y corrigieron **dos fallos reales**:

**Fallo A — «no pude comprobar» se confundía con «no hay deudas».**
`ventasConSaldoPendiente` devolvía una lista vacía cuando todavía no se conocía el ID de
la hoja. Quien llama interpreta la lista vacía como «ya nadie debe nada» y **cancela
todos los recordatorios**. Ahora devuelve `null`, que significa «no se pudo comprobar», y
los dos consumidores ya lo manejaban correctamente.

**Fallo B — reintentar sin conexión duplicaba la entrada en el historial local.**
Al reintentar una venta desde el historial de errores se reutiliza su identificador
(Regla 2), pero `HistorialLocal.agregar` no comprobaba si ya existía uno con ese
identificador: quedaban dos entradas de la misma venta en la lista del celular. La hoja
nunca se habría duplicado —eso lo protege la comprobación del ID—, pero la dueña habría
visto la misma venta dos veces y no habría sabido cuál creer. Ahora sustituye en vez de
duplicar.

Ambos compilan y están confirmados.

**Resultado:** 📋 Preparada. No puede empezar hasta que la app funcione en el celular.

---

