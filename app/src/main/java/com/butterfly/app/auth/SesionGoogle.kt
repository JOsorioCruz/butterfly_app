package com.butterfly.app.auth

import android.content.Context

/** La cuenta de Google con la que quedo iniciada la sesion. */
data class SesionGoogle(
    val correo: String,
    val nombre: String?,
)

/**
 * Guarda en el celular con que cuenta se inicio sesion, para no volver a preguntar
 * cada vez que se abre la app.
 *
 * Importante: aqui NO se guarda ningun token de acceso. Los tokens caducan en una hora
 * y se piden de nuevo con [AutenticacionGoogle.obtenerTokenDeAcceso], que los entrega
 * sin molestar a la usuaria mientras el permiso siga concedido. Guardar tokens en el
 * celular seria guardar algo que caduca y que no hace falta.
 */
class AlmacenDeSesion(context: Context) {

    private val preferencias =
        context.getSharedPreferences("butterfly_sesion", Context.MODE_PRIVATE)

    fun guardar(sesion: SesionGoogle) {
        preferencias.edit()
            .putString(CLAVE_CORREO, sesion.correo)
            .putString(CLAVE_NOMBRE, sesion.nombre)
            .apply()
    }

    fun leer(): SesionGoogle? {
        val correo = preferencias.getString(CLAVE_CORREO, null) ?: return null
        return SesionGoogle(correo, preferencias.getString(CLAVE_NOMBRE, null))
    }

    fun borrar() {
        preferencias.edit().clear().apply()
    }

    private companion object {
        const val CLAVE_CORREO = "correo"
        const val CLAVE_NOMBRE = "nombre"
    }
}
