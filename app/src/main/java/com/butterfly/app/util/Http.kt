package com.butterfly.app.util

import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Lo que devuelve una llamada HTTP: el codigo y el cuerpo, sin interpretar. */
data class RespuestaHttp(val codigo: Int, val cuerpo: String) {
    val exitosa: Boolean get() = codigo in 200..299
}

/**
 * Una sola llamada HTTP, sin libreria externa.
 *
 * La app hace pocas llamadas (Gemini, crear la hoja, leer una columna, agregar una
 * fila), asi que no compensa añadir una libreria de red: basta con lo que Android ya
 * trae. Aqui se centraliza para no repetir el mismo bloque en cada sitio.
 *
 * Ojo: es bloqueante. Hay que llamarla desde Dispatchers.IO.
 */
fun peticionHttp(
    url: String,
    metodo: String = "GET",
    cabeceras: Map<String, String> = emptyMap(),
    cuerpoJson: String? = null,
    tiempoDeEspera: Int = 30_000,
): RespuestaHttp {
    val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = metodo
        connectTimeout = 20_000
        readTimeout = tiempoDeEspera
        cabeceras.forEach { (clave, valor) -> setRequestProperty(clave, valor) }
        if (cuerpoJson != null) {
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
    }

    return try {
        cuerpoJson?.let { conexion.outputStream.use { flujo -> flujo.write(it.toByteArray()) } }
        val codigo = conexion.responseCode
        val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
        val cuerpo = flujo?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        RespuestaHttp(codigo, cuerpo)
    } catch (e: IOException) {
        // Sin conexion, DNS caido, tiempo agotado... Se distingue del error del
        // servidor con el codigo 0, para que la Tarea 8 sepa que hay que encolar.
        RespuestaHttp(0, e.message ?: "Sin conexion")
    } finally {
        conexion.disconnect()
    }
}
