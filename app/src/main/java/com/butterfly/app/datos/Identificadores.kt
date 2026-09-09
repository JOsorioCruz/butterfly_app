package com.butterfly.app.datos

import java.util.UUID

/**
 * Genera el identificador unico de una venta, exigido por la **Regla 2**.
 *
 * Se genera en el momento de escribir la venta, ANTES de intentar mandarla, y funciona
 * igual sin conexion. Es lo que permite que un reintento pregunte a la hoja "¿esta ya
 * este registro?" en vez de crear una fila duplicada.
 */
fun nuevoIdDeRegistro(): String = UUID.randomUUID().toString()
