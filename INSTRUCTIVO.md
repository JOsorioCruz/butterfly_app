# Butterfly 🦋 — Guía de uso

Esta guía está escrita para usarse sin saber nada de tecnología.
Si algo no coincide con lo que ves en tu celular, avisa: la guía está mal, no tú.

---

## 1. Cómo instalar la aplicación

Butterfly no está en la Play Store, así que se instala con un archivo. Es normal que
el celular pregunte varias veces si estás segura: le estás dando permiso para instalar
algo que no viene de la tienda.

### Paso a paso

1. **Pasa el archivo `Butterfly.apk` al celular.** Por correo, por cable, o
   guardándolo en tu Drive y descargándolo desde el celular.

2. **Ábrelo.** Busca el archivo en *Archivos* o en *Descargas* y tócalo.

3. **El celular dirá que no puede instalar apps de origen desconocido.** Es lo esperado.
   Toca **«Configuración»** o **«Ajustes»** en ese mismo aviso.

4. **Activa el permiso.** Verás una pantalla con el nombre de la app desde donde abriste
   el archivo (Archivos, Chrome, Gmail…). Activa la opción, que según la marca del
   celular se llama:
   - **«Permitir desde esta fuente»**
   - **«Instalar apps desconocidas»**
   - **«Fuentes desconocidas»**

5. **Vuelve atrás** y toca **«Instalar»**.

6. Si aparece un aviso de **Play Protect** («¿Enviar app para analizar?» o «App no
   segura»), toca **«Instalar de todas formas»**. Google avisa así de cualquier app que
   no venga de su tienda; no significa que tenga nada malo.

7. Listo. Busca la mariposa 🦋 entre tus aplicaciones.

> **Después de instalar**, puedes volver a desactivar el permiso del paso 4 si te deja
> más tranquila. No afecta a Butterfly una vez instalada.

---

## 2. La primera vez que abres la app

1. Toca **«Entrar con Google»** y elige tu cuenta.

2. **Si aparece un aviso que dice «Google no verificó esta aplicación»:**
   toca **«Avanzado»** y luego **«Ir a Butterfly (no seguro)»**.

   Ese aviso sale porque la app es tuya y no está publicada en la tienda de Google.
   Solo aparece la primera vez.

3. Google te preguntará si permites que Butterfly vea y edite archivos de tu Drive.
   Acepta.

   **Importante:** Butterfly solo puede ver la hoja de cálculo que ella misma crea.
   No puede abrir, leer ni borrar nada más de tu Drive.

4. Ya está. No tendrás que volver a hacer esto.

---

## 3. Cómo registrar una venta

Escribe en el cuadro grande igual que le escribirías a alguien por WhatsApp, y toca
**«Guardar venta»**.

**Ejemplos que la app entiende bien:**

```
venta de blusa: maria, talla m, 50 mil, credito, abono 20 mil
le fie un vestido a luisa talla s en 120 mil
carolina compro un jean talla 10 y pago los 85 mil de una
blusa para diana 60k credito abono 30k
```

No importan las tildes, las mayúsculas ni el orden de las cosas.

### Los tres datos que no pueden faltar

**El nombre, el producto y el precio.** Si falta alguno, la app te dirá cuál y no
guardará nada, para no dejar una venta a medias en la hoja.

Lo que escribiste **no se borra**: corrige lo que falta y vuelve a tocar el botón.

### Cuándo es crédito y cuándo no

- Si escribes **crédito**, **abono**, **cuotas**, **fiado** o **«queda debiendo»**,
  la app lo anota como crédito y calcula sola cuánto falta por cobrar.
- Si no dices nada de eso, lo anota como **pago inmediato** y el saldo queda en $0.

---

## 4. Cómo saber que quedó guardada

Debajo del botón aparece un resumen así:

> ✅ Venta guardada: Maria - Blusa talla M - $50.000 - Crédito (abono $20.000, saldo $30.000)

Y más abajo, en **«Ventas recientes»**, verás las últimas con una marca:

| Marca | Qué significa |
|---|---|
| ✅ **Guardada** | Ya está en tu hoja de cálculo. |
| ⏳ **Pendiente de sincronizar** | Estabas sin internet. Está guardada en el celular y **se subirá sola**. |
| ⚠️ **Incompleta** | Faltaba un dato. **No** se guardó en la hoja. |

---

## 5. Cuando no hay internet

**Registra la venta igual.** No esperes a tener señal.

La app la guarda en el celular y la marca como ⏳ **Pendiente de sincronizar**. En
cuanto vuelva el internet la sube sola, sin que tengas que hacer nada — aunque hayas
cerrado la app o apagado el celular.

