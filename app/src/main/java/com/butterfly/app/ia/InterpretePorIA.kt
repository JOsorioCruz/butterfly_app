package com.butterfly.app.ia

import com.butterfly.app.BuildConfig
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Convierte el texto libre que escribe la dueña en datos estructurados, usando la API
 * de Google Gemini (Google AI Studio, nivel gratuito, sin tarjeta ni facturacion).
 *
 * No se usa ninguna libreria HTTP extra: con dos llamadas REST en toda la app,
 * HttpURLConnection y el org.json que ya trae Android son suficientes. Menos
 * dependencias que mantener y un APK mas pequeño.
 */
class InterpretePorIA {

    val faltaClave: Boolean get() = BuildConfig.GEMINI_API_KEY.isBlank()

    suspend fun interpretar(texto: String): ResultadoInterpretacion =
        withContext(Dispatchers.IO) {
            if (texto.isBlank()) return@withContext ResultadoInterpretacion.NoEsUnaVenta
            if (faltaClave) {
                return@withContext ResultadoInterpretacion.Fallo(
                    "Falta la clave de Gemini. Se configura en local.properties.",
                )
            }
            try {
                val respuesta = llamarAGemini(texto)
                leerRespuesta(respuesta, texto)
            } catch (e: Exception) {
                ResultadoInterpretacion.Fallo(
                    e.message ?: "No se pudo hablar con la IA. Revisa la conexion.",
                )
            }
        }

