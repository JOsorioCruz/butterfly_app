package com.butterfly.app.datos

import android.content.Context
import com.butterfly.app.ia.ModalidadDePago
import com.butterfly.app.ia.VentaInterpretada
import com.butterfly.app.util.peticionHttp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** El nombre con el que la dueña vera la hoja en su Drive. */
private const val NOMBRE_DE_LA_HOJA = "Butterfly - Ventas"

/**
 * Las 14 columnas de la hoja, en orden.
 *
 * La ultima, "ID de registro", no estaba en la especificacion original: se agrego el
 * 2026-09-09 porque la Regla 2 (idempotencia) exige poder preguntarle a la hoja si un
 * registro ya existe. Sin ella, un envio interrumpido a mitad de camino duplicaria la
 * fila al reintentar. Ver BITACORA.md #001.
 */
val COLUMNAS = listOf(
    "Fecha",
    "Nombre del cliente",
    "Teléfono",
    "Producto",
    "Talla",
    "Precio",
    "Modalidad de pago",
    "Abono recibido",
    "Saldo pendiente",
    "Cuotas",
    "Fecha próximo pago",
    "Estado de entrega",
    "Mensaje original",
    "ID de registro",
)

/** La columna donde vive el identificador unico: la 14, o sea la "N". */
private const val COLUMNA_DEL_ID = "N"

sealed interface ResultadoGuardado {
    data object Guardada : ResultadoGuardado

    /**
     * El identificador ya estaba en la hoja: la fila se habia guardado en un intento
     * anterior. No se vuelve a escribir. Esto es la Regla 2 funcionando.
     */
    data object YaEstaba : ResultadoGuardado

    /** Fallo de red. La venta debe quedar en la cola pendiente (Tarea 8). */
    data class SinConexion(val mensaje: String) : ResultadoGuardado

    data class Fallo(val mensaje: String) : ResultadoGuardado
}

/**
 * Escribe las ventas en la Google Sheet de la dueña.
 *
 * Dos garantias que no se pueden romper:
 *  1. **Solo agrega filas al final.** Nunca sobrescribe ni borra. La dueña sigue
 *     editando la hoja a mano y sus cambios no se pierden (punto 5).
 *  2. **Nunca duplica.** Antes de escribir comprueba si el identificador ya esta en la
 *     hoja (Regla 2).
 */
class HojaDeVentas(context: Context) {

    private val preferencias =
        context.getSharedPreferences("butterfly_hoja", Context.MODE_PRIVATE)

    var idDeLaHoja: String?
        get() = preferencias.getString("id_hoja", null)
        private set(valor) {
            preferencias.edit().putString("id_hoja", valor).apply()
        }

    /** Enlace para abrir la hoja desde el celular. */
    fun enlace(): String? = idDeLaHoja?.let { "https://docs.google.com/spreadsheets/d/$it" }

    suspend fun guardar(
        venta: VentaInterpretada,
        idDeRegistro: String,
        token: String,
    ): ResultadoGuardado = withContext(Dispatchers.IO) {
        try {
            val hoja = obtenerOCrearHoja(token)
                ?: return@withContext ResultadoGuardado.Fallo(
                    "No se pudo abrir ni crear la hoja de cálculo.",
                )

            when (yaEstaEnLaHoja(hoja, idDeRegistro, token)) {
                true -> return@withContext ResultadoGuardado.YaEstaba
                null -> return@withContext ResultadoGuardado.SinConexion(
                    "No se pudo comprobar si la venta ya estaba guardada.",
                )
                false -> Unit
            }

            agregarFila(hoja, venta, idDeRegistro, token)
        } catch (e: Exception) {
            ResultadoGuardado.Fallo(e.message ?: "No se pudo guardar en la hoja.")
        }
    }

    /**
     * Lee de la hoja las ventas a credito que todavia deben algo.
     *
     * Se lee de la HOJA y no del archivo local a proposito: la dueña edita la hoja a
     * mano desde su celular, asi que un saldo saldado solo existe alli. Si los
     * recordatorios se basaran en los datos del telefono, seguirian avisando de deudas
     * ya cobradas.
     */
    suspend fun ventasConSaldoPendiente(token: String): List<VentaPendienteDePago>? =
        withContext(Dispatchers.IO) {
            val hoja = idDeLaHoja ?: return@withContext emptyList()
            val respuesta = peticionHttp(
                url = "https://sheets.googleapis.com/v4/spreadsheets/$hoja/values/A2:N",
                cabeceras = mapOf("Authorization" to "Bearer $token"),
            )
            if (!respuesta.exitosa) return@withContext null

            val filas = JSONObject(respuesta.cuerpo).optJSONArray("values")
                ?: return@withContext emptyList()

            (0 until filas.length()).mapNotNull { i ->
                val fila = filas.optJSONArray(i) ?: return@mapNotNull null
                fun celda(indice: Int) =
                    if (fila.length() > indice) fila.optString(indice).trim() else ""

                val saldo = celda(8).filter { it.isDigit() }.toLongOrNull() ?: 0L
                // Saldo en cero = deuda saldada. No genera recordatorio.
                if (saldo <= 0L) return@mapNotNull null

                VentaPendienteDePago(
                    id = celda(13).ifBlank { "fila-$i" },
                    nombre = celda(1).ifBlank { "Sin nombre" },
                    saldo = saldo,
                    fechaProximoPago = celda(10).ifBlank { null },
                )
            }
        }

    // ---------- Encontrar o crear la hoja ----------

