# Tarea 10 — Una semana de uso real (Validación F)

**Estado:** ⬜ Pendiente
**Depende de:** Validaciones A, B, C, D, E y G ya aprobadas + Tarea 9 (app instalada)
**Bloquea a:** nada — es el cierre del proyecto
**Validación asociada:** **F**

## Objetivo

Comprobar que la app aguanta el uso diario real, que es la única prueba que importa de
verdad. Los ejemplos controlados de la Tarea 3 no sustituyen a una semana de ventas.

## Cómo se hace

La dueña usa la app con normalidad, como único método de registro, durante **al menos
7 días seguidos**. Nada de uso simulado.

Durante esa semana se revisa:

- Cada venta escrita **quedó** en la hoja (ninguna se perdió).
- La IA interpretó bien los mensajes reales, que siempre son más desordenados que los
  de prueba.
- Los recordatorios de pago llegaron el día que debían.
- La sesión de Google **no se cerró sola** (esto además cierra la parte diferida de la
  **Validación A**).
- Las ventas registradas sin señal se subieron solas y **sin duplicarse**.
- Qué apareció en el historial de errores, si algo apareció.

## Criterio de aprobación

✅ Se aprueba si: **ningún registro se perdió** y no hubo errores graves.

❌ **No** se aprueba si: se perdió aunque sea una venta, se duplicó una fila, la sesión
se cerró sola, o un recordatorio no llegó.

## Si algo falla — Regla 1

Un fallo aquí no es "un detalle final". Se aplica la **Regla 1** del punto 10: se
identifica de qué tarea viene el problema, se corrige **esa** tarea, y se revisan
**todas las tareas posteriores que dependían de ella**, informando explícitamente qué
hubo que ajustar en cada una. Todo queda anotado en `BITACORA.md` (Regla 3).

Y se aplica la **Regla 4**: no se sigue usando la app "provisionalmente" sobre una
dependencia rota. Se corrige, se revalida, y la semana de uso real **vuelve a empezar**.

## Cierre del proyecto

Con la Validación F aprobada, se entrega:
- APK final funcionando
- Instructivo (Tarea 9)
- `BITACORA.md` con el registro completo de todo lo que se corrigió durante el desarrollo
