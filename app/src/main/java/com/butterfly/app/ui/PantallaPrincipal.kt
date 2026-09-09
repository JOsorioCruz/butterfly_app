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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.butterfly.app.R
import com.butterfly.app.modelo.EstadoVenta
import com.butterfly.app.modelo.VENTAS_DE_EJEMPLO
import com.butterfly.app.modelo.VentaReciente
import com.butterfly.app.ui.theme.TemaButterfly

/**
 * La unica pantalla de la app: escribir, guardar y ver lo ultimo guardado.
 *
 * TAREA 1: todavia no guarda nada de verdad. El boton solo agrega la venta al
 * historial en memoria para poder juzgar el diseno. La interpretacion con IA llega
 * en la Tarea 3 y el guardado real en Sheets en la Tarea 4.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal() {
    var texto by remember { mutableStateOf("") }
    val historial = remember { VENTAS_DE_EJEMPLO.toMutableStateList() }
    // Marcador de hora para la Tarea 1; en la Tarea 5 se usa la hora real.
    val ahora = stringResource(R.string.ahora)

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
                    // Punto de entrada al historial de errores. La pantalla en si
                    // se construye en la Tarea 6.
                    TextButton(onClick = { }) {
                        Text(stringResource(R.string.errores))
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
            AvisoDeDiseno()

            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.ejemplo_venta)) },
                minLines = 4,
                maxLines = 8,
            )

            Button(
                onClick = {
                    if (texto.isBlank()) return@Button
                    historial.add(
                        0,
                        VentaReciente(
                            id = "simulada-${historial.size}",
                            resumen = "\"${texto.trim()}\"",
                            hora = ahora,
                            estado = EstadoVenta.PENDIENTE,
                        ),
                    )
                    texto = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    text = stringResource(R.string.guardar_venta),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Text(
                text = stringResource(R.string.ventas_recientes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )

            historial.forEach { venta ->
                FilaDeVenta(venta)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AvisoDeDiseno() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Text(
            text = stringResource(R.string.aviso_diseno),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun FilaDeVenta(venta: VentaReciente) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = venta.estado.emoji,
                modifier = Modifier.padding(end = 10.dp),
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = venta.resumen,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = buildString {
                        append(venta.hora)
                        append(" · ")
                        append(venta.estado.etiqueta)
                        venta.faltante?.let {
                            append(" · ")
                            append(it)
                        }
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
    TemaButterfly { PantallaPrincipal() }
}
