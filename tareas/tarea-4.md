# Tarea 4 — Guardado en Google Sheets + ID único (Validación C)

**Estado:** ⬜ Pendiente
**Depende de:** Tarea 3
**Bloquea a:** Tareas 5, 7 y 8
**Validación asociada:** **C**
**Reglas del punto 10 que aplica:** Regla 2 (idempotencia)

## Objetivo

Que cada venta interpretada se agregue como una **fila nueva al final** de la hoja, sin
tocar jamás lo que la dueña haya editado a mano.

## Columnas de la hoja

| # | Columna |
|---|---|
| 1 | Fecha |
| 2 | Nombre del cliente |
| 3 | Teléfono |
| 4 | Producto |
| 5 | Talla |
| 6 | Precio |
| 7 | Modalidad de pago |
| 8 | Abono recibido |
| 9 | Saldo pendiente |
| 10 | Cuotas |
| 11 | Fecha próximo pago |
| 12 | Estado de entrega |
| 13 | Mensaje original |
| 14 | **ID de registro** ← *aprobado el 2026-09-09* |

## Alcance

**Sí incluye:**
- Crear la hoja automáticamente la primera vez, con los encabezados.
- **Solo agregar filas al final.** La app nunca sobrescribe ni borra filas existentes.
- Generar un **identificador único** por venta en el momento de escribirla, incluso
  sin internet (Regla 2).
- Antes de reintentar un envío, **verificar si ese identificador ya existe** en la hoja,
  para no duplicar la fila.
- Una venta solo se marca como **"sincronizada"** después de que Google Sheets confirme
  que la fila quedó guardada. Nunca antes.

**No incluye:** la cola offline completa (Tarea 8) ni los recordatorios (Tarea 7).

## ✅ Decisión tomada (2026-09-09): se agrega la columna 14

La Regla 2 exige un ID único para evitar duplicados, pero las 13 columnas originales
no tenían dónde guardarlo. Si el ID vivía solo en el celular, un envío interrumpido a
mitad de camino podía duplicar la fila al reintentar, porque no había forma de
preguntarle a la hoja si el registro ya existía.

**Decisión aprobada:** la hoja lleva **14 columnas**. La última, `ID de registro`, la
escribe la app. La dueña puede ignorarla u ocultarla; no le estorba para editar a mano.

Con esto, la **Validación E** (Tarea 8) ya se puede garantizar.
Queda registrado en `BITACORA.md`, entrada **#001**.


## Validación C

1. Se guardan varias ventas seguidas y aparecen como filas nuevas al final, correctas.
2. **Mientras la app está guardando, la dueña edita la hoja a mano desde su celular**
   (cambia un saldo, corrige un nombre): sus cambios **no se pierden ni se sobrescriben**.
3. Se agrega una fila manualmente en medio de la hoja y la app sigue escribiendo al final.
4. Se corta el envío a la mitad a propósito y se reintenta: **no se crea fila duplicada**.
5. Se comprueba que la venta no queda marcada como "sincronizada" si Sheets no respondió.
