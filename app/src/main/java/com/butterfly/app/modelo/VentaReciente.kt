package com.butterfly.app.modelo

/**
 * Los tres estados en que puede quedar una venta en el historial reciente.
 *
 * - [GUARDADA]: confirmada en la Google Sheet.
 * - [PENDIENTE]: guardada en el celular, esperando conexion para subirse (Tarea 8).
 * - [INCOMPLETA]: falta un dato obligatorio (nombre, producto o precio) y por eso
 *   NO se creo ninguna fila. Ver Tarea 3.
 */
enum class EstadoVenta(val etiqueta: String, val emoji: String) {
    GUARDADA("Guardada", "✅"),
    PENDIENTE("Pendiente de sincronizar", "⏳"),
    INCOMPLETA("Incompleta", "⚠️"),
}

/**
 * Una linea del historial reciente de la pantalla principal.
 *
 * El [id] es el identificador unico exigido por la Regla 2 (idempotencia): se genera
 * al escribir la venta, incluso sin conexion, y es lo que evita filas duplicadas al
 * reintentar. Aqui todavia es un valor de ejemplo; se genera de verdad en la Tarea 4.
 */
data class VentaReciente(
    val id: String,
    val resumen: String,
    val hora: String,
    val estado: EstadoVenta,
    /** Que dato falta, cuando el estado es [EstadoVenta.INCOMPLETA]. */
    val faltante: String? = null,
)
