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

/** Cuantas ventas se conservan en el celular. Mas que suficiente para "reciente". */
private const val MAXIMO = 200

/**
 * El historial de ventas guardado en el celular, en un archivo JSON.
 *
 * Se eligio un archivo simple en vez de una base de datos (Room) a proposito: aqui hay
 * una sola lista de unas pocas ventas al dia, sin consultas ni relaciones. Room habria
 * traido un generador de codigo, mas dependencias y migraciones que mantener, a cambio
 * de nada util en este caso. La prioridad del proyecto es que el codigo sea simple.
 *
 * La Tarea 8 reutiliza este mismo almacen para la cola de pendientes: por eso cada
 * registro guarda la venta completa y no solo su resumen.
 */
class HistorialLocal(context: Context) {

    private val archivo = File(context.filesDir, "historial.json")
    private val candado = Mutex()

    suspend fun leer(): List<RegistroLocal> = withContext(Dispatchers.IO) {
        candado.withLock { leerDelDisco() }
    }

    /** Agrega un registro nuevo al principio (lo mas reciente primero). */
    suspend fun agregar(registro: RegistroLocal): List<RegistroLocal> =
        withContext(Dispatchers.IO) {
            candado.withLock {
                val lista = (listOf(registro) + leerDelDisco()).take(MAXIMO)
                escribirEnDisco(lista)
                lista
            }
        }

    /** Cambia el estado de un registro, por ejemplo al sincronizarse (Tarea 8). */
    suspend fun actualizarEstado(id: String, nuevo: EstadoRegistro): List<RegistroLocal> =
        withContext(Dispatchers.IO) {
            candado.withLock {
                val lista = leerDelDisco().map {
                    if (it.id == id) it.copy(estado = nuevo) else it
                }
                escribirEnDisco(lista)
                lista
            }
        }

    /** Sustituye un registro entero por otro con el mismo identificador. */
    suspend fun reemplazar(registro: RegistroLocal): List<RegistroLocal> =
        withContext(Dispatchers.IO) {
            candado.withLock {
                val lista = leerDelDisco().map { if (it.id == registro.id) registro else it }
                escribirEnDisco(lista)
                lista
            }
        }

    /** Las ventas que todavia no llegaron a la hoja. La Tarea 8 las reintenta. */
    suspend fun pendientes(): List<RegistroLocal> =
        leer().filter { it.estado == EstadoRegistro.PENDIENTE }

    private fun leerDelDisco(): List<RegistroLocal> {
        val array = leerArrayJson(archivo)
        return (0 until array.length()).mapNotNull { i ->
            runCatching { RegistroLocal.deJson(array.getJSONObject(i)) }.getOrNull()
        }
    }

    private fun escribirEnDisco(lista: List<RegistroLocal>) {
        val array = JSONArray()
        lista.forEach { array.put(it.aJson()) }
        escribirArrayJson(archivo, array)
    }
}
