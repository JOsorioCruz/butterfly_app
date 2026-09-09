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
import com.butterfly.app.datos.EstadoRegistro
import com.butterfly.app.datos.HistorialLocal
import com.butterfly.app.datos.HojaDeVentas
import com.butterfly.app.datos.RegistroLocal
import com.butterfly.app.datos.ResultadoGuardado
import com.butterfly.app.datos.nuevoIdDeRegistro
import com.butterfly.app.ia.InterpretePorIA
import com.butterfly.app.ia.ResultadoInterpretacion
import com.butterfly.app.ia.ejecutarBancoDePruebas
import com.butterfly.app.ui.EstadoDeGuardado
import com.butterfly.app.ui.PantallaLogin
import com.butterfly.app.ui.PantallaPruebas
import com.butterfly.app.ui.ResultadoDeCaso
import com.butterfly.app.ui.PantallaPrincipal
import com.butterfly.app.ui.theme.TemaButterfly
import com.butterfly.app.util.resumenDeVenta
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val autenticacion = AutenticacionGoogle(this)
        val almacen = AlmacenDeSesion(this)
        val interprete = InterpretePorIA()
        val hoja = HojaDeVentas(this)
        val historialLocal = HistorialLocal(this)

        setContent {
            TemaButterfly {
                val alcance = rememberCoroutineScope()

                // La sesion guardada es lo que hace que la app no vuelva a pedir login
                // cada vez que se abre (Validacion A).
                var sesion by remember { mutableStateOf(almacen.leer()) }
                var tokenDeAcceso by remember { mutableStateOf<String?>(null) }
                var cargando by remember { mutableStateOf(false) }
                var mensajeDeError by remember { mutableStateOf<String?>(null) }

                // Tareas 3 y 4: interpretar el texto y escribirlo en la hoja.
                var estadoDeGuardado by remember {
                    mutableStateOf<EstadoDeGuardado>(EstadoDeGuardado.Inactivo)
                }
                // Tarea 5: el historial vive en el celular, asi que sigue ahi al
                // cerrar y reabrir la app.
                var historial by remember { mutableStateOf(emptyList<RegistroLocal>()) }
                LaunchedEffect(Unit) { historial = historialLocal.leer() }

                // Validacion B: solo en la version de depuracion.
                var enPantallaDePruebas by remember { mutableStateOf(false) }
                var corriendoPruebas by remember { mutableStateOf(false) }
                var resultadosDePruebas by remember { mutableStateOf(emptyList<ResultadoDeCaso>()) }

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
                } else if (enPantallaDePruebas) {
                    PantallaPruebas(
                        corriendo = corriendoPruebas,
                        resultados = resultadosDePruebas,
                        onEjecutar = {
                            corriendoPruebas = true
                            resultadosDePruebas = emptyList()
                            alcance.launch {
                                resultadosDePruebas = ejecutarBancoDePruebas(interprete)
                                corriendoPruebas = false
                            }
                        },
                        onVolver = { enPantallaDePruebas = false },
                    )
                } else {
                    PantallaPrincipal(
                        correo = sesionActual.correo,
                        autorizado = tokenDeAcceso != null,
                        estado = estadoDeGuardado,
                        historial = historial,
                        onGuardar = { texto ->
                            estadoDeGuardado = EstadoDeGuardado.Trabajando
                            alcance.launch {
                                estadoDeGuardado = interpretarYGuardar(
                                    texto, interprete, hoja, historialLocal, tokenDeAcceso,
                                )
                                historial = historialLocal.leer()
                            }
                        },
                        onProbarEjemplos = { enPantallaDePruebas = true },
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

    /**
     * Las dos etapas que para la dueña son una sola accion: la IA lee el texto y, si
     * quedo completo, la venta se escribe en la hoja.
     *
     * Si algo falla en cualquiera de las dos, NO se crea ninguna fila (punto 9).
     */
    private suspend fun interpretarYGuardar(
        texto: String,
        interprete: InterpretePorIA,
        hoja: HojaDeVentas,
        historial: HistorialLocal,
        token: String?,
    ): EstadoDeGuardado {
        // El identificador se genera ANTES de intentar mandar nada, para que un
        // reintento pueda reconocer la venta y no duplicarla (Regla 2).
        val id = nuevoIdDeRegistro()

        return when (val lectura = interprete.interpretar(texto)) {
            is ResultadoInterpretacion.Incompleta -> {
                val falta = lectura.venta.datosQueFaltan.joinToString(" y ")
                // Se deja constancia del intento: no se creo ninguna fila en la hoja,
                // pero la dueña puede ver que ese mensaje no quedo registrado.
                historial.agregar(
                    RegistroLocal(
                        id = id,
                        fecha = System.currentTimeMillis(),
                        resumen = "\"" + texto.trim() + "\"",
                        estado = EstadoRegistro.INCOMPLETA,
                        mensajeOriginal = texto,
                        faltante = "Falta " + falta,
                    ),
                )
                EstadoDeGuardado.Incompleta(falta)
            }

            ResultadoInterpretacion.NoEsUnaVenta -> EstadoDeGuardado.NoEsUnaVenta

            is ResultadoInterpretacion.Fallo -> EstadoDeGuardado.Error(lectura.mensaje)

            is ResultadoInterpretacion.Completa -> {
                val resumen = resumenDeVenta(lectura.venta)
                if (token == null) {
                    EstadoDeGuardado.Error(
                        "Falta el permiso para escribir en la hoja. Vuelve a entrar con Google.",
                    )
                } else {
                    val guardado = hoja.guardar(lectura.venta, id, token)
                    val estado = when (guardado) {
                        ResultadoGuardado.Guardada, ResultadoGuardado.YaEstaba ->
                            EstadoRegistro.GUARDADA
                        is ResultadoGuardado.SinConexion -> EstadoRegistro.PENDIENTE
                        is ResultadoGuardado.Fallo -> null
                    }
                    // Una venta solo se anota como guardada cuando Google Sheets lo
                    // confirmo. Si fallo, no se anota nada (Regla 2 y punto 9).
                    if (estado != null) {
                        historial.agregar(
                            RegistroLocal(
                                id = id,
                                fecha = System.currentTimeMillis(),
                                resumen = resumen,
                                estado = estado,
                                mensajeOriginal = texto,
                                venta = lectura.venta,
                            ),
                        )
                    }
                    when (guardado) {
                        ResultadoGuardado.Guardada -> EstadoDeGuardado.Guardada(resumen)
                        ResultadoGuardado.YaEstaba -> EstadoDeGuardado.YaEstaba(resumen)
                        is ResultadoGuardado.SinConexion ->
                            EstadoDeGuardado.SinConexion(guardado.mensaje)
                        is ResultadoGuardado.Fallo -> EstadoDeGuardado.Error(guardado.mensaje)
                    }
                }
            }
        }
    }
}
