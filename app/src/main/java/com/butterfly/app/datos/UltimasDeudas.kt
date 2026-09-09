package com.butterfly.app.datos

import android.content.Context
import com.butterfly.app.util.escribirArrayJson
import com.butterfly.app.util.leerArrayJson
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Copia local de las deudas leidas de la hoja la ultima vez.
 *
 * Es el respaldo del barrido diario: si al amanecer no hay internet o no se puede
 * renovar el permiso en silencio, se avisa con esta copia en vez de no avisar nada.
 * Puede estar algo desactualizada, pero un aviso de mas es mejor que olvidar un cobro.
 */
class UltimasDeudas(context: Context) {

    private val archivo = File(context.filesDir, "deudas.json")

    suspend fun guardar(deudas: List<VentaPendienteDePago>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        deudas.forEach { deuda ->
            array.put(
                JSONObject().apply {
                    put("id", deuda.id)
                    put("nombre", deuda.nombre)
                    put("saldo", deuda.saldo)
                    put("fechaProximoPago", deuda.fechaProximoPago ?: JSONObject.NULL)
                },
            )
        }
        escribirArrayJson(archivo, array)
    }

    suspend fun leer(): List<VentaPendienteDePago> = withContext(Dispatchers.IO) {
        val array = leerArrayJson(archivo)
        (0 until array.length()).mapNotNull { i ->
            runCatching {
                val o = array.getJSONObject(i)
                VentaPendienteDePago(
                    id = o.getString("id"),
                    nombre = o.optString("nombre"),
                    saldo = o.optLong("saldo"),
                    fechaProximoPago =
                        if (o.isNull("fechaProximoPago")) null else o.optString("fechaProximoPago"),
                )
            }.getOrNull()
        }
    }
}
