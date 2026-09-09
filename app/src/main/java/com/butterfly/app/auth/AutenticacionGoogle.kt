package com.butterfly.app.auth

import android.app.Activity
import android.app.PendingIntent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.butterfly.app.BuildConfig
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Permiso que la app pide sobre el Drive de la dueña.
 *
 * Es el mas limitado que existe para este caso: da acceso UNICAMENTE a los archivos
 * que la propia app crea. La app no puede ver ni tocar el resto del Drive. Esto es lo
 * que exige el punto 2 de la especificacion.
 */
const val PERMISO_ARCHIVOS_PROPIOS = "https://www.googleapis.com/auth/drive.file"

/** Como puede terminar el intento de iniciar sesion. */
sealed interface ResultadoLogin {
    data class Exito(val sesion: SesionGoogle) : ResultadoLogin
    /** La usuaria cerro la ventana de Google a proposito. No es un error que avisar. */
    data object Cancelado : ResultadoLogin
    data class Fallo(val mensaje: String) : ResultadoLogin
}

/** Como puede terminar el intento de conseguir permiso para escribir en la hoja. */
sealed interface ResultadoAutorizacion {
    data class Concedida(val tokenDeAcceso: String) : ResultadoAutorizacion
    /** Google necesita mostrar la pantalla de permisos antes de conceder nada. */
    data class HayQuePedirPermiso(val pantalla: PendingIntent) : ResultadoAutorizacion
    data class Fallo(val mensaje: String) : ResultadoAutorizacion
}

/**
 * Inicio de sesion con Google y permiso para escribir en Sheets/Drive.
 *
 * Usa Credential Manager, que es el metodo vigente de Google. El antiguo
 * "Google Sign-In" (GoogleSignInClient) esta descontinuado y no se usa aqui.
 *
 * Son dos cosas distintas, y conviene no confundirlas:
 *  - **Identificarse** (quien eres): [iniciarSesion].
 *  - **Autorizar** (que puede hacer la app en tu Drive): [obtenerTokenDeAcceso].
 */
class AutenticacionGoogle(private val activity: Activity) {

    private val gestorDeCredenciales = CredentialManager.create(activity)

    /** true si falta el ID del cliente Web en local.properties (ver Tarea 2). */
    val faltaConfiguracion: Boolean
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()

    suspend fun iniciarSesion(): ResultadoLogin {
        if (faltaConfiguracion) {
            return ResultadoLogin.Fallo(
                "Falta el ID del cliente Web de Google. Se configura en local.properties.",
            )
        }

        val opcion = GetSignInWithGoogleOption
            .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        val peticion = GetCredentialRequest.Builder()
            .addCredentialOption(opcion)
            .build()

        return try {
            val respuesta = gestorDeCredenciales.getCredential(activity, peticion)
            val credencial = respuesta.credential
            if (
                credencial is CustomCredential &&
                credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val datos = GoogleIdTokenCredential.createFrom(credencial.data)
                ResultadoLogin.Exito(
                    SesionGoogle(correo = datos.id, nombre = datos.displayName),
                )
            } else {
                ResultadoLogin.Fallo("Google devolvio un tipo de credencial inesperado.")
            }
        } catch (e: GetCredentialCancellationException) {
            ResultadoLogin.Cancelado
        } catch (e: NoCredentialException) {
            ResultadoLogin.Fallo(
                "No hay ninguna cuenta de Google en este celular. Agrega una en Ajustes.",
            )
        } catch (e: GetCredentialException) {
            ResultadoLogin.Fallo(e.message ?: "No se pudo iniciar sesion con Google.")
        }
    }

    /**
     * Pide el token con el que se escribe en Google Sheets.
     *
     * Si el permiso ya estaba concedido, lo devuelve sin mostrar nada. La primera vez
     * devuelve [ResultadoAutorizacion.HayQuePedirPermiso] y hay que abrir esa pantalla.
     */
    suspend fun obtenerTokenDeAcceso(): ResultadoAutorizacion =
        suspendCancellableCoroutine { continuacion ->
            val peticion = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(PERMISO_ARCHIVOS_PROPIOS)))
                .build()

            Identity.getAuthorizationClient(activity)
                .authorize(peticion)
                .addOnSuccessListener { resultado ->
                    val pantalla = resultado.pendingIntent
                    continuacion.resume(
                        when {
                            resultado.hasResolution() && pantalla != null ->
                                ResultadoAutorizacion.HayQuePedirPermiso(pantalla)

                            resultado.accessToken != null ->
                                ResultadoAutorizacion.Concedida(resultado.accessToken!!)

                            else -> ResultadoAutorizacion.Fallo(
                                "Google no devolvio el permiso para escribir en la hoja.",
                            )
                        },
                    )
                }
                .addOnFailureListener { error ->
                    continuacion.resume(
                        ResultadoAutorizacion.Fallo(
                            error.message ?: "No se pudo pedir permiso a Google.",
                        ),
                    )
                }
        }

    /** Lee el token de la pantalla de permisos que la usuaria acaba de aceptar. */
    fun leerTokenDeLaRespuesta(datos: android.content.Intent?): ResultadoAutorizacion = try {
        val resultado = Identity.getAuthorizationClient(activity)
            .getAuthorizationResultFromIntent(datos)
        val token = resultado.accessToken
        if (token != null) {
            ResultadoAutorizacion.Concedida(token)
        } else {
            ResultadoAutorizacion.Fallo("No se concedio el permiso.")
        }
    } catch (e: Exception) {
        ResultadoAutorizacion.Fallo(e.message ?: "No se pudo leer la respuesta de Google.")
    }

    suspend fun cerrarSesion() {
        runCatching {
            gestorDeCredenciales.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest(),
            )
        }
    }
}
