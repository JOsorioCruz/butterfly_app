package com.butterfly.app.datos

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Una deuda viva, tal como esta hoy en la hoja de calculo. */
data class VentaPendienteDePago(
    val id: String,
    val nombre: String,
    val saldo: Long,
    /** Formato AAAA-MM-DD. Vacia si nunca se acordo una fecha. */
    val fechaProximoPago: String?,
) {
    /** El momento del dia de pago en que conviene avisar: las 8 de la mañana. */
    fun momentoDelAviso(): Long? {
        val fecha = fechaProximoPago ?: return null
        val dia = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)
        }.getOrNull() ?: return null

        return Calendar.getInstance().apply {
            time = dia
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /** Dias que faltan para el pago. Negativo si ya esta vencido. */
    fun diasParaElPago(ahora: Long = System.currentTimeMillis()): Int? {
        val momento = momentoDelAviso() ?: return null
        return ((momento - ahora) / 86_400_000L).toInt()
    }
}
