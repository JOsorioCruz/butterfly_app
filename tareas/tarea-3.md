# Tarea 3 — Gemini interpreta el texto + lógica de crédito (Validación B)

**Estado:** ⬜ Pendiente
**Depende de:** Tarea 2
**Bloquea a:** Tareas 4 y 8
**Validación asociada:** **B**

## Objetivo

Convertir un mensaje escrito a mano — con el mismo estilo desordenado de un WhatsApp —
en datos estructurados y confiables.

Ejemplo de entrada:
> `venta de blusa: maria, talla m, 50 mil, credito, abono 20 mil`

## Alcance

**Sí incluye:**
- Llamada a la API de Google Gemini vía Google AI Studio (nivel gratuito, sin tarjeta
  ni facturación).
- Extracción de: nombre del cliente, producto, talla, precio, modalidad de pago,
  monto de abono, estado de entrega, y **teléfono si el texto lo menciona**.
- Interpretación de precios escritos en lenguaje natural colombiano
  (`50 mil`, `50.000`, `50k`, `cincuenta mil`).
- Regla de modalidad: si menciona *crédito*, *abono*, *cuotas* o similar → **Crédito**.
  Si no menciona nada → **Inmediato**.
- **Lógica del punto 4:**
  - Crédito → `Saldo pendiente = Precio − Abono`. Cuotas y fecha de próximo pago
    quedan **vacías** si no se mencionaron (no se inventan).
  - Inmediato → `Saldo pendiente = $0`.
- **Regla anti-invención:** si falta nombre, producto o precio, el registro se marca
  como **"incompleto"** y se dice cuál dato falta. Nunca se rellena a la fuerza.
- La clave de la API se guarda en un archivo local fuera del control de versiones,
  nunca escrita dentro del código.

**No incluye:** guardar nada en Google Sheets todavía (eso es la Tarea 4).

## Aclaración sobre el teléfono

La columna *Teléfono* existe en la hoja (punto 5) pero no estaba en la lista de datos
a extraer (punto 3). Criterio adoptado: **se extrae si aparece en el texto; si no,
queda vacía y NO cuenta como "incompleto"**. Solo nombre, producto y precio disparan
el estado incompleto.

## Nota de seguridad, dicha sin alarmismo

Una clave de API dentro de un APK siempre es extraíble por alguien con conocimientos.
Para una app personal de un solo usuario que no se distribuye, es un riesgo aceptable.
Lo que sí se evita es dejarla escrita en el código fuente.

## Validación B

Se prueban **al menos 15 mensajes de ejemplo** con estilos de escritura distintos,
cubriendo obligatoriamente:
- venta a crédito con abono parcial
- venta a crédito sin abono
- pago inmediato
- precios escritos de varias formas (`50 mil`, `50.000`, `50k`)
- sin talla
- con teléfono y sin teléfono
- con estado de entrega mencionado
- texto sin nombre → debe salir **incompleto**
- texto sin precio → debe salir **incompleto**
- texto que no tiene nada que ver con una venta → **no** debe producir datos

Te entrego una **tabla comparando lo escrito contra lo que la IA entendió**, caso por
caso, para que la revises antes de aprobar.

## Riesgos

- La IA puede acertar en 15 ejemplos y fallar en el 16 real. Por eso el punto 9
  (historial de errores, Tarea 6) y la Validación F (uso real) existen.
- Si el nivel gratuito de Gemini se agota o cambia, la app debe fallar mostrando el
  error, nunca guardando una fila inventada.
