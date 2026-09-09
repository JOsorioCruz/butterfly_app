package com.butterfly.app.ia

import com.butterfly.app.ui.ResultadoDeCaso
import com.butterfly.app.util.enPesos

/**
 * Corre el banco de la **Validacion B** contra la IA de verdad y compara, caso por
 * caso, lo esperado con lo entendido.
 *
 * Solo se comparan los campos que el caso declara. Un campo a null en el caso significa
 * "no me importa en esta prueba", no "debe venir vacio".
 */
suspend fun ejecutarBancoDePruebas(
    interprete: InterpretePorIA,
    casos: List<CasoDePrueba> = BANCO_DE_PRUEBAS,
): List<ResultadoDeCaso> = casos.map { caso ->
    val resultado = interprete.interpretar(caso.texto)
    val diferencias = mutableListOf<String>()

    when {
        !caso.esVenta -> if (resultado !is ResultadoInterpretacion.NoEsUnaVenta) {
            diferencias += "Debia detectarse como texto que no es una venta."
        }

        caso.debeSerIncompleta -> if (resultado !is ResultadoInterpretacion.Incompleta) {
            diferencias += "Debia quedar marcada como incompleta."
        }

        else -> when (resultado) {
            is ResultadoInterpretacion.Completa -> comparar(caso, resultado.venta, diferencias)
            is ResultadoInterpretacion.Incompleta ->
                diferencias += "Quedo incompleta; falta ${resultado.venta.datosQueFaltan}."
            ResultadoInterpretacion.NoEsUnaVenta ->
                diferencias += "No la reconocio como venta."
            is ResultadoInterpretacion.Fallo ->
                diferencias += "Fallo: ${resultado.mensaje}"
        }
    }

    ResultadoDeCaso(
        descripcion = caso.descripcion,
        texto = caso.texto,
        paso = diferencias.isEmpty(),
        loQueEntendio = describir(resultado),
        diferencias = diferencias,
    )
}

private fun comparar(
    caso: CasoDePrueba,
    venta: VentaInterpretada,
    diferencias: MutableList<String>,
) {
    caso.nombre?.let {
        if (!venta.nombreCliente.equals(it, ignoreCase = true)) {
            diferencias += "Nombre: esperaba \"$it\", entendio \"${venta.nombreCliente}\"."
        }
    }
    caso.producto?.let {
        if (!venta.producto.equals(it, ignoreCase = true)) {
            diferencias += "Producto: esperaba \"$it\", entendio \"${venta.producto}\"."
        }
    }
    caso.precio?.let {
        if (venta.precio != it) {
            diferencias += "Precio: esperaba ${enPesos(it)}, entendio ${venta.precio?.let(::enPesos)}."
        }
    }
    caso.talla?.let {
        if (!venta.talla.equals(it, ignoreCase = true)) {
            diferencias += "Talla: esperaba \"$it\", entendio \"${venta.talla}\"."
        }
    }
    caso.modalidad?.let {
        if (venta.modalidad != it) {
            diferencias += "Modalidad: esperaba $it, entendio ${venta.modalidad}."
        }
    }
    caso.abono?.let {
        if (venta.abono != it) {
            diferencias += "Abono: esperaba ${enPesos(it)}, entendio ${enPesos(venta.abono)}."
        }
    }
    caso.saldo?.let {
        if (venta.saldoPendiente != it) {
            diferencias += "Saldo: esperaba ${enPesos(it)}, calculo ${enPesos(venta.saldoPendiente)}."
        }
    }
    caso.telefono?.let {
        if (venta.telefono?.filter(Char::isDigit) != it) {
            diferencias += "Telefono: esperaba \"$it\", entendio \"${venta.telefono}\"."
        }
    }
}

private fun describir(resultado: ResultadoInterpretacion): String = when (resultado) {
    is ResultadoInterpretacion.Completa -> con(resultado.venta)
    is ResultadoInterpretacion.Incompleta ->
        "incompleta (falta ${resultado.venta.datosQueFaltan.joinToString(" y ")})"
    ResultadoInterpretacion.NoEsUnaVenta -> "no es una venta"
    is ResultadoInterpretacion.Fallo -> "fallo: ${resultado.mensaje}"
}

private fun con(v: VentaInterpretada): String = buildString {
    append(v.nombreCliente ?: "?").append(" · ")
    append(v.producto ?: "?")
    v.talla?.let { append(" talla ").append(it) }
    append(" · ").append(v.precio?.let(::enPesos) ?: "?")
    append(" · ").append(v.modalidad)
    if (v.abono > 0) append(" · abono ").append(enPesos(v.abono))
    append(" · saldo ").append(enPesos(v.saldoPendiente))
    v.telefono?.let { append(" · tel ").append(it) }
}
