# Tarea 8 — Modo offline: cola local y sincronización automática (Validación E)

**Estado:** ⬜ Pendiente
**Depende de:** Tareas 2, 3 y 4 — **todas validadas por separado antes de probarlas sin conexión**
**Bloquea a:** Tarea 10 (Validación F)
**Validación asociada:** **E**
**Reglas del punto 10 que aplica:** Regla 2 (idempotencia)

## Objetivo

Que la dueña pueda registrar una venta aunque no tenga señal, y que la app se encargue
sola de subirla cuando vuelva internet. Ella no debe hacer nada más.

## Alcance

**Sí incluye:**
- Al presionar "Guardar venta" sin internet, el registro se guarda **en el celular**, en
  una cola pendiente.
- Aparece en el historial como **"Pendiente de sincronizar"** ⏳.
- Cuando vuelve la conexión, se envía **automáticamente**, sin intervención de la dueña.
- Al confirmarse el guardado en Sheets, cambia a **"Guardada"** ✅.
- Los reintentos respetan la **Regla 2**: se verifica el identificador único antes de
  escribir, para no crear filas duplicadas.
- Si el celular se reinicia con la cola llena, la sincronización se retoma igual.

## Nota sobre la interpretación de la IA sin internet

Gemini también necesita internet. Por lo tanto, sin conexión **el texto se guarda tal
como fue escrito** y la interpretación se hace en el momento de sincronizar. El mensaje
original nunca se pierde, que es lo que importa.

## ✅ Dependencia resuelta: la columna «ID de registro» fue aprobada

Esta validación depende de poder verificar el identificador único **en la hoja**, no
solo en el celular. La columna 14 `ID de registro` quedó aprobada el 2026-09-09
(ver `tarea-4.md` y la entrada **#001** de `BITACORA.md`), así que el punto 3 de la
validación ya se puede garantizar.


## Validación E

1. Se activa el modo avión, se guarda una venta → queda como **"Pendiente"** y no se pierde.
2. Se desactiva el modo avión → se sincroniza **sola**, sin que la dueña toque nada.
3. **No se duplica.** Se comprueba en la hoja que hay exactamente una fila.
4. Se guardan **varias** ventas sin conexión → todas se suben, en orden y sin duplicados.
5. Se corta la conexión **justo durante** el envío y se restablece → una sola fila.
6. Se reinicia el celular con ventas pendientes en la cola → se sincronizan igual.
