package com.butterfly.app.ia

/** Como paga la clienta. */
enum class ModalidadDePago { CREDITO, INMEDIATO }

/**
 * Los datos de una venta ya extraidos del texto libre.
 *
 * OJO con [saldoPendiente]: NO lo calcula la IA. Lo calcula Kotlin en [crear], a
 * proposito. Una operacion aritmetica siempre debe dar el mismo resultado, y un modelo
 * de lenguaje no garantiza eso. La IA solo extrae lo que dice el texto; las cuentas
 * las hace el codigo.
 */
data class VentaInterpretada(
    val nombreCliente: String?,
    val telefono: String?,
    val producto: String?,
    val talla: String?,
    val precio: Long?,
    val modalidad: ModalidadDePago,
    val abono: Long,
    val saldoPendiente: Long,
    val cuotas: Int?,
    val fechaProximoPago: String?,
    val estadoEntrega: String?,
    val mensajeOriginal: String,
) {
    companion object {
        /**
         * Aplica la logica del punto 4 de la especificacion:
         *  - Credito  -> saldo = precio - abono.
         *  - Inmediato -> saldo = 0, siempre.
         */
        fun crear(
            nombreCliente: String?,
            telefono: String?,
            producto: String?,
            talla: String?,
            precio: Long?,
            modalidad: ModalidadDePago,
            abono: Long?,
            cuotas: Int?,
            fechaProximoPago: String?,
            estadoEntrega: String?,
            mensajeOriginal: String,
        ): VentaInterpretada {
            val abonoReal = when (modalidad) {
                ModalidadDePago.INMEDIATO -> precio ?: 0L
                ModalidadDePago.CREDITO -> abono ?: 0L
            }
            val saldo = when (modalidad) {
                ModalidadDePago.INMEDIATO -> 0L
                ModalidadDePago.CREDITO -> ((precio ?: 0L) - abonoReal).coerceAtLeast(0L)
            }
            return VentaInterpretada(
                nombreCliente = nombreCliente?.takeIf { it.isNotBlank() },
                telefono = telefono?.takeIf { it.isNotBlank() },
                producto = producto?.takeIf { it.isNotBlank() },
                talla = talla?.takeIf { it.isNotBlank() },
                precio = precio,
                modalidad = modalidad,
                abono = abonoReal,
                saldoPendiente = saldo,
                // Si no se mencionaron, quedan vacias. No se inventan (punto 4).
                cuotas = cuotas,
                fechaProximoPago = fechaProximoPago?.takeIf { it.isNotBlank() },
                estadoEntrega = estadoEntrega?.takeIf { it.isNotBlank() },
                mensajeOriginal = mensajeOriginal,
            )
        }
    }

    /**
     * Que datos obligatorios faltan. Segun el punto 3, solo estos tres hacen que un
     * registro sea "incompleto"; el telefono, la talla y lo demas pueden faltar.
     */
    val datosQueFaltan: List<String>
        get() = buildList {
            if (nombreCliente == null) add("el nombre de la clienta")
            if (producto == null) add("el producto")
            if (precio == null) add("el precio")
        }

    val estaCompleta: Boolean get() = datosQueFaltan.isEmpty()
}

/** Como puede terminar el intento de interpretar un texto. */
sealed interface ResultadoInterpretacion {
    /** Se entendio todo lo obligatorio. */
    data class Completa(val venta: VentaInterpretada) : ResultadoInterpretacion

    /** Se entendio, pero falta un dato obligatorio. NO se crea ninguna fila. */
    data class Incompleta(val venta: VentaInterpretada) : ResultadoInterpretacion

    /** El texto no habla de una venta. Va al historial de errores (Tarea 6). */
    data object NoEsUnaVenta : ResultadoInterpretacion

    /** Fallo la IA o la conexion. NO se crea ninguna fila (punto 9). */
    data class Fallo(val mensaje: String) : ResultadoInterpretacion
}
