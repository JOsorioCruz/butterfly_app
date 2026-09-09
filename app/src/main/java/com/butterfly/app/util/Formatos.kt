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
