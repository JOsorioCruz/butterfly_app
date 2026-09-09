package com.butterfly.app.recordatorios

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.butterfly.app.datos.VentaPendienteDePago
import java.util.Calendar

const val ACCION_RECORDATORIO = "com.butterfly.app.RECORDATORIO"
const val ACCION_BARRIDO_DIARIO = "com.butterfly.app.BARRIDO_DIARIO"
const val EXTRA_ID_VENTA = "id_venta"

private const val CODIGO_BARRIDO = 777

/**
 * Programa los avisos de cobro con las alarmas del propio Android. Sin servidor y sin
 * costo, como pide la especificacion.
 *
 * **Se usan alarmas inexactas a proposito.** Android 12 y posteriores exigen un permiso
 * especial para las alarmas al minuto exacto, y aqui no hace falta: para "hoy toca
 * cobrarle a Maria" da igual que el aviso llegue a las 8:00 o a las 8:20. Pedir ese
 * permiso habria añadido un paso mas de configuracion a cambio de nada.
 */
object ProgramadorDeRecordatorios {

    /** Aviso para el dia en que una clienta debe pagar. */
    fun programarParaVenta(context: Context, venta: VentaPendienteDePago) {
        val momento = venta.momentoDelAviso() ?: return
        // Una fecha que ya paso no se programa: de eso se encarga el barrido diario,
        // que avisa de los vencidos.
        if (momento <= System.currentTimeMillis()) return

        alarmas(context).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            momento,
            intentDeVenta(context, venta.id),
        )
    }

    /**
     * Cancela el aviso de una venta. Se llama cuando el saldo llega a cero, para que
     * no siga avisando de una deuda ya cobrada.
     */
    fun cancelarParaVenta(context: Context, idVenta: String) {
        alarmas(context).cancel(intentDeVenta(context, idVenta))
    }

    /** Revision diaria al amanecer de todas las deudas vivas. */
    fun programarBarridoDiario(context: Context) {
        val manana = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmas(context).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            manana.timeInMillis,
            PendingIntent.getBroadcast(
                context,
                CODIGO_BARRIDO,
                Intent(context, ReceptorDeRecordatorios::class.java)
                    .setAction(ACCION_BARRIDO_DIARIO),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
    }

    private fun alarmas(context: Context) =
        context.getSystemService(AlarmManager::class.java)

    private fun intentDeVenta(context: Context, idVenta: String) = PendingIntent.getBroadcast(
        context,
        // El identificador de la venta convertido a numero: es lo que ata el aviso a
        // esa venta concreta y permite cancelarlo despues (punto 7).
        idVenta.hashCode(),
        Intent(context, ReceptorDeRecordatorios::class.java)
            .setAction(ACCION_RECORDATORIO)
            .putExtra(EXTRA_ID_VENTA, idVenta),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
