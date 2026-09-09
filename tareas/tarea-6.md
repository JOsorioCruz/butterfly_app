# Tarea 6 — Historial de errores

**Estado:** ⬜ Pendiente
**Depende de:** Tarea 5
**Bloquea a:** Tarea 10 (Validación F)
**Validación asociada:** ninguna de A–G (corresponde al punto 9 de la especificación)

## Objetivo

Que ningún fallo termine en una fila vacía o incorrecta dentro de la hoja, y que la
dueña pueda ver qué pasó sin necesitar ayuda técnica.

## Alcance

**Sí incluye:**
- Pantalla de **"Historial de errores"** dentro de la misma app, accesible desde la
  pantalla principal.
- Se registra el incidente cuando:
  - la IA (Gemini) falla o no responde,
  - Google Sheets no responde o rechaza la escritura,
  - el texto escrito no tiene relación con una venta.
- Cada incidente guarda: **fecha y hora, el texto original, y qué salió mal en lenguaje
  simple** (no un mensaje técnico incomprensible).
- **Garantía central: ante cualquiera de esos fallos, NO se crea ninguna fila en la hoja.**
- Poder reintentar un incidente directamente desde esa pantalla.

## Validación

1. Se desconecta la IA a propósito → el incidente aparece en el historial de errores y
   **no** se crea fila en la hoja.
2. Se provoca un fallo de Google Sheets → mismo resultado.
3. Se escribe un texto sin relación con una venta (ej. *"hola que hora es"*) →
   se registra como incidente y **no** se crea fila.
4. Se reintenta un incidente cuando el servicio ya funciona y la venta se guarda bien,
   **sin duplicarse** (respetando el ID único de la Tarea 4).
5. Los mensajes de error se entienden sin saber de tecnología.
