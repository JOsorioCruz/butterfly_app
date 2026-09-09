# Tarea 5 — Confirmación en pantalla e historial reciente (Validación D)

**Estado:** ⬜ Pendiente
**Depende de:** Tarea 4
**Bloquea a:** Tarea 10 (Validación F)
**Validación asociada:** **D**

## Objetivo

Que la dueña vea, sin salir de la app, que la venta quedó bien registrada. Es lo que
reemplaza la tranquilidad de "ver el mensaje enviado" en WhatsApp.

## Alcance

**Sí incluye:**
- Resumen inmediato después de guardar, en el formato acordado:

  > ✅ Venta guardada: Maria - Blusa talla M - $50.000 - Crédito (abono $20.000, saldo $30.000)

- La venta se agrega al historial reciente de la pantalla principal (Tarea 1), ahora
  con datos reales en vez de falsos.
- **Aviso de registro incompleto:** si falta nombre, producto o precio, se muestra
  claramente **cuál** dato falta.
- **El texto escrito no se pierde.** Ante un registro incompleto, el mensaje original
  queda en el cuadro de texto para corregirlo y reintentar sin volver a escribirlo todo.
- Formato de dinero en pesos colombianos (`$50.000`), no `50000.0`.

## Validación D

1. Se guarda una venta a crédito con abono → el resumen muestra precio, abono y saldo
   correctos.
2. Se guarda una venta de pago inmediato → el resumen muestra saldo $0.
3. Ambas aparecen en el historial reciente con el resumen correcto.
4. Se escribe un texto sin precio → aparece el aviso de incompleto **diciendo que falta
   el precio**, y el texto escrito sigue en pantalla.
5. Se corrige ese mismo texto agregando el precio y se guarda bien al segundo intento.
6. Se cierra y reabre la app: el historial reciente sigue ahí.
