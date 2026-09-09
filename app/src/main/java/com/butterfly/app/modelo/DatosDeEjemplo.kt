package com.butterfly.app.modelo

/**
 * DATOS FALSOS, SOLO PARA LA TAREA 1.
 *
 * Sirven para acordar el diseno de la pantalla antes de conectar nada.
 * Se borran en la Tarea 5, cuando el historial pase a mostrar ventas de verdad.
 */
val VENTAS_DE_EJEMPLO = listOf(
    VentaReciente(
        id = "ejemplo-1",
        resumen = "Maria - Blusa talla M - $50.000 - Credito (abono $20.000, saldo $30.000)",
        hora = "Hoy, 2:14 p. m.",
        estado = EstadoVenta.GUARDADA,
    ),
    VentaReciente(
        id = "ejemplo-2",
        resumen = "Carolina - Jean talla 10 - $85.000 - Pago inmediato",
        hora = "Hoy, 11:02 a. m.",
        estado = EstadoVenta.GUARDADA,
    ),
    VentaReciente(
        id = "ejemplo-3",
        resumen = "Luisa - Vestido talla S - $120.000 - Credito (saldo $120.000)",
        hora = "Hoy, 9:41 a. m.",
        estado = EstadoVenta.PENDIENTE,
    ),
    VentaReciente(
        id = "ejemplo-4",
        resumen = "\"venta de camisa a juan, azul\"",
        hora = "Ayer, 6:20 p. m.",
        estado = EstadoVenta.INCOMPLETA,
        faltante = "Falta el precio",
    ),
)