    private fun llamarAGemini(texto: String): String {
        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/" +
                "${BuildConfig.GEMINI_MODELO}:generateContent",
        )
        val conexion = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 40_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
        }

        conexion.outputStream.use { it.write(cuerpoDeLaPeticion(texto).toByteArray()) }

        val codigo = conexion.responseCode
        val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
        val cuerpo = flujo?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        conexion.disconnect()

        if (codigo !in 200..299) {
            throw IllegalStateException(mensajeDeErrorLegible(codigo, cuerpo))
        }
        return cuerpo
    }

    /** Traduce los errores de la API a algo que se entienda sin ser tecnico. */
    private fun mensajeDeErrorLegible(codigo: Int, cuerpo: String): String = when (codigo) {
        400 -> "La IA rechazo la peticion. Puede que el nombre del modelo sea incorrecto."
        401, 403 -> "La clave de Gemini no es valida o no tiene permiso."
        404 -> "El modelo '${BuildConfig.GEMINI_MODELO}' no existe. Cambialo en local.properties."
        429 -> "Se alcanzo el limite gratuito de la IA por ahora. Intenta mas tarde."
        in 500..599 -> "La IA de Google no esta respondiendo. Intenta mas tarde."
        else -> "La IA respondio con un error ($codigo). ${cuerpo.take(200)}"
    }

    private fun cuerpoDeLaPeticion(texto: String): String {
        val esquema = JSONObject()
            .put("type", "OBJECT")
            .put(
                "properties",
                JSONObject()
                    .put("es_venta", campo("BOOLEAN"))
                    .put("nombre_cliente", campo("STRING", nulo = true))
                    .put("telefono", campo("STRING", nulo = true))
                    .put("producto", campo("STRING", nulo = true))
                    .put("talla", campo("STRING", nulo = true))
                    .put("precio", campo("INTEGER", nulo = true))
                    .put("modalidad_pago", campoEnum())
                    .put("abono", campo("INTEGER", nulo = true))
                    .put("cuotas", campo("INTEGER", nulo = true))
                    .put("fecha_proximo_pago", campo("STRING", nulo = true))
                    .put("estado_entrega", campo("STRING", nulo = true)),
            )
            .put("required", JSONArray().put("es_venta").put("modalidad_pago"))

        return JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", texto))),
                ),
            )
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", INSTRUCCIONES)),
                ),
            )
            .put(
                "generationConfig",
                JSONObject()
                    // Temperatura 0: ante el mismo texto, siempre la misma lectura.
                    .put("temperature", 0)
                    .put("responseMimeType", "application/json")
                    .put("responseSchema", esquema),
            )
            .toString()
    }

    private fun campo(tipo: String, nulo: Boolean = false) =
        JSONObject().put("type", tipo).apply { if (nulo) put("nullable", true) }

    private fun campoEnum() = JSONObject()
        .put("type", "STRING")
        .put("enum", JSONArray().put("CREDITO").put("INMEDIATO"))

    private fun leerRespuesta(json: String, textoOriginal: String): ResultadoInterpretacion {
        val raiz = JSONObject(json)
        val candidatos = raiz.optJSONArray("candidates")
            ?: return ResultadoInterpretacion.Fallo("La IA respondio vacio.")
        if (candidatos.length() == 0) {
            return ResultadoInterpretacion.Fallo("La IA respondio vacio.")
        }
        val texto = candidatos.getJSONObject(0)
            .optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            ?: return ResultadoInterpretacion.Fallo("La IA respondio en un formato inesperado.")

        val datos = JSONObject(texto)
        if (!datos.optBoolean("es_venta", false)) {
            return ResultadoInterpretacion.NoEsUnaVenta
        }

        val venta = VentaInterpretada.crear(
            nombreCliente = datos.textoONulo("nombre_cliente"),
            telefono = datos.textoONulo("telefono"),
            producto = datos.textoONulo("producto"),
            talla = datos.textoONulo("talla"),
            precio = datos.numeroONulo("precio"),
            modalidad = if (datos.optString("modalidad_pago") == "CREDITO") {
                ModalidadDePago.CREDITO
            } else {
                ModalidadDePago.INMEDIATO
            },
            abono = datos.numeroONulo("abono"),
            cuotas = datos.numeroONulo("cuotas")?.toInt(),
            fechaProximoPago = datos.textoONulo("fecha_proximo_pago"),
            estadoEntrega = datos.textoONulo("estado_entrega"),
            mensajeOriginal = textoOriginal,
        )

        return if (venta.estaCompleta) {
            ResultadoInterpretacion.Completa(venta)
        } else {
            ResultadoInterpretacion.Incompleta(venta)
        }
    }

    private fun JSONObject.textoONulo(clave: String): String? =
        if (isNull(clave)) null else optString(clave).takeIf { it.isNotBlank() }

    private fun JSONObject.numeroONulo(clave: String): Long? =
        if (isNull(clave)) null else optLong(clave, Long.MIN_VALUE).takeIf { it != Long.MIN_VALUE }

    private companion object {
        val INSTRUCCIONES = """
            Extraes datos de ventas de ropa a partir de mensajes escritos a mano por la
            dueña de un negocio en Colombia. Los mensajes son informales, sin tildes ni
            puntuacion, como un WhatsApp rapido.

            REGLAS:

            1. NO INVENTES NADA. Si un dato no aparece en el texto, devuelvelo como null.
               Es preferible dejarlo vacio a adivinar.

            2. Precios en pesos colombianos, como numero entero sin puntos ni simbolos:
               "50 mil" -> 50000 | "50.000" -> 50000 | "50k" -> 50000
               "cincuenta mil" -> 50000 | "120" en contexto de ropa -> 120000
               Si el numero es ambiguo, prefiere la lectura en miles.

            3. modalidad_pago:
               - "CREDITO" si menciona credito, abono, cuotas, fiado, "queda debiendo",
                 "me paga despues" o cualquier pago parcial.
               - "INMEDIATO" en cualquier otro caso, incluso si no dice nada de pago.

            4. abono: solo el monto ya entregado. Si es pago inmediato, devuelve null.

            5. cuotas y fecha_proximo_pago: solo si el texto los menciona. Si no, null.
               La fecha en formato AAAA-MM-DD.

            6. talla: tal como aparezca (M, 10, S, unica...).

            7. telefono: solo si aparece un numero de telefono. No confundas con precios.

            8. estado_entrega: solo si dice algo de entrega (entregado, pendiente,
               "se lo llevo mañana"...).

            9. es_venta: false si el texto no habla de una venta de ropa (un saludo, una
               nota suelta, algo sin relacion). En ese caso los demas campos van null.

            NO calcules saldos ni restas. Solo extrae lo que el texto dice.
        """.trimIndent()
    }
}
