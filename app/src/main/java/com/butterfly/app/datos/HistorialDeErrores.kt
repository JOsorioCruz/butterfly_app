package com.butterfly.app.datos

import android.content.Context
import com.butterfly.app.util.escribirArrayJson
import com.butterfly.app.util.leerArrayJson
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

private const val MAXIMO = 100

/**
 * Los incidentes que impidieron registrar una venta (punto 9 de la especificacion).
 *
 * Vive en un archivo aparte del historial de ventas a proposito: son dos cosas
 * distintas para la dueña, y mezclarlas haria que un error enterrara las ventas buenas.
 */
class HistorialDeErrores(context: Context) {

    private val archivo = File(context.filesDir, "errores.json")
    private val candado = Mutex()

    suspend fun leer(): List<Incidente> = withContext(Dispatchers.IO) {
        candado.withLock { leerDelDisco() }
    }

    suspend fun agregar(incidente: Incidente) = withContext(Dispatchers.IO) {
        candado.withLock {
            escribirEnDisco((listOf(incidente) + leerDelDisco()).take(MAXIMO))
        }
    }

    /** Marca un incidente como resuelto cuando el reintento si funciono. */
    suspend fun marcarResuelto(id: String) = withContext(Dispatchers.IO) {
        candado.withLock {
            escribirEnDisco(
                leerDelDisco().map { if (it.id == id) it.copy(resuelto = true) else it },
            )
        }
    }

    suspend fun borrarTodo() = withContext(Dispatchers.IO) {
        candado.withLock { escribirEnDisco(emptyList()) }
    }

    private fun leerDelDisco(): List<Incidente> {
        val array = leerArrayJson(archivo)
        return (0 until array.length()).mapNotNull { i ->
            runCatching { Incidente.deJson(array.getJSONObject(i)) }.getOrNull()
        }
    }

    private fun escribirEnDisco(lista: List<Incidente>) {
        val array = JSONArray()
        lista.forEach { array.put(it.aJson()) }
        escribirArrayJson(archivo, array)
    }
}
