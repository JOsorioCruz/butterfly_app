package com.butterfly.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Si el celular tiene internet ahora mismo.
 *
 * Sirve para decidir si la venta se manda o se guarda en la cola. Ojo: responder que
 * si no garantiza que la llamada vaya a funcionar (puede haber wifi sin salida a
 * internet), por eso el guardado tambien maneja el fallo de red por su cuenta.
 */
fun hayInternet(context: Context): Boolean {
    val gestor = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val red = gestor.activeNetwork ?: return false
    val capacidades = gestor.getNetworkCapabilities(red) ?: return false
    return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
