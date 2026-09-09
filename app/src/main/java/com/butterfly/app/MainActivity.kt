package com.butterfly.app

import android.Manifest
import android.os.Build
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
import com.butterfly.app.datos.HistorialDeErrores
import com.butterfly.app.datos.HistorialLocal
import com.butterfly.app.datos.HojaDeVentas
import com.butterfly.app.datos.Incidente
import com.butterfly.app.datos.RegistroLocal
import com.butterfly.app.datos.ResultadoGuardado
import com.butterfly.app.datos.TipoDeIncidente
import com.butterfly.app.datos.UltimasDeudas
import com.butterfly.app.datos.VentaPendienteDePago
import com.butterfly.app.datos.nuevoIdDeRegistro
import com.butterfly.app.ia.InterpretePorIA
import com.butterfly.app.recordatorios.ProgramadorDeRecordatorios
import com.butterfly.app.recordatorios.crearCanalDeAvisos
import com.butterfly.app.sincronizacion.pedirSincronizacion
import com.butterfly.app.sincronizacion.sincronizarPendientes
import com.butterfly.app.util.hayInternet
import com.butterfly.app.ia.ResultadoInterpretacion
import com.butterfly.app.ia.ejecutarBancoDePruebas
import com.butterfly.app.ui.EstadoDeGuardado
import com.butterfly.app.ui.PantallaErrores
import com.butterfly.app.ui.PantallaLogin
import com.butterfly.app.ui.PantallaPrincipal
import com.butterfly.app.ui.PantallaPruebas
import com.butterfly.app.ui.ResultadoDeCaso
import com.butterfly.app.ui.theme.TemaButterfly
import com.butterfly.app.util.resumenDeVenta
import kotlinx.coroutines.launch

