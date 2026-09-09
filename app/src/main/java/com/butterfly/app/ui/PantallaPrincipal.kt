package com.butterfly.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.butterfly.app.BuildConfig
import com.butterfly.app.R
import com.butterfly.app.datos.EstadoRegistro
import com.butterfly.app.datos.RegistroLocal
import com.butterfly.app.ui.theme.TemaButterfly
import com.butterfly.app.util.horaLegible

/**
 * La unica pantalla de la app: escribir, guardar y ver lo ultimo guardado.
 *
 * Estado en la Tarea 3: el boton ya manda el texto a la IA y muestra lo que entendio,
 * pero todavia no escribe nada en Google Sheets (eso es la Tarea 4). El historial de
 * abajo sigue con datos de ejemplo hasta la Tarea 5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    correo: String,
    autorizado: Boolean,
    estado: EstadoDeGuardado,
    historial: List<RegistroLocal>,
    onAutorizar: () -> Unit,
    onCerrarSesion: () -> Unit,
    onGuardar: (String) -> Unit,
    onProbarEjemplos: () -> Unit,
) {
    // El texto NO se borra al interpretar: si el registro sale incompleto, la dueña
    // corrige sobre lo que ya escribio en vez de volver a escribirlo (punto 6).
    var texto by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    // El historial de errores se construye en la Tarea 6.
                    TextButton(onClick = { }) { Text(stringResource(R.string.errores)) }
                    TextButton(onClick = onCerrarSesion) {
                        Text(stringResource(R.string.cerrar_sesion))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EstadoDeLaCuenta(correo, autorizado, onAutorizar)

            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.ejemplo_venta)) },
                minLines = 4,
                maxLines = 8,
                enabled = estado !is EstadoDeGuardado.Trabajando,
            )

            Button(
                onClick = { onGuardar(texto) },
                enabled = estado !is EstadoDeGuardado.Trabajando && texto.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (estado is EstadoDeGuardado.Trabajando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.guardar_venta),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            if (estado !is EstadoDeGuardado.Inactivo) TarjetaDeResultado(estado)

            if (BuildConfig.DEBUG) {
                OutlinedButton(
                    onClick = onProbarEjemplos,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.probar_ejemplos))
                }
            }

            Text(
                text = stringResource(R.string.ventas_recientes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (historial.isEmpty()) {
                Text(
                    text = stringResource(R.string.historial_vacio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                historial.forEach { FilaDeVenta(it) }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Lo que paso al guardar. Es el punto 6 de la especificacion. */
@Composable
private fun TarjetaDeResultado(estado: EstadoDeGuardado) {
    val colores = MaterialTheme.colorScheme
    val correcto = estado is EstadoDeGuardado.Guardada || estado is EstadoDeGuardado.YaEstaba
    val fondo = if (correcto) colores.primaryContainer else colores.errorContainer
    val contenido = if (correcto) colores.onPrimaryContainer else colores.onErrorContainer

    val texto = when (estado) {
        is EstadoDeGuardado.Guardada -> "✅ Venta guardada: " + estado.resumen
        is EstadoDeGuardado.YaEstaba ->
            "✅ Esta venta ya estaba guardada: " + estado.resumen + ". No se duplicó."
        is EstadoDeGuardado.Incompleta ->
            "⚠️ No se registró: falta " + estado.faltan + ". Corrige el texto y vuelve a intentar."
        EstadoDeGuardado.NoEsUnaVenta ->
            "⚠️ Ese texto no parece una venta, así que no se registró nada."
        is EstadoDeGuardado.SinConexion ->
            "⏳ Sin conexión. La venta no se ha guardado todavía."
        is EstadoDeGuardado.Error -> "⚠️ " + estado.mensaje
        EstadoDeGuardado.Trabajando, EstadoDeGuardado.Inactivo -> ""
    }
    if (texto.isBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = contenido,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
private fun EstadoDeLaCuenta(correo: String, autorizado: Boolean, onAutorizar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (autorizado) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = correo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    if (autorizado) R.string.cuenta_autorizada else R.string.cuenta_sin_permiso,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            if (!autorizado) {
                TextButton(onClick = onAutorizar, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.dar_permiso))
                }
            }
        }
    }
}

@Composable
private fun FilaDeVenta(registro: RegistroLocal) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Text(text = registro.estado.emoji, modifier = Modifier.padding(end = 10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = registro.resumen, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = buildString {
                        append(horaLegible(registro.fecha))
                        append(" · ")
                        append(registro.estado.etiqueta)
                        registro.faltante?.let { append(" · "); append(it) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (registro.estado == EstadoRegistro.INCOMPLETA) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaPantallaPrincipal() {
    TemaButterfly {
        PantallaPrincipal(
            correo = "dueña@gmail.com",
            autorizado = true,
            estado = EstadoDeGuardado.Inactivo,
            historial = emptyList(),
            onAutorizar = {},
            onCerrarSesion = {},
            onGuardar = {},
            onProbarEjemplos = {},
        )
    }
}
