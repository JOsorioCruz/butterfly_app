package com.butterfly.app.recordatorios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.butterfly.app.datos.HojaDeVentas
import com.butterfly.app.datos.UltimasDeudas
import com.butterfly.app.datos.VentaPendienteDePago
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Cuantos dias antes del vencimiento se empieza a avisar (punto 7). */
private const val DIAS_DE_AVISO_PREVIO = 2

/**
 * Atiende las alarmas de cobro: el aviso del dia de una venta concreta, el barrido
 * diario, y el reinicio del celular.
 *
 * Todo lo que hace es leer las deudas vivas y mostrar UN aviso agrupado.
 */
class ReceptorDeRecordatorios : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val contexto = context.applicationContext

        // El receptor debe terminar rapido, pero leer la hoja tarda. goAsync() mantiene
        // el proceso vivo mientras se completa el trabajo en segundo plano.
        val pendiente = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED -> {
                        // Al reiniciar el celular, Android borra todas las alarmas.
                        // Sin esto, los recordatorios desaparecerian en silencio.
                        ProgramadorDeRecordatorios.programarBarridoDiario(contexto)
                        reprogramarTodo(contexto)
                    }

                    else -> {
                        revisarYAvisar(contexto)
                        // El barrido se reprograma a si mismo cada dia: una alarma solo
                        // suena una vez.
                        ProgramadorDeRecordatorios.programarBarridoDiario(contexto)
                    }
                }
            } finally {
                pendiente.finish()
            }
        }
    }

    private suspend fun revisarYAvisar(context: Context) {
        val deudas = deudasVivas(context)
        val ahora = System.currentTimeMillis()

        val vencidas = deudas.filter { (it.diasParaElPago(ahora) ?: return@filter false) < 0 }
        val proximas = deudas.filter {
            val dias = it.diasParaElPago(ahora) ?: return@filter false
            dias in 0..DIAS_DE_AVISO_PREVIO
        }

        crearCanalDeAvisos(context)
        avisarDePagos(context, vencidas, proximas)
    }

    private suspend fun reprogramarTodo(context: Context) {
        deudasVivas(context).forEach {
            ProgramadorDeRecordatorios.programarParaVenta(context, it)
        }
    }

    /**
     * Las deudas de la hoja. Si no se puede leer (sin internet, o el permiso necesita
     * intervencion), se usa la ultima copia guardada: un aviso con datos de ayer es
     * mejor que ningun aviso.
     */
    private suspend fun deudasVivas(context: Context): List<VentaPendienteDePago> {
        val copia = UltimasDeudas(context)
        val token = tokenSinInteraccion(context) ?: return copia.leer()
        val deLaHoja = HojaDeVentas(context).ventasConSaldoPendiente(token)
            ?: return copia.leer()
        copia.guardar(deLaHoja)
        return deLaHoja
    }
}
