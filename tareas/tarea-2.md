# Tarea 2 — Inicio de sesión con Google (Validación A)

**Estado:** ⬜ Pendiente
**Depende de:** Tarea 1
**Bloquea a:** Tareas 3, 4, 8 (y por cadena, todo lo demás)
**Validación asociada:** **A**

## Objetivo

Que la dueña entre una sola vez con su cuenta de Google y la app quede autorizada a
crear y escribir su hoja de cálculo, **sin tarjeta de crédito y sin tener que volver
a entrar cada semana**.

## Alcance

**Sí incluye:**
- Crear el proyecto en Google Cloud Console y un cliente OAuth para Android
  (gratuito, no pide facturación ni tarjeta).
- Pantalla de consentimiento OAuth publicada **"En producción"**, no en "Prueba".
- Permiso (scope) mínimo: **solo los archivos que la propia app crea**
  (`drive.file`), no acceso a todo Google Drive.
- Login en la app con el método actual de Google (Credential Manager + autorización).
  El "Google Sign-In" clásico está descontinuado y no se usará.
- La sesión se guarda: al reabrir la app no vuelve a pedir login.
- Botón para cerrar sesión y volver a entrar (necesario para el instructivo del punto 12).

**No incluye:** leer o escribir datos reales todavía. Solo autorización.

## Por qué "En producción" y no "Prueba"

En estado "Prueba", Google **caduca la sesión cada 7 días** y la dueña tendría que
volver a iniciar sesión constantemente. En "Producción" la sesión se mantiene.

## Sobre la advertencia de "Google no verificó esta app"

Se le explicará que, de aparecer, debe tocar **"Avanzado"** → **"Ir a la app"**.
Es un paso único.

**Matiz que hay que comprobar en la práctica:** esa advertencia aparece cuando la app
pide permisos que Google considera *sensibles*. Como aquí se pide únicamente
`drive.file` (acceso solo a los archivos creados por la app), es posible que la
advertencia **no aparezca en absoluto**. No lo doy por sentado: se verifica en la
validación y se documenta lo que realmente pase, para que el instructivo final diga
la verdad y no asuste sin motivo.

## Validación A

**Parte inmediata (mismo día):**
1. La dueña entra con su cuenta desde el celular real y concede el permiso.
2. Se registra con captura qué pantallas exactas vio (incluida la advertencia, si sale).
3. Se cierra la app por completo y se reabre: **no** vuelve a pedir login.
4. Se comprueba que el permiso concedido es el mínimo y no acceso total a Drive.

**Parte diferida (a los 7 días):**
5. Pasados 7 días sin abrir la app, se abre y **la sesión sigue activa**.

⚠️ **Esta validación no se puede cerrar el mismo día.** El punto 5 solo se puede
comprobar cuando pase el tiempo. Se deja anotado en `BITACORA.md` con la fecha de
inicio y se revisa en la fecha correspondiente. Mientras tanto, se puede avanzar a la
Tarea 3, pero la Tarea 2 **no se marca como aprobada** hasta que el punto 5 se cumpla.

## Riesgo de cuenta (no técnico)

Todo el sistema depende de una sola cuenta de Google: hoja, login e IA. Si esa cuenta
se bloquea, se pierde todo al mismo tiempo. Por eso el instructivo final incluirá la
recomendación de **exportar una copia de la hoja una vez al mes** fuera de esa cuenta.

---

## Preparativos en Google Cloud (lo que hay que hacer antes de escribir código)

Esto lo haces tú en el navegador; yo no puedo entrar a tu cuenta. Son gratis y en
ningún momento piden tarjeta. Cuando llegue el turno de la Tarea 2 lo repasamos paso
a paso, pero lo dejo escrito desde ya para que no sea una sorpresa.

### 1. Crear el proyecto

En **console.cloud.google.com**, crear un proyecto nuevo llamado `Butterfly`.
No hace falta habilitar facturación en ningún momento.

### 2. Habilitar las dos APIs que la app usa

En **APIs y servicios → Biblioteca**, habilitar:

- **Google Sheets API** — para escribir las filas.
- **Google Drive API** — para crear la hoja la primera vez.

### 3. Pantalla de consentimiento OAuth

- Tipo: **Externo**.
- Nombre de la app: `Butterfly`. Correo de soporte: el de la dueña.
- Permiso (scope): **únicamente** `.../auth/drive.file`
  — acceso solo a los archivos que la propia app crea, no a todo el Drive.

⚠️ **Publicarla en estado «En producción».** Si se deja en «Prueba», Google cierra la
sesión **cada 7 días** y la dueña tendría que volver a entrar constantemente. Este es
el punto exacto que comprueba la parte diferida de la Validación A.

### 4. Crear DOS clientes OAuth (no uno)

Esto sorprende, así que conviene tenerlo claro de antemano. El método actual de login
de Google necesita los dos:

| Cliente | Para qué | Qué pide |
|---|---|---|
| **Android** | Autoriza que *esta* app firmada pueda pedir permisos | Nombre del paquete + huella SHA-1 |
| **Web** | Su ID es el que va escrito dentro de la app para identificar el login | Nada especial |

- Nombre del paquete: `com.butterfly.app`
- Huella SHA-1 de depuración: `77:41:FE:31:73:80:8D:0F:B1:87:EA:D2:8B:95:E0:D2:71:FB:26:3A`
  *(generada el 2026-09-09 en la primera compilación; sirve para el APK de depuración)*

### 5. Aviso de «Google no verificó esta app»

Si aparece, la dueña debe tocar **«Avanzado» → «Ir a la app»**. Es un paso único.

Como aquí solo se pide `drive.file`, que Google no clasifica como permiso sensible, es
posible que la advertencia **no aparezca**. No lo doy por hecho: se comprueba en la
Validación A y el instructivo final dirá lo que realmente pasó, para no asustar sin
motivo.

### 6. Al firmar el APK final (Tarea 9)

El APK de la Tarea 9 se firma con un almacén **distinto** al de depuración, así que
tendrá **otra huella SHA-1**. Habrá que agregar un segundo cliente Android en la consola
con esa huella, o el login dejará de funcionar en el APK instalado. Queda anotado aquí
para no descubrirlo el último día.

