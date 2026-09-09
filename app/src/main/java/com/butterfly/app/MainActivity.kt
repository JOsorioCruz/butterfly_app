package com.butterfly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.butterfly.app.auth.AlmacenDeSesion
import com.butterfly.app.auth.AutenticacionGoogle
import com.butterfly.app.auth.ResultadoAutorizacion
import com.butterfly.app.auth.ResultadoLogin
import com.butterfly.app.ui.PantallaLogin
import com.butterfly.app.ui.PantallaPrincipal
import com.butterfly.app.ui.theme.TemaButterfly
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val autenticacion = AutenticacionGoogle(this)
        val almacen = AlmacenDeSesion(this)

        setContent {
            TemaButterfly {
                val alcance = rememberCoroutineScope()

                // La sesion guardada es lo que hace que la app no vuelva a pedir login
                // cada vez que se abre (Validacion A).
                var sesion by remember { mutableStateOf(almacen.leer()) }
                var tokenDeAcceso by remember { mutableStateOf<String?>(null) }
                var cargando by remember { mutableStateOf(false) }
                var mensajeDeError by remember { mutableStateOf<String?>(null) }

                // Pantalla de permisos de Google, cuando hace falta mostrarla.
                val pedirPermiso = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartIntentSenderForResult(),
                ) { resultado ->
                    cargando = false
                    when (val r = autenticacion.leerTokenDeLaRespuesta(resultado.data)) {
                        is ResultadoAutorizacion.Concedida -> {
                            tokenDeAcceso = r.tokenDeAcceso
                            mensajeDeError = null
                        }
                        is ResultadoAutorizacion.Fallo -> mensajeDeError = r.mensaje
                        is ResultadoAutorizacion.HayQuePedirPermiso ->
                            mensajeDeError = "Google volvio a pedir permiso; intentalo de nuevo."
                    }
                }

                /** Pide el permiso de escritura; abre la pantalla de Google si hace falta. */
                fun pedirAutorizacion(alTerminar: () -> Unit = {}) {
                    alcance.launch {
                        when (val r = autenticacion.obtenerTokenDeAcceso()) {
                            is ResultadoAutorizacion.Concedida -> {
                                tokenDeAcceso = r.tokenDeAcceso
                                cargando = false
                                alTerminar()
                            }
                            is ResultadoAutorizacion.HayQuePedirPermiso ->
                                pedirPermiso.launch(
                                    IntentSenderRequest.Builder(r.pantalla).build(),
                                )
                            is ResultadoAutorizacion.Fallo -> {
                                cargando = false
                                mensajeDeError = r.mensaje
                            }
                        }
                    }
                }

                val sesionActual = sesion

                // Al abrir con sesion ya guardada, se renueva el token en silencio.
                // Si el permiso sigue concedido, la usuaria no ve nada.
                LaunchedEffect(sesionActual) {
                    if (sesionActual != null && tokenDeAcceso == null) pedirAutorizacion()
                }

                if (sesionActual == null) {
                    PantallaLogin(
                        cargando = cargando,
                        mensajeDeError = mensajeDeError,
                        faltaConfiguracion = autenticacion.faltaConfiguracion,
                        onEntrar = {
                            cargando = true
                            mensajeDeError = null
                            alcance.launch {
                                when (val r = autenticacion.iniciarSesion()) {
                                    is ResultadoLogin.Exito -> {
                                        almacen.guardar(r.sesion)
                                        sesion = r.sesion
                                        // Identificarse no basta: hace falta ademas el
                                        // permiso para escribir en la hoja.
                                        pedirAutorizacion()
                                    }
                                    ResultadoLogin.Cancelado -> cargando = false
                                    is ResultadoLogin.Fallo -> {
                                        cargando = false
                                        mensajeDeError = r.mensaje
                                    }
                                }
                            }
                        },
                    )
                } else {
                    PantallaPrincipal(
                        correo = sesionActual.correo,
                        autorizado = tokenDeAcceso != null,
                        onAutorizar = { pedirAutorizacion() },
                        onCerrarSesion = {
                            alcance.launch {
                                autenticacion.cerrarSesion()
                                almacen.borrar()
                                tokenDeAcceso = null
                                sesion = null
                            }
                        },
                    )
                }

            }
        }
    }
}
