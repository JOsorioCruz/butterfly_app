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
import com.butterfly.app.ia.ResultadoInterpretacion
import com.butterfly.app.modelo.EstadoVenta
import com.butterfly.app.modelo.VENTAS_DE_EJEMPLO
import com.butterfly.app.modelo.VentaReciente
import com.butterfly.app.ui.theme.TemaButterfly
import com.butterfly.app.util.resumenDeVenta

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
    interpretando: Boolean,
    resultado: ResultadoInterpretacion?,
    onAutorizar: () -> Unit,
    onCerrarSesion: () -> Unit,
    onInterpretar: (String) -> Unit,
    onProbarEjemplos: () -> Unit,
) {
    // El texto NO se borra al interpretar: si el registro sale incompleto, la dueña
    // corrige sobre lo que ya escribio en vez de volver a escribirlo (punto 6).
    var texto by remember { mutableStateOf("") }
    val historial = remember { VENTAS_DE_EJEMPLO.toList() }

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
                enabled = !interpretando,
            )

            Button(
                onClick = { onInterpretar(texto) },
                enabled = !interpretando && texto.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (interpretando) {
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

            if (resultado != null) TarjetaDeResultado(resultado)

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

            historial.forEach { FilaDeVenta(it) }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Lo que la IA entendio, o por que no pudo. Es el punto 6 de la especificacion. */
@Composable
private fun TarjetaDeResultado(resultado: ResultadoInterpretacion) {
    val colores = MaterialTheme.colorScheme
    val (fondo, contenido) = when (resultado) {
        is ResultadoInterpretacion.Completa -> colores.primaryContainer to colores.onPrimaryContainer
        else -> colores.errorContainer to colores.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            when (resultado) {
                is ResultadoInterpretacion.Completa -> {
                    Text(
                        text = "✅ " + resumenDeVenta(resultado.venta),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contenido,
                    )
                    Text(
                        text = stringResource(R.string.todavia_no_se_guarda),
                        style = MaterialTheme.typography.bodySmall,
                        color = contenido,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                is ResultadoInterpretacion.Incompleta -> Text(
                    text = "⚠️ " + stringResource(
                        R.string.falta_dato,
                        resultado.venta.datosQueFaltan.joinToString(" y "),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contenido,
                )

                ResultadoInterpretacion.NoEsUnaVenta -> Text(
                    text = "⚠️ " + stringResource(R.string.no_es_venta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contenido,
                )

                is ResultadoInterpretacion.Fallo -> Text(
                    text = "⚠️ " + resultado.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contenido,
                )
            }
        }
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
private fun FilaDeVenta(venta: VentaReciente) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Text(text = venta.estado.emoji, modifier = Modifier.padding(end = 10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = venta.resumen, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = buildString {
                        append(venta.hora)
                        append(" · ")
                        append(venta.estado.etiqueta)
                        venta.faltante?.let { append(" · "); append(it) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (venta.estado == EstadoVenta.INCOMPLETA) {
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
            interpretando = false,
            resultado = null,
            onAutorizar = {},
            onCerrarSesion = {},
            onInterpretar = {},
            onProbarEjemplos = {},
        )
    }
}
