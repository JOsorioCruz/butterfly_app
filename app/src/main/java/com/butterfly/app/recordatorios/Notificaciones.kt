package com.butterfly.app.recordatorios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.butterfly.app.MainActivity
import com.butterfly.app.R
import com.butterfly.app.datos.VentaPendienteDePago
import com.butterfly.app.util.enPesos

private const val CANAL = "pagos_pendientes"
private const val ID_AVISO_AGRUPADO = 1

/** Crea el canal de notificaciones. Obligatorio desde Android 8. */
fun crearCanalDeAvisos(context: Context) {
    val canal = NotificationChannel(
        CANAL,
        "Pagos pendientes",
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = "Avisos de cobros de ventas a crédito"
    }
    context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
}

/**
 * Muestra UN solo aviso con todas las deudas del dia.
 *
 * La especificacion pide agrupar: si hay varias clientas pendientes el mismo dia, un
 * aviso por cada una convertiria la app en algo molesto que se termina silenciando.
 */
fun avisarDePagos(context: Context, vencidas: List<VentaPendienteDePago>, proximas: List<VentaPendienteDePago>) {
    val todas = vencidas + proximas
    if (todas.isEmpty()) return

    val titulo = when {
        vencidas.isNotEmpty() && proximas.isEmpty() ->
            if (vencidas.size == 1) "1 pago vencido" else "${vencidas.size} pagos vencidos"
        vencidas.isEmpty() ->
            if (proximas.size == 1) "1 pago por cobrar" else "${proximas.size} pagos por cobrar"
        else -> "${todas.size} pagos por cobrar"
    }

    val lineas = buildList {
        vencidas.forEach { add("⚠️ ${it.nombre} — ${enPesos(it.saldo)} (vencido)") }
        proximas.forEach { add("${it.nombre} — ${enPesos(it.saldo)}") }
    }

    val abrirLaApp = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val aviso = NotificationCompat.Builder(context, CANAL)
        .setSmallIcon(R.drawable.ic_aviso)
        .setContentTitle(titulo)
        .setContentText(lineas.first())
        .setStyle(
            NotificationCompat.InboxStyle().also { estilo ->
                lineas.take(6).forEach(estilo::addLine)
                if (lineas.size > 6) estilo.setSummaryText("y ${lineas.size - 6} más")
            },
        )
        .setContentIntent(abrirLaApp)
        .setAutoCancel(true)
        .build()

    runCatching {
        NotificationManagerCompat.from(context).notify(ID_AVISO_AGRUPADO, aviso)
    }
}
