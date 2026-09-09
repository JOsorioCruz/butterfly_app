# Tarea 7 — Recordatorios de pagos a crédito (Validación G)

**Estado:** ⬜ Pendiente
**Depende de:** Tarea 4 (necesita que se guarden bien la fecha de próximo pago y el saldo)
**Bloquea a:** Tarea 10 (Validación F)
**Validación asociada:** **G**

## Objetivo

Que la dueña no tenga que acordarse sola de quién le debe. Todo con notificaciones
locales del propio celular: **sin servidor externo y sin costo**.

## Alcance

**Sí incluye:**
- Al guardar una venta a crédito **con fecha de próximo pago**, se programa una
  notificación local para ese día.
- **Barrido diario** (al amanecer): revisa todas las ventas a crédito con saldo mayor
  a $0 y avisa de pagos **vencidos** o **próximos a vencer (2 días)**.
- **Agrupación:** si hay varios clientes pendientes el mismo día, se envía **una sola
  notificación** con todos, no una por cliente.
- **Cancelación automática:** si el saldo de una venta llega a $0, se cancela cualquier
  recordatorio pendiente asociado.
- Cada recordatorio queda vinculado al **identificador único de la venta** (el mismo de
  la Regla 2, Tarea 4), para poder actualizarlo o cancelarlo si el registro cambia.

## Detalles de Android que hay que resolver aquí

Estos no son opcionales; si se ignoran, las notificaciones simplemente no llegan:

- **Android 13 y superior** exige pedir permiso explícito para enviar notificaciones.
- **Android 12 y superior** restringe las alarmas a hora exacta y requiere un permiso
  aparte.
- El **ahorro de batería** del fabricante puede retrasar o matar el barrido diario.
  Se probará en el celular real y, si hace falta, el instructivo final incluirá el paso
  para excluir Butterfly del ahorro de batería.

## Validación G

1. Se registra una venta a crédito con fecha de próximo pago → **la notificación llega
   ese día** (se prueba adelantando el reloj del emulador y luego en el celular real).
2. Se salda la deuda antes de la fecha → **el recordatorio se cancela solo** y no llega.
3. Se registran dos o más clientes con pago el mismo día → llega **un solo aviso
   agrupado**, no uno por cliente.
4. Una venta a crédito **sin** fecha de próximo pago no programa nada, pero **sí** aparece
   en el barrido diario mientras tenga saldo pendiente.
5. Se comprueba que un pago vencido genera aviso, y uno a 2 días también.
