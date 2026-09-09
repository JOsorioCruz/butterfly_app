package com.butterfly.app.datos

import com.butterfly.app.ia.ModalidadDePago
import com.butterfly.app.ia.VentaInterpretada
import org.json.JSONObject

/** En que quedo un intento de guardar una venta. */
enum class EstadoRegistro(val etiqueta: String, val emoji: String) {
    /** Confirmada en la Google Sheet. */
    GUARDADA("Guardada", "✅"),

    /** Guardada en el celular, esperando conexion para subirse (Tarea 8). */
    PENDIENTE("Pendiente de sincronizar", "⏳"),

    /** Faltaba un dato obligatorio: NO se creo ninguna fila en la hoja. */
    INCOMPLETA("Incompleta", "⚠️"),
}

/**
 * Una venta tal como queda guardada en el celular.
 *
 * Guarda los datos completos, no solo el resumen, porque la Tarea 8 necesitara poder
 * reintentar el envio de una venta pendiente sin volver a preguntarle a la IA.
 */
data class RegistroLocal(
    val id: String,
    val fecha: Long,
    val resumen: String,
    val estado: EstadoRegistro,
    val mensajeOriginal: String,
    val faltante: String? = null,
    val venta: VentaInterpretada? = null,
) {
    fun aJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("fecha", fecha)
        put("resumen", resumen)
        put("estado", estado.name)
        put("mensajeOriginal", mensajeOriginal)
        put("faltante", faltante ?: JSONObject.NULL)
        put("venta", venta?.let(::ventaAJson) ?: JSONObject.NULL)
    }

    companion object {
        fun deJson(json: JSONObject): RegistroLocal = RegistroLocal(
            id = json.getString("id"),
            fecha = json.getLong("fecha"),
            resumen = json.getString("resumen"),
            estado = runCatching { EstadoRegistro.valueOf(json.getString("estado")) }
                .getOrDefault(EstadoRegistro.PENDIENTE),
            mensajeOriginal = json.optString("mensajeOriginal"),
            faltante = if (json.isNull("faltante")) null else json.optString("faltante"),
            venta = if (json.isNull("venta")) null else ventaDeJson(json.getJSONObject("venta")),
        )
    }
}

private fun ventaAJson(v: VentaInterpretada): JSONObject = JSONObject().apply {
    put("nombreCliente", v.nombreCliente ?: JSONObject.NULL)
    put("telefono", v.telefono ?: JSONObject.NULL)
    put("producto", v.producto ?: JSONObject.NULL)
    put("talla", v.talla ?: JSONObject.NULL)
    put("precio", v.precio ?: JSONObject.NULL)
    put("modalidad", v.modalidad.name)
    put("abono", v.abono)
    put("saldoPendiente", v.saldoPendiente)
    put("cuotas", v.cuotas ?: JSONObject.NULL)
    put("fechaProximoPago", v.fechaProximoPago ?: JSONObject.NULL)
    put("estadoEntrega", v.estadoEntrega ?: JSONObject.NULL)
    put("mensajeOriginal", v.mensajeOriginal)
}

private fun ventaDeJson(j: JSONObject) = VentaInterpretada(
    nombreCliente = j.textoONulo("nombreCliente"),
    telefono = j.textoONulo("telefono"),
    producto = j.textoONulo("producto"),
    talla = j.textoONulo("talla"),
    precio = if (j.isNull("precio")) null else j.getLong("precio"),
    modalidad = runCatching { ModalidadDePago.valueOf(j.getString("modalidad")) }
        .getOrDefault(ModalidadDePago.INMEDIATO),
    abono = j.optLong("abono"),
    saldoPendiente = j.optLong("saldoPendiente"),
    cuotas = if (j.isNull("cuotas")) null else j.getInt("cuotas"),
    fechaProximoPago = j.textoONulo("fechaProximoPago"),
    estadoEntrega = j.textoONulo("estadoEntrega"),
    mensajeOriginal = j.optString("mensajeOriginal"),
)

private fun JSONObject.textoONulo(clave: String): String? =
    if (isNull(clave)) null else optString(clave).takeIf { it.isNotBlank() }
