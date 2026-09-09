package com.butterfly.app.recordatorios

import android.content.Context
import com.butterfly.app.auth.PERMISO_ARCHIVOS_PROPIOS
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Consigue el token para leer la hoja SIN abrir la app ni mostrar nada.
 *
 * Hace falta porque el barrido diario corre en segundo plano, cuando no hay ninguna
 * pantalla abierta a la que pedirle permiso a la dueña. Si el permiso sigue concedido,
 * Google devuelve el token en silencio; si hiciera falta interaccion, devuelve null y
 * el barrido usa la ultima copia guardada de las deudas.
 */
suspend fun tokenSinInteraccion(context: Context): String? =
    suspendCancellableCoroutine { continuacion ->
        val peticion = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(PERMISO_ARCHIVOS_PROPIOS)))
            .build()

        Identity.getAuthorizationClient(context)
            .authorize(peticion)
            .addOnSuccessListener { resultado ->
                // hasResolution() significa "hay que preguntarle a la usuaria", y en
                // segundo plano no se puede. Se devuelve null y se usa el respaldo.
                continuacion.resume(
                    if (resultado.hasResolution()) null else resultado.accessToken,
                )
            }
            .addOnFailureListener { continuacion.resume(null) }
    }
