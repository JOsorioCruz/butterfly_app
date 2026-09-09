# Validación F — Seguimiento de la semana de uso real

Este archivo se llena **durante** la semana. Es la prueba de la Validación F.

- **Fecha de inicio:** ____________
- **Fecha de cierre prevista:** ____________ (7 días después)
- **Celular usado:** ____________ (marca, modelo, versión de Android)

---

## Antes del día 1 — nada de esto es opcional

La semana no empieza hasta que las 10 casillas estén marcadas. Si una falla, se corrige
y **la semana vuelve a empezar** (Regla 4).

### Configuración

- [ ] Proyecto creado en Google Cloud, con **Sheets API** y **Drive API** habilitadas
- [ ] Pantalla de consentimiento **publicada en «En producción»**, no en «Prueba»
- [ ] Cliente OAuth **Android** con la huella de **release**:
      `C8:5E:54:3A:75:EF:47:9C:C7:55:C8:33:6F:CE:CB:19:4D:AD:79:74`
- [ ] Cliente OAuth **Web** creado, y su ID puesto en `local.properties`
- [ ] Clave de **Gemini** puesta en `local.properties`
- [ ] APK regenerado con `./gradlew assembleRelease` **después** de poner las claves

### En el celular de la dueña

- [ ] APK instalado siguiendo el `INSTRUCTIVO.md`, **por ella misma**
- [ ] Inició sesión con su cuenta y concedió el permiso
- [ ] Permiso de **notificaciones** concedido
- [ ] Butterfly **excluida del ahorro de batería** (ver punto 6 del instructivo)

### Validaciones que deben estar aprobadas antes

| | Validación | Aprobada |
|---|---|---|
| A | Inicio de sesión (parte inmediata) | ☐ |
| B | La IA interpreta los 15 ejemplos | ☐ |
| C | Las filas se agregan sin dañar la hoja | ☐ |
| D | Confirmación en pantalla e incompletos | ☐ |
| E | Sin conexión queda pendiente y se sincroniza sin duplicar | ☐ |
| G | Los recordatorios llegan, se agrupan y se cancelan | ☐ |

---

## Registro diario

Una línea por día. Lo importante es anotar **lo que pasó**, no lo que debería pasar.

| Día | Fecha | Ventas escritas | Llegaron a la hoja | Errores | Recordatorios | Notas |
|---|---|---|---|---|---|---|
| 1 | | | | | | |
| 2 | | | | | | |
| 3 | | | | | | |
| 4 | | | | | | |
| 5 | | | | | | |
| 6 | | | | | | |
| 7 | | | | | | |

**Cómo llenar «Llegaron a la hoja»:** contar las filas nuevas en la Google Sheet y
compararlas con las ventas escritas ese día. Si no coinciden, **eso es un fallo grave**
aunque la app no haya dado ningún error.

---

## Lo que hay que vigilar cada día

- [ ] ¿Se perdió alguna venta? *(la pregunta más importante de todas)*
- [ ] ¿Alguna fila salió duplicada?
- [ ] ¿La IA entendió mal algún mensaje? → anotar el texto exacto abajo
- [ ] ¿Se cerró sola la sesión de Google?
- [ ] ¿Llegaron los recordatorios el día que tocaba?
- [ ] ¿Apareció algo en el historial de errores?

### Mensajes que la IA entendió mal

Anotar aquí el texto tal cual se escribió y qué entendió mal. Estos casos son los que
más valen: son mensajes reales, no inventados para una prueba.

| Lo que escribió | Lo que la app entendió | Lo correcto |
|---|---|---|
| | | |

---

## Cierre de la semana

### Se aprueba solo si las cuatro son ciertas

- [ ] **No se perdió ningún registro** — todas las ventas escritas están en la hoja
- [ ] **No hubo filas duplicadas**
- [ ] **La sesión de Google no se cerró sola** *(esto además cierra la parte diferida de
      la Validación A)*
- [ ] **Los recordatorios llegaron** cuando había pagos que cobrar

### Si algo falla — Regla 1 y Regla 4

Un fallo aquí no es «un detalle final». Hay que:

1. Identificar **de qué tarea** viene el problema.
2. Corregir esa tarea.
3. Revisar **todas las tareas posteriores que dependían de ella**, e informar
   explícitamente qué hubo que ajustar en cada una.
4. Anotarlo todo en `BITACORA.md`.
5. **Volver a empezar la semana.** No se sigue usando la app «provisionalmente» sobre
   una dependencia rota.

### Veredicto

- **Resultado:** ☐ Aprobada  ☐ No aprobada
- **Fecha:** ____________
- **Si no se aprobó, qué falló y qué tareas se revisaron:**

```


```
