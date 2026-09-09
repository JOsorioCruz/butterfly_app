package com.butterfly.app.ui

/**
 * Lo que la pantalla principal tiene que mostrar despues de pulsar "Guardar venta".
 *
 * Junta en un solo estado las dos etapas (interpretar con la IA y escribir en la hoja)
 * porque para la dueña son una sola accion: escribio y pulso un boton.
 */
sealed interface EstadoDeGuardado {
    data object Inactivo : EstadoDeGuardado
    data object Trabajando : EstadoDeGuardado

    /** Guardada de verdad en la hoja. */
    data class Guardada(val resumen: String) : EstadoDeGuardado

    /** Ya estaba en la hoja de un intento anterior: la Regla 2 evito el duplicado. */
    data class YaEstaba(val resumen: String) : EstadoDeGuardado

    /** Falta un dato obligatorio. NO se creo ninguna fila. */
    data class Incompleta(val faltan: String) : EstadoDeGuardado

    /** El texto no habla de una venta. NO se creo ninguna fila. */
    data object NoEsUnaVenta : EstadoDeGuardado

    /** Sin conexion: la venta ira a la cola pendiente cuando exista (Tarea 8). */
    data class SinConexion(val mensaje: String) : EstadoDeGuardado

    /** Cualquier otro fallo. NO se creo ninguna fila (punto 9). */
    data class Error(val mensaje: String) : EstadoDeGuardado
}
