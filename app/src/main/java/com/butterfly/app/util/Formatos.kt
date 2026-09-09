package com.butterfly.app.util

import com.butterfly.app.ia.ModalidadDePago
import com.butterfly.app.ia.VentaInterpretada
import java.text.NumberFormat
import java.util.Locale

private val PESOS: NumberFormat = NumberFormat.getIntegerInstance(Locale("es", "CO"))

/** 50000 -> "$50.000". En pesos colombianos, no "50000.0". */
fun enPesos(valor: Long): String = "$" + PESOS.format(valor)

/**
 * El resumen que se muestra al guardar, con el formato acordado en el punto 6:
 *
 * "Maria - Blusa talla M - $50.000 - Credito (abono $20.000, saldo $30.000)"
 */
fun resumenDeVenta(venta: VentaInterpretada): String = buildString {
    append(venta.nombreCliente?.replaceFirstChar { it.uppercase() } ?: "Sin nombre")
    append(" - ")
    append(venta.producto?.replaceFirstChar { it.uppercase() } ?: "Sin producto")
    venta.talla?.let { append(" talla ").append(it.uppercase()) }
    append(" - ")
    append(venta.precio?.let(::enPesos) ?: "Sin precio")
    append(" - ")
    when (venta.modalidad) {
        ModalidadDePago.INMEDIATO -> append("Pago inmediato")
        ModalidadDePago.CREDITO -> {
            append("Crédito (")
            if (venta.abono > 0) append("abono ").append(enPesos(venta.abono)).append(", ")
            append("saldo ").append(enPesos(venta.saldoPendiente)).append(")")
        }
    }
}

/** "Hoy, 2:14 p. m." · "Ayer, 6:20 p. m." · "9 sep, 2:14 p. m." */
fun horaLegible(momento: Long): String {
    val hora = java.text.SimpleDateFormat("h:mm a", Locale("es", "CO")).format(java.util.Date(momento))
    val hoy = java.util.Calendar.getInstance()
    val cuando = java.util.Calendar.getInstance().apply { timeInMillis = momento }

    fun mismoDia(a: java.util.Calendar, b: java.util.Calendar) =
        a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) &&
            a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)

    val ayer = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        mismoDia(hoy, cuando) -> "Hoy, $hora"
        mismoDia(ayer, cuando) -> "Ayer, $hora"
        else -> java.text.SimpleDateFormat("d MMM, h:mm a", Locale("es", "CO"))
            .format(java.util.Date(momento))
    }
}
