package com.butterfly.app.datos

import org.json.JSONObject

/** De donde vino el problema. Sirve para saber si tiene sentido reintentar. */
enum class TipoDeIncidente(val etiqueta: String) {
    /** La IA no respondio, fallo, o se agoto la cuota gratuita. */
    IA("La IA no pudo leer el mensaje"),

    /** Google Sheets no respondio o rechazo la escritura. */
    HOJA("No se pudo escribir en la hoja"),

    /** El texto no hablaba de una venta. Reintentar tal cual no sirve de nada. */
    NO_ES_VENTA("El texto no parecía una venta"),
}

/**
 * Un problema que impidio registrar una venta.
 *
 * Lo importante: **cuando se crea un incidente, NO se creo ninguna fila en la hoja**.
 * Es la garantia del punto 9. Nada se pierde en silencio: el mensaje original queda
 * guardado aqui para poder reintentarlo.
 *
 * El [id] es el mismo identificador de registro que se habria usado para la fila. Al
 * reintentar se reutiliza, de modo que si la escritura si llego a Google pero la
 * respuesta se perdio, el reintento lo detecta y no duplica (Regla 2).
 */
data class Incidente(
    val id: String,
    val fecha: Long,
    val textoOriginal: String,
    val queFallo: String,
    val tipo: TipoDeIncidente,
    val resuelto: Boolean = false,
) {
    fun aJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("fecha", fecha)
        put("textoOriginal", textoOriginal)
        put("queFallo", queFallo)
        put("tipo", tipo.name)
        put("resuelto", resuelto)
    }

    companion object {
        fun deJson(json: JSONObject) = Incidente(
            id = json.getString("id"),
            fecha = json.getLong("fecha"),
            textoOriginal = json.optString("textoOriginal"),
            queFallo = json.optString("queFallo"),
            tipo = runCatching { TipoDeIncidente.valueOf(json.getString("tipo")) }
                .getOrDefault(TipoDeIncidente.HOJA),
            resuelto = json.optBoolean("resuelto", false),
        )
    }
}
