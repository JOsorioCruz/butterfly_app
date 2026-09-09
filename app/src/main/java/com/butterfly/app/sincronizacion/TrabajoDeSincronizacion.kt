package com.butterfly.app.sincronizacion

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

private const val NOMBRE_DEL_TRABAJO = "sincronizar_ventas"

/**
 * Sube las ventas pendientes en cuanto vuelve la conexion, sin que la dueña haga nada.
 *
 * Se usa WorkManager porque es lo unico que cumple las tres condiciones a la vez:
 * espera a que haya internet, sobrevive a que se cierre la app, y sobrevive a que se
 * reinicie el celular. Escuchar los cambios de red a mano no sobrevive a lo segundo.
 */
class TrabajoDeSincronizacion(
    context: Context,
    parametros: WorkerParameters,
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        val resumen = runCatching { sincronizarPendientes(applicationContext) }
            .getOrElse { return Result.retry() }

        // retry() hace que WorkManager espere cada vez un poco mas antes de volver a
        // intentarlo, en vez de castigar la bateria reintentando sin parar.
        return if (resumen.huboFalloDeRed) Result.retry() else Result.success()
    }
}

/**
 * Pide que se sincronice en cuanto haya internet.
 *
 * `KEEP` evita amontonar trabajos: si ya hay uno esperando conexion, se deja el que
 * esta, porque hara exactamente lo mismo.
 */
fun pedirSincronizacion(context: Context) {
    val trabajo = OneTimeWorkRequestBuilder<TrabajoDeSincronizacion>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        )
        .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork(NOMBRE_DEL_TRABAJO, ExistingWorkPolicy.KEEP, trabajo)
}
