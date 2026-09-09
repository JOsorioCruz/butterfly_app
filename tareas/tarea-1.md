# Tarea 1 — Pantalla principal (solo interfaz, sin red)

**Estado:** ⬜ Pendiente
**Depende de:** Tarea 0
**Bloquea a:** Tarea 2
**Validación asociada:** ninguna de A–G (prepara la D)

## Objetivo

Construir la única pantalla de la app, funcionando con datos falsos, para acordar cómo
se ve y cómo se usa **antes** de conectar nada. Así, si algo del diseño no gusta, se
cambia cuando todavía es barato cambiarlo.

## Alcance

**Sí incluye:**
- Un cuadro de texto libre grande, de varias líneas, con texto de ejemplo tenue
  (ej. *"venta de blusa: maria, talla m, 50 mil, credito, abono 20 mil"*).
- Un botón **"Guardar venta"** grande, fácil de presionar con el pulgar.
- Debajo, el **historial reciente**: lista de las últimas ventas con su resumen corto.
- Los tres estados visuales que una venta puede tener en el historial:
  `Guardada` ✅ · `Pendiente de sincronizar` ⏳ · `Incompleta` ⚠️
- Acceso a la pantalla de **historial de errores** (se construye en la Tarea 6; aquí
  solo queda el punto de entrada).

**No incluye:** login, Gemini, Google Sheets, guardado real. Todo con datos inventados.

## Criterio de diseño

Debe ser tan rápido de usar como enviar un mensaje de WhatsApp: abrir → escribir →
un botón → listo. Sin menús, sin pestañas, sin formularios con campos separados.
Si una decisión de diseño agrega un paso, se descarta.

## Validación

Esta tarea es de puro diseño y no toca la red, así que **se itera en el emulador**, que
es mucho más rápido. Antes de cerrarla, una mirada en el celular real.

1. La app abre directo en esta pantalla, sin pasos intermedios.
2. Se puede escribir en el cuadro de texto y el teclado no tapa el botón.
3. El historial falso se ve correctamente con los tres estados.
4. Te muestro capturas del emulador y esperas tu visto bueno del diseño.
5. Antes de cerrar la tarea, una mirada en el celular real para confirmar que se ve bien
   en la pantalla de verdad (tamaños y tipografías cambian entre equipos).

## Riesgos

- Es la tarea más probable de necesitar retoques por gusto visual. No es un error:
  es más barato ajustarlo aquí que en la Tarea 5.