/** En que pantalla esta la app. La app tiene pocas, asi que no hace falta mas. */
private enum class Pantalla { PRINCIPAL, ERRORES, PRUEBAS }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val autenticacion = AutenticacionGoogle(this)
        val almacen = AlmacenDeSesion(this)
        val interprete = InterpretePorIA()
        val hoja = HojaDeVentas(this)
        val historialLocal = HistorialLocal(this)
        val errores = HistorialDeErrores(this)
        val ultimasDeudas = UltimasDeudas(this)

        // Tarea 7: los avisos de cobro necesitan un canal, y desde Android 13 tambien
        // el permiso explicito de la dueña.
        crearCanalDeAvisos(this)
        pedirPermisoDeAvisos()
        ProgramadorDeRecordatorios.programarBarridoDiario(this)

        setContent {
            TemaButterfly {
                val alcance = rememberCoroutineScope()

                // La sesion guardada es lo que hace que la app no vuelva a pedir login
                // cada vez que se abre (Validacion A).
                var sesion by remember { mutableStateOf(almacen.leer()) }
                var tokenDeAcceso by remember { mutableStateOf<String?>(null) }
                var cargando by remember { mutableStateOf(false) }
                var mensajeDeError by remember { mutableStateOf<String?>(null) }

                var pantalla by remember { mutableStateOf(Pantalla.PRINCIPAL) }

                // Tareas 3, 4 y 5: interpretar, escribir en la hoja y dejar constancia.
                var estadoDeGuardado by remember {
                    mutableStateOf<EstadoDeGuardado>(EstadoDeGuardado.Inactivo)
                }
                var historial by remember { mutableStateOf(emptyList<RegistroLocal>()) }
                var incidentes by remember { mutableStateOf(emptyList<Incidente>()) }
                var reintentando by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    historial = historialLocal.leer()
                    incidentes = errores.leer()

                    // Al abrir la app se intenta vaciar la cola. Si no hay internet,
                    // WorkManager se encarga cuando vuelva.
                    if (historialLocal.pendientes().isNotEmpty()) {
                        sincronizarPendientes(applicationContext)
                        historial = historialLocal.leer()
                        pedirSincronizacion(applicationContext)
                    }
                }

                // Validacion B: solo en la version de depuracion.
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
                fun pedirAutorizacion() {
                    alcance.launch {
                        when (val r = autenticacion.obtenerTokenDeAcceso()) {
                            is ResultadoAutorizacion.Concedida -> {
                                tokenDeAcceso = r.tokenDeAcceso
                                cargando = false
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

                /** Recarga lo que se muestra despues de guardar o reintentar. */
                suspend fun refrescar() {
                    historial = historialLocal.leer()
                    incidentes = errores.leer()
                }

                val sesionActual = sesion

                // Al abrir con sesion ya guardada, se renueva el token en silencio.
                // Si el permiso sigue concedido, la usuaria no ve nada.
                LaunchedEffect(sesionActual) {
                    if (sesionActual != null && tokenDeAcceso == null) pedirAutorizacion()
                }

                // Cada vez que la app tiene permiso, se vuelven a cuadrar los avisos con
                // lo que dice la hoja. Asi, si la dueña salda una deuda editando a mano,
                // el recordatorio se cancela solo (punto 7).
                LaunchedEffect(tokenDeAcceso) {
                    val token = tokenDeAcceso ?: return@LaunchedEffect
                    val deudas = hoja.ventasConSaldoPendiente(token) ?: return@LaunchedEffect
                    sincronizarRecordatorios(deudas, ultimasDeudas)
                }

                when {
                    sesionActual == null -> PantallaLogin(
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

                    pantalla == Pantalla.ERRORES -> PantallaErrores(
                        incidentes = incidentes,
                        reintentando = reintentando,
                        onReintentar = { incidente ->
                            reintentando = incidente.id
                            alcance.launch {
                                val resultado = interpretarYGuardar(
                                    texto = incidente.textoOriginal,
                                    interprete = interprete,
                                    hoja = hoja,
                                    historial = historialLocal,
                                    errores = errores,
                                    token = tokenDeAcceso,
                                    // Se reutiliza el identificador del intento fallido:
                                    // si aquella escritura si llego a Google pero la
                                    // respuesta se perdio, esto lo detecta (Regla 2).
                                    idExistente = incidente.id,
                                )
                                if (
                                    resultado is EstadoDeGuardado.Guardada ||
                                    resultado is EstadoDeGuardado.YaEstaba
                                ) {
                                    errores.marcarResuelto(incidente.id)
                                }
                                reintentando = null
                                refrescar()
                            }
                        },
                        onBorrarTodo = {
                            alcance.launch {
                                errores.borrarTodo()
                                incidentes = errores.leer()
                            }
                        },
                        onVolver = { pantalla = Pantalla.PRINCIPAL },
                    )

                    pantalla == Pantalla.PRUEBAS -> PantallaPruebas(
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
                        onVolver = { pantalla = Pantalla.PRINCIPAL },
                    )

                    else -> PantallaPrincipal(
                        correo = sesionActual.correo,
                        autorizado = tokenDeAcceso != null,
                        estado = estadoDeGuardado,
                        historial = historial,
                        cuantosErrores = incidentes.count { !it.resuelto },
                        onGuardar = { texto ->
                            estadoDeGuardado = EstadoDeGuardado.Trabajando
                            alcance.launch {
                                estadoDeGuardado = interpretarYGuardar(
                                    texto = texto,
                                    interprete = interprete,
                                    hoja = hoja,
                                    historial = historialLocal,
                                    errores = errores,
                                    token = tokenDeAcceso,
                                )
                                refrescar()
                            }
                        },
                        onVerErrores = { pantalla = Pantalla.ERRORES },
                        onProbarEjemplos = { pantalla = Pantalla.PRUEBAS },
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
     * Garantia del punto 9: si algo falla en cualquiera de las dos, **no se crea
     * ninguna fila** y el incidente queda en el historial de errores.
     */
    private suspend fun interpretarYGuardar(
        texto: String,
        interprete: InterpretePorIA,
        hoja: HojaDeVentas,
        historial: HistorialLocal,
        errores: HistorialDeErrores,
        token: String?,
        idExistente: String? = null,
    ): EstadoDeGuardado {
        // El identificador se genera ANTES de intentar mandar nada, para que un
        // reintento pueda reconocer la venta y no duplicarla (Regla 2).
        val id = idExistente ?: nuevoIdDeRegistro()
        val ahora = System.currentTimeMillis()

        // Sin internet no se puede ni preguntarle a la IA, asi que se guarda el texto
        // tal como se escribio y se interpreta al sincronizar. Lo importante es que el
        // mensaje no se pierda (Tarea 8).
        if (!hayInternet(this)) {
            historial.agregar(
                RegistroLocal(
                    id = id,
                    fecha = ahora,
                    resumen = "\"" + texto.trim() + "\"",
                    estado = EstadoRegistro.PENDIENTE,
                    mensajeOriginal = texto,
                ),
            )
            pedirSincronizacion(applicationContext)
            return EstadoDeGuardado.SinConexion("Sin conexión")
        }

        suspend fun anotarIncidente(tipo: TipoDeIncidente, queFallo: String) {
            errores.agregar(
                Incidente(
                    id = id,
                    fecha = ahora,
                    textoOriginal = texto,
                    queFallo = queFallo,
                    tipo = tipo,
                ),
            )
        }

        return when (val lectura = interprete.interpretar(texto)) {
            is ResultadoInterpretacion.Incompleta -> {
                val falta = lectura.venta.datosQueFaltan.joinToString(" y ")
                // No es un error de la app, asi que no va al historial de errores:
                // va al historial de ventas marcada como incompleta, para que la
                // dueña vea que ese mensaje no quedo registrado.
                historial.agregar(
                    RegistroLocal(
                        id = id,
                        fecha = ahora,
                        resumen = "\"" + texto.trim() + "\"",
                        estado = EstadoRegistro.INCOMPLETA,
                        mensajeOriginal = texto,
                        faltante = "Falta " + falta,
                    ),
                )
                EstadoDeGuardado.Incompleta(falta)
            }

            ResultadoInterpretacion.NoEsUnaVenta -> {
                anotarIncidente(
                    TipoDeIncidente.NO_ES_VENTA,
                    "El mensaje no hablaba de una venta, así que no se registró nada.",
                )
                EstadoDeGuardado.NoEsUnaVenta
            }

            is ResultadoInterpretacion.Fallo -> {
                anotarIncidente(TipoDeIncidente.IA, lectura.mensaje)
                EstadoDeGuardado.Error(lectura.mensaje)
            }

            is ResultadoInterpretacion.Completa -> {
                val resumen = resumenDeVenta(lectura.venta)
                if (token == null) {
                    val motivo =
                        "Falta el permiso para escribir en la hoja. Vuelve a entrar con Google."
                    anotarIncidente(TipoDeIncidente.HOJA, motivo)
                    return EstadoDeGuardado.Error(motivo)
                }

                val guardado = hoja.guardar(lectura.venta, id, token)
                val estado = when (guardado) {
                    ResultadoGuardado.Guardada, ResultadoGuardado.YaEstaba ->
                        EstadoRegistro.GUARDADA
                    is ResultadoGuardado.SinConexion -> EstadoRegistro.PENDIENTE
                    is ResultadoGuardado.Fallo -> null
                }
                // Una venta solo se anota como guardada cuando Google Sheets lo
                // confirmo. Si fallo del todo, no se anota: va al historial de errores.
                if (estado != null) {
                    historial.agregar(
                        RegistroLocal(
                            id = id,
                            fecha = ahora,
                            resumen = resumen,
                            estado = estado,
                            mensajeOriginal = texto,
                            venta = lectura.venta,
                        ),
                    )
                    // Se guarda la venta ya interpretada: al reintentar no hay que
                    // volver a preguntarle a la IA.
                    if (estado == EstadoRegistro.PENDIENTE) {
                        pedirSincronizacion(applicationContext)
                    }
                }

                when (guardado) {
                    ResultadoGuardado.Guardada -> EstadoDeGuardado.Guardada(resumen)
                    ResultadoGuardado.YaEstaba -> EstadoDeGuardado.YaEstaba(resumen)
                    is ResultadoGuardado.SinConexion ->
                        EstadoDeGuardado.SinConexion(guardado.mensaje)
                    is ResultadoGuardado.Fallo -> {
                        anotarIncidente(TipoDeIncidente.HOJA, guardado.mensaje)
                        EstadoDeGuardado.Error(guardado.mensaje)
                    }
                }
            }
        }
    }

    /**
     * Cuadra los avisos programados con las deudas que hoy tiene la hoja.
     *
     * Cancela los de las ventas que ya no deben nada: es lo que hace que un recordatorio
     * desaparezca solo cuando la dueña salda la deuda editando la hoja a mano.
     */
    private suspend fun sincronizarRecordatorios(
        deudas: List<VentaPendienteDePago>,
        copia: UltimasDeudas,
    ) {
        val siguenDebiendo = deudas.map { it.id }.toSet()

        // Lo que estaba en la copia anterior y ya no aparece = deuda saldada.
        copia.leer()
            .filter { it.id !in siguenDebiendo }
            .forEach { ProgramadorDeRecordatorios.cancelarParaVenta(this, it.id) }

        deudas.forEach { ProgramadorDeRecordatorios.programarParaVenta(this, it) }
        copia.guardar(deudas)
    }

    /** Desde Android 13 hay que pedir permiso para poder notificar. */
    private fun pedirPermisoDeAvisos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permiso = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permiso) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permiso), 1)
        }
    }
}
