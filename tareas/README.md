# Butterfly 🦋 — Índice de tareas

App Android nativa (Kotlin) para registrar ventas de ropa escribiendo un mensaje libre,
que se interpreta con IA y se guarda en Google Sheets. Sin WhatsApp, sin servidor propio
y **sin asociar ninguna tarjeta de crédito o débito en ningún servicio**.

## Cómo se trabaja

Una tarea a la vez. Cada una se construye, se valida, y **se espera aprobación explícita
antes de pasar a la siguiente**. No se entrega todo el desarrollo de una vez.

## Estado

| # | Tarea | Validación | Estado |
|---|---|---|---|
| [0](tarea-0.md) | Convertir el proyecto a Android + Gradle | — | ✅ Compila e instala — falta tu visto bueno |
| [1](tarea-1.md) | Pantalla principal (solo interfaz) | — | ✅ **Diseño aprobado** el 2026-09-09 |
| [2](tarea-2.md) | Inicio de sesión con Google | **A** | 🔨 Código listo — falta el ID del cliente Web |
| [3](tarea-3.md) | Gemini interpreta el texto + lógica de crédito | **B** | 🔨 Código listo — falta la clave de Gemini |
| [4](tarea-4.md) | Guardado en Google Sheets + ID único | **C** | ⬜ Pendiente |
| [5](tarea-5.md) | Confirmación en pantalla e historial | **D** | ⬜ Pendiente |
| [6](tarea-6.md) | Historial de errores | — | ⬜ Pendiente |
| [7](tarea-7.md) | Recordatorios de pagos a crédito | **G** | ⬜ Pendiente |
| [8](tarea-8.md) | Modo offline y sincronización | **E** | ⬜ Pendiente |
| [9](tarea-9.md) | APK instalable e instructivo | — | ⬜ Pendiente |
| [10](tarea-10.md) | Una semana de uso real | **F** | ⬜ Pendiente |

## Mapa de dependencias

```
0 ─> 1 ─> 2 (A) ─> 3 (B) ─> 4 (C) ─┬─> 5 (D) ─> 6
                                    ├─> 7 (G)
                    (2+3+4) ────────┴─> 8 (E)
                                         │
              (A,B,C,D,E,G validadas) ──> 9 ─> 10 (F)
```

## Reglas que aplican a todo el desarrollo

1. **Ninguna tarea se aprueba aislada.** Si al validar se encuentra un error o se cambia
   el diseño, todas las tareas posteriores que dependan de ella se revisan de nuevo,
   informando explícitamente qué hubo que ajustar en cada una.
2. **Idempotencia obligatoria.** Cada registro lleva un identificador único generado al
   escribirlo, incluso sin conexión. Solo se marca como "sincronizado" tras confirmar que
   la fila quedó en Google Sheets. Al reintentar se verifica ese identificador para no
   duplicar filas.
3. **Bitácora de cambios.** Todo error corregido se anota en `BITACORA.md` (raíz del
   proyecto): qué se cambió, por qué, y qué otras tareas se revisaron como consecuencia.
4. **No se avanza sobre una dependencia rota.** Se corrige primero, se revalida, y solo
   entonces se retoman las tareas dependientes.

## Decisiones tomadas

| Fecha | Decisión |
|---|---|
| 2026-09-09 | Se aprueba la conversión del proyecto a Android (Tarea 0) |
| 2026-09-09 | Se agrega la columna 14 `ID de registro` a la hoja — ver `BITACORA.md` #001 |

## Puntos que requieren decisión antes de avanzar

| Dónde | Asunto |
|---|---|
| [Tarea 0](tarea-0.md) | Versión de Android del celular de la dueña (por defecto: Android 8+) |

## Validaciones que dependen del calendario, no del código

- **Validación A** (Tarea 2): la parte de "la sesión sigue viva a los 7 días" solo se
  puede comprobar cuando pasen esos 7 días.
- **Validación F** (Tarea 10): exige una semana completa de uso real.

Ninguna de las dos se puede cerrar el mismo día en que se construye.