    private fun obtenerOCrearHoja(token: String): String? {
        idDeLaHoja?.let { return it }

        // Con el permiso drive.file, esta busqueda solo devuelve archivos creados por
        // esta misma app: sirve para reencontrar la hoja si se reinstalo la app, sin
        // ver nada mas del Drive de la dueña.
        buscarHojaExistente(token)?.let {
            idDeLaHoja = it
            return it
        }

        return crearHoja(token)?.also { idDeLaHoja = it }
    }

    private fun buscarHojaExistente(token: String): String? {
        val consulta = "name='$NOMBRE_DE_LA_HOJA' and " +
            "mimeType='application/vnd.google-apps.spreadsheet' and trashed=false"
        val respuesta = peticionHttp(
            url = "https://www.googleapis.com/drive/v3/files" +
                "?q=${java.net.URLEncoder.encode(consulta, "UTF-8")}&fields=files(id)",
            cabeceras = mapOf("Authorization" to "Bearer $token"),
        )
        if (!respuesta.exitosa) return null
        val archivos = JSONObject(respuesta.cuerpo).optJSONArray("files") ?: return null
        return if (archivos.length() > 0) archivos.getJSONObject(0).optString("id") else null
    }

    private fun crearHoja(token: String): String? {
        val cuerpo = JSONObject().put(
            "properties",
            JSONObject().put("title", NOMBRE_DE_LA_HOJA),
        )
        val respuesta = peticionHttp(
            url = "https://sheets.googleapis.com/v4/spreadsheets",
            metodo = "POST",
            cabeceras = mapOf("Authorization" to "Bearer $token"),
            cuerpoJson = cuerpo.toString(),
        )
        if (!respuesta.exitosa) return null
        val id = JSONObject(respuesta.cuerpo).optString("spreadsheetId").takeIf { it.isNotBlank() }
        id?.let { escribirEncabezados(it, token) }
        return id
    }

    private fun escribirEncabezados(hoja: String, token: String) {
        val valores = JSONArray().put(JSONArray(COLUMNAS))
        peticionHttp(
            url = "https://sheets.googleapis.com/v4/spreadsheets/$hoja/values/A1" +
                "?valueInputOption=RAW",
            metodo = "PUT",
            cabeceras = mapOf("Authorization" to "Bearer $token"),
            cuerpoJson = JSONObject().put("values", valores).toString(),
        )
    }

    // ---------- Regla 2: no duplicar ----------

    /** true si ya esta, false si no, null si no se pudo comprobar. */
    private fun yaEstaEnLaHoja(hoja: String, idDeRegistro: String, token: String): Boolean? {
        val respuesta = peticionHttp(
            url = "https://sheets.googleapis.com/v4/spreadsheets/$hoja/values/" +
                "$COLUMNA_DEL_ID:$COLUMNA_DEL_ID",
            cabeceras = mapOf("Authorization" to "Bearer $token"),
        )
        // Ante la duda NO se escribe: es preferible reintentar mas tarde a arriesgarse
        // a crear una fila duplicada.
        if (!respuesta.exitosa) return null

        val filas = JSONObject(respuesta.cuerpo).optJSONArray("values") ?: return false
        for (i in 0 until filas.length()) {
            val fila = filas.optJSONArray(i) ?: continue
            if (fila.length() > 0 && fila.optString(0) == idDeRegistro) return true
        }
        return false
    }

    // ---------- Escribir la fila ----------

    private fun agregarFila(
        hoja: String,
        venta: VentaInterpretada,
        idDeRegistro: String,
        token: String,
    ): ResultadoGuardado {
        val fila = JSONArray().apply {
            put(fechaDeHoy())
            put(venta.nombreCliente.orEmpty())
            put(venta.telefono.orEmpty())
            put(venta.producto.orEmpty())
            put(venta.talla.orEmpty())
            put(venta.precio ?: "")
            put(if (venta.modalidad == ModalidadDePago.CREDITO) "Crédito" else "Inmediato")
            put(venta.abono)
            put(venta.saldoPendiente)
            // Vacias si no se mencionaron. No se inventan (punto 4).
            put(venta.cuotas ?: "")
            put(venta.fechaProximoPago.orEmpty())
            put(venta.estadoEntrega.orEmpty())
            put(venta.mensajeOriginal)
            put(idDeRegistro)
        }

        val respuesta = peticionHttp(
            // insertDataOption=INSERT_ROWS agrega al final SIN tocar nada de lo que ya
            // hay. Es lo que garantiza que la hoja editada a mano no se dañe.
            url = "https://sheets.googleapis.com/v4/spreadsheets/$hoja/values/A:N:append" +
                "?valueInputOption=USER_ENTERED&insertDataOption=INSERT_ROWS",
            metodo = "POST",
            cabeceras = mapOf("Authorization" to "Bearer $token"),
            cuerpoJson = JSONObject().put("values", JSONArray().put(fila)).toString(),
        )

        return when {
            respuesta.exitosa -> ResultadoGuardado.Guardada
            respuesta.codigo == 0 -> ResultadoGuardado.SinConexion(respuesta.cuerpo)
            respuesta.codigo == 401 || respuesta.codigo == 403 ->
                ResultadoGuardado.Fallo("Se perdió el permiso para escribir en la hoja.")
            else -> ResultadoGuardado.Fallo(
                "Google Sheets respondió con un error (${respuesta.codigo}).",
            )
        }
    }

    private fun fechaDeHoy(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es", "CO")).format(Date())
}
