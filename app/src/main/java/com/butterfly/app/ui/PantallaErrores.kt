package com.butterfly.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.butterfly.app.R
import com.butterfly.app.datos.Incidente
import com.butterfly.app.datos.TipoDeIncidente
import com.butterfly.app.util.horaLegible

/**
 * El historial de errores (punto 9).
 *
 * Todo lo que se muestra aqui son ventas que **no** llegaron a la hoja. Ese es el
 * valor de esta pantalla: nada se pierde en silencio, y desde aqui se puede reintentar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaErrores(
    incidentes: List<Incidente>,
    reintentando: String?,
    onReintentar: (Incidente) -> Unit,
    onBorrarTodo: () -> Unit,
    onVolver: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.errores_titulo)) },
                actions = {
                    if (incidentes.isNotEmpty()) {
                        TextButton(onClick = onBorrarTodo) {
                            Text(stringResource(R.string.errores_limpiar))
                        }
                    }
                    TextButton(onClick = onVolver) {
                        Text(stringResource(R.string.pruebas_volver))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (incidentes.isEmpty()) {
                Text(
                    text = stringResource(R.string.errores_vacio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            Text(
                text = stringResource(R.string.errores_explicacion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            incidentes.forEach { incidente ->
                TarjetaDeIncidente(
                    incidente = incidente,
                    reintentando = reintentando == incidente.id,
                    onReintentar = { onReintentar(incidente) },
                )
            }
        }
    }
}

@Composable
private fun TarjetaDeIncidente(
    incidente: Incidente,
    reintentando: Boolean,
    onReintentar: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (incidente.resuelto) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = (if (incidente.resuelto) "✅ " else "⚠️ ") + incidente.tipo.etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = incidente.queFallo,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "\"${incidente.textoOriginal}\"",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = horaLegible(incidente.fecha),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )

            when {
                incidente.resuelto -> Text(
                    text = stringResource(R.string.errores_ya_resuelto),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
                // Reintentar un texto que no era una venta daria el mismo resultado.
                incidente.tipo == TipoDeIncidente.NO_ES_VENTA -> Text(
                    text = stringResource(R.string.errores_no_reintentable),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
                else -> TextButton(
                    onClick = onReintentar,
                    enabled = !reintentando,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        stringResource(
                            if (reintentando) R.string.errores_reintentando
                            else R.string.errores_reintentar,
                        ),
                    )
                }
            }
        }
    }
}
