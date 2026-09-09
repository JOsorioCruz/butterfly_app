package com.butterfly.app.ia

/**
 * Un caso de prueba: el texto que se escribe y lo que se espera que la IA entienda.
 *
 * Solo se comprueban los campos que importan en cada caso; los que van a null en
 * [esperado] no se revisan.
 */
data class CasoDePrueba(
    val descripcion: String,
    val texto: String,
    val esVenta: Boolean = true,
    val nombre: String? = null,
    val producto: String? = null,
    val precio: Long? = null,
    val talla: String? = null,
    val modalidad: ModalidadDePago? = null,
    val abono: Long? = null,
    val saldo: Long? = null,
    val telefono: String? = null,
    val debeSerIncompleta: Boolean = false,
)

/**
 * Los 15 mensajes de la **Validacion B**, con los estilos de escritura que exige la
 * especificacion: credito, pago inmediato, abono parcial y datos incompletos.
 */
val BANCO_DE_PRUEBAS = listOf(
    CasoDePrueba(
        descripcion = "1. Credito con abono parcial (el ejemplo original)",
        texto = "venta de blusa: maria, talla m, 50 mil, credito, abono 20 mil",
        nombre = "maria", producto = "blusa", precio = 50_000, talla = "M",
        modalidad = ModalidadDePago.CREDITO, abono = 20_000, saldo = 30_000,
    ),
    CasoDePrueba(
        descripcion = "2. Credito sin abono",
        texto = "le fie un vestido a luisa talla s en 120 mil, queda debiendo todo",
        nombre = "luisa", producto = "vestido", precio = 120_000,
        modalidad = ModalidadDePago.CREDITO, abono = 0, saldo = 120_000,
    ),
    CasoDePrueba(
        descripcion = "3. Pago inmediato explicito",
        texto = "carolina compro un jean talla 10 y pago los 85 mil de una",
        nombre = "carolina", producto = "jean", precio = 85_000,
        modalidad = ModalidadDePago.INMEDIATO, saldo = 0,
    ),
    CasoDePrueba(
        descripcion = "4. Sin mencionar forma de pago (debe asumir inmediato)",
        texto = "vendi una camiseta a pedro en 35000",
        nombre = "pedro", producto = "camiseta", precio = 35_000,
        modalidad = ModalidadDePago.INMEDIATO, saldo = 0,
    ),
    CasoDePrueba(
        descripcion = "5. Precio escrito con puntos",
        texto = "sandra, falda, 45.000, pago completo",
        nombre = "sandra", producto = "falda", precio = 45_000,
        modalidad = ModalidadDePago.INMEDIATO,
    ),
    CasoDePrueba(
        descripcion = "6. Precio con k",
        texto = "blusa para diana 60k credito abono 30k",
        nombre = "diana", producto = "blusa", precio = 60_000,
        modalidad = ModalidadDePago.CREDITO, abono = 30_000, saldo = 30_000,
    ),
    CasoDePrueba(
        descripcion = "7. Precio en letras",
        texto = "a marcela le vendi un saco en ochenta mil, me abono treinta mil",
        nombre = "marcela", producto = "saco", precio = 80_000,
        modalidad = ModalidadDePago.CREDITO, abono = 30_000, saldo = 50_000,
    ),
    CasoDePrueba(
        descripcion = "8. Sin talla (no debe volverla incompleta)",
        texto = "pijama para andrea 55 mil de contado",
        nombre = "andrea", producto = "pijama", precio = 55_000,
        modalidad = ModalidadDePago.INMEDIATO,
    ),
    CasoDePrueba(
        descripcion = "9. Con telefono",
        texto = "venta a claudia 3105551234, short talla 8, 40 mil pagados",
        nombre = "claudia", producto = "short", precio = 40_000,
        telefono = "3105551234", modalidad = ModalidadDePago.INMEDIATO,
    ),
    CasoDePrueba(
        descripcion = "10. Con estado de entrega",
        texto = "chaqueta de paula 95 mil, ya pago, se la entregue hoy",
        nombre = "paula", producto = "chaqueta", precio = 95_000,
        modalidad = ModalidadDePago.INMEDIATO,
    ),
    CasoDePrueba(
        descripcion = "11. Con cuotas y fecha",
        texto = "abrigo a rosa 200 mil a credito, abono 50 mil, me paga el resto el 15 de octubre",
        nombre = "rosa", producto = "abrigo", precio = 200_000,
        modalidad = ModalidadDePago.CREDITO, abono = 50_000, saldo = 150_000,
    ),
    CasoDePrueba(
        descripcion = "12. INCOMPLETA: falta el precio",
        texto = "venta de camisa a juan, azul",
        debeSerIncompleta = true,
    ),
    CasoDePrueba(
        descripcion = "13. INCOMPLETA: falta el nombre",
        texto = "blusa talla m 50 mil credito",
        debeSerIncompleta = true,
    ),
    CasoDePrueba(
        descripcion = "14. INCOMPLETA: falta el producto",
        texto = "a natalia le cobre 70 mil, quedo debiendo 20",
        debeSerIncompleta = true,
    ),
    CasoDePrueba(
        descripcion = "15. NO es una venta",
        texto = "hola, acuerdate de llamar a la proveedora mañana",
        esVenta = false,
    ),
)
