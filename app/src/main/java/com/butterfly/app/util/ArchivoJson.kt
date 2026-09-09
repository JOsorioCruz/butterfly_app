package com.butterfly.app.util

import java.io.File
import org.json.JSONArray

/**
 * Lee una lista JSON de un archivo. Si no existe o esta dañado, devuelve una lista
 * vacia en vez de reventar: perder el historial es malo, pero que la app no abra es peor.
 */
fun leerArrayJson(archivo: File): JSONArray =
    if (!archivo.exists()) {
        JSONArray()
    } else {
        runCatching { JSONArray(archivo.readText()) }.getOrDefault(JSONArray())
    }

/**
 * Escribe una lista JSON de forma segura.
 *
 * Escribe primero a un archivo temporal y luego lo renombra. Si el celular se apaga a
 * mitad de la escritura, el contenido anterior sigue intacto en vez de quedar a medias.
 */
fun escribirArrayJson(archivo: File, contenido: JSONArray) {
    val temporal = File(archivo.parentFile, archivo.name + ".tmp")
    temporal.writeText(contenido.toString())
    temporal.renameTo(archivo)
}
