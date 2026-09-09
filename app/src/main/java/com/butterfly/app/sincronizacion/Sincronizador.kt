package com.butterfly.app.sincronizacion

import android.content.Context
import com.butterfly.app.datos.EstadoRegistro
import com.butterfly.app.datos.HistorialDeErrores
import com.butterfly.app.datos.HistorialLocal
import com.butterfly.app.datos.HojaDeVentas
import com.butterfly.app.datos.Incidente
import com.butterfly.app.datos.RegistroLocal
import com.butterfly.app.datos.ResultadoGuardado
import com.butterfly.app.datos.TipoDeIncidente
import com.butterfly.app.ia.InterpretePorIA
import com.butterfly.app.ia.ResultadoInterpretacion
import com.butterfly.app.ia.VentaInterpretada
import com.butterfly.app.recordatorios.tokenSinInteraccion
import com.butterfly.app.util.resumenDeVenta

/** Como termino un intento de vaciar la cola. */
data class ResumenDeSincronizacion(
    val subidas: Int,
    val quedanPendientes: Int,
    val huboFalloDeRed: Boolean,
)

/**
 * Sube a la hoja las ventas que quedaron pendientes.
 *
 * Lo usan tanto la app cuando esta abierta como el trabajo en segundo plano, para que
 * la logica de sincronizar viva en un solo sitio.
 *
 * **Nunca duplica** (Regla 2): cada pendiente conserva el identificador que se le
 * genero al escribirla, y `HojaDeVentas.guardar` comprueba ese identificador en la hoja
 * antes de escribir nada.
 */
suspend fun sincronizarPendientes(context: Context): ResumenDeSincronizacion {
    val historial = HistorialLocal(context)
    val pendientes = historial.pendientes()
    if (pendientes.isEmpty()) return ResumenDeSincronizacion(0, 0, false)

    val token = tokenSinInteraccion(context)
        ?: return ResumenDeSincronizacion(0, pendientes.size, true)

    val hoja = HojaDeVentas(context)
    val interprete = InterpretePorIA()
    val errores = HistorialDeErrores(context)

    var subidas = 0
    var falloDeRed = false

    // De la mas antigua a la mas reciente, para que la hoja quede en orden cronologico.
    for (pendiente in pendientes.reversed()) {
        if (falloDeRed) break

        val venta = pendiente.venta ?: when (
            val lectura = interprete.interpretar(pendiente.mensajeOriginal)
        ) {
            is ResultadoInterpretacion.Completa -> lectura.venta

            is ResultadoInterpretacion.Incompleta -> {
                // Se escribio sin conexion y resulto que faltaba un dato. No se puede
                // registrar; se marca para que la dueña lo vea y lo corrija.
                historial.actualizarEstado(pendiente.id, EstadoRegistro.INCOMPLETA)
                continue
            }

            ResultadoInterpretacion.NoEsUnaVenta -> {
                anotar(
                    errores, pendiente, TipoDeIncidente.NO_ES_VENTA,
                    "El mensaje no hablaba de una venta, así que no se registró nada.",
                )
                historial.actualizarEstado(pendiente.id, EstadoRegistro.INCOMPLETA)
                continue
            }

            is ResultadoInterpretacion.Fallo -> {
                // La IA no respondio: se deja pendiente y se reintenta mas tarde.
                falloDeRed = true
                continue
            }
        }

        when (val guardado = hoja.guardar(venta, pendiente.id, token)) {
            ResultadoGuardado.Guardada, ResultadoGuardado.YaEstaba -> {
                historial.actualizarEstado(pendiente.id, EstadoRegistro.GUARDADA)
                actualizarResumen(historial, pendiente, venta)
                subidas++
            }

            is ResultadoGuardado.SinConexion -> falloDeRed = true

            is ResultadoGuardado.Fallo -> {
                anotar(errores, pendiente, TipoDeIncidente.HOJA, guardado.mensaje)
                falloDeRed = true
            }
        }
    }

    return ResumenDeSincronizacion(
        subidas = subidas,
        quedanPendientes = historial.pendientes().size,
        huboFalloDeRed = falloDeRed,
    )
}

/**
 * Una venta escrita sin conexion se guardo con el texto crudo como resumen. Al
 * sincronizarse ya se sabe que decia, asi que el historial pasa a mostrar el resumen
 * de verdad en vez del texto original.
 */
private suspend fun actualizarResumen(
    historial: HistorialLocal,
    pendiente: RegistroLocal,
    venta: VentaInterpretada,
) {
    if (pendiente.venta != null) return
    historial.reemplazar(
        pendiente.copy(
            resumen = resumenDeVenta(venta),
            estado = EstadoRegistro.GUARDADA,
            venta = venta,
        ),
    )
}

private suspend fun anotar(
    errores: HistorialDeErrores,
    pendiente: RegistroLocal,
    tipo: TipoDeIncidente,
    queFallo: String,
) {
    errores.agregar(
        Incidente(
            id = pendiente.id,
            fecha = System.currentTimeMillis(),
            textoOriginal = pendiente.mensajeOriginal,
            queFallo = queFallo,
            tipo = tipo,
        ),
    )
}