**Nunca se duplica.** Aunque la conexión se corte a mitad de la subida, la app revisa
si esa venta ya está en la hoja antes de escribirla otra vez.

---

## 6. Los recordatorios de cobro

Cuando registras una venta a crédito con fecha de pago, la app te avisa **ese día en
la mañana**.

Además, cada mañana revisa todas las deudas y te avisa de:
- las que ya se **vencieron**
- las que vencen en los **próximos 2 días**

Si hay varias clientas el mismo día, llega **un solo aviso** con todas, no uno por cada
una.

### El recordatorio se cancela solo

Cuando pongas el saldo en **0** en la hoja —sea desde la app o editándola a mano— el
recordatorio de esa clienta deja de sonar. No tienes que hacer nada más.

### ⚠️ Si los avisos no llegan

Algunos celulares (Xiaomi, Huawei, Oppo, Samsung) apagan las apps para ahorrar batería,
y eso silencia los recordatorios. Si notas que no llegan:

1. Entra a **Ajustes → Aplicaciones → Butterfly**.
2. Busca **«Batería»** y elige **«Sin restricciones»** o **«Permitir en segundo plano»**.
3. Si tu celular tiene **«Inicio automático»** o **«Autostart»**, actívalo también.

---

## 7. Tu hoja de cálculo

La app crea sola una hoja en tu Drive llamada **«Butterfly - Ventas»**.

**Puedes editarla a mano cuando quieras.** Corregir un nombre, cambiar un saldo, anotar
que ya te pagaron: nada de eso se pierde. La app **solo agrega filas nuevas al final** y
nunca borra ni cambia lo que tú escribiste.

La última columna se llama **«ID de registro»** y tiene un código raro. Es lo que usa la
app para no repetir ventas. **No la borres**; si te estorba, ocúltala.

### 🔒 Haz una copia una vez al mes

Todo depende de tu cuenta de Google: la hoja, la app y la inteligencia artificial. Si esa
cuenta se bloqueara, perderías las tres cosas a la vez.

Para tener un respaldo aparte: abre la hoja → **Archivo → Descargar → Excel**, y guarda
ese archivo fuera de esa cuenta (en el celular, en un correo distinto, donde sea).

---

## 8. Si algo sale mal

Arriba a la derecha hay un botón que dice **«Errores»**. Si aparece con un número —por
ejemplo **«Errores (2)»**— es que hubo ventas que **no se pudieron guardar**.

Entra ahí y verás qué pasó y el mensaje que habías escrito. **Nada se pierde.**

En cada una hay un botón **«Reintentar»**. Úsalo cuando ya tengas internet. Si funciona,
queda marcada ✅.

**Si dice que el texto no parecía una venta**, reintentar no sirve: escribe el mensaje de
nuevo incluyendo nombre, producto y precio.

---

## 9. Si se cierra la sesión

No debería pasar, pero si la app vuelve a pedirte iniciar sesión:

1. Toca **«Entrar con Google»**.
2. Elige **la misma cuenta de siempre** — es donde está tu hoja.
3. Si sale el aviso de «Google no verificó esta aplicación», haz lo mismo que la primera
   vez: **«Avanzado» → «Ir a Butterfly»**.

Para salir a propósito, el botón **«Salir»** está arriba a la derecha.

---

## 10. Para el futuro: agregar un dato nuevo

Si algún día quieres registrar algo más (el color de la prenda, cómo pagó, quién se lo
llevó), hacen falta **tres cambios**, y los tiene que hacer alguien con el proyecto:

1. **En la hoja:** agregar la columna nueva. Tiene que ir **antes** de la última columna
   («ID de registro»).
2. **En la app:** decirle a la inteligencia artificial que busque ese dato en el texto.
3. **En la app:** decirle en qué columna escribirlo.

Después hay que **generar un APK nuevo e instalarlo encima** del que ya tienes,
siguiendo el punto 1 de esta guía. No se pierde nada al hacerlo.

> **Recuerda:** la app **no se actualiza sola**, porque no está en la Play Store. Cada
> cambio necesita instalar el archivo nuevo a mano.

---

## Resumen de un vistazo

| Quiero… | Hago… |
|---|---|
| Registrar una venta | Escribo como un WhatsApp y toco «Guardar venta» |
| Ver si quedó guardada | Miro la marca ✅ en «Ventas recientes» |
| Registrar sin internet | Igual. Se sube sola después |
| Ver qué falló | Botón «Errores», arriba a la derecha |
| Corregir un dato | Edito la hoja a mano, sin miedo |
| Respaldar | Hoja → Archivo → Descargar → Excel, una vez al mes |
