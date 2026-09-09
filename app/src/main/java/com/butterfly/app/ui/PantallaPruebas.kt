package com.butterfly.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.butterfly.app.R

/** El veredicto de un caso del banco de pruebas. */
data class ResultadoDeCaso(
    val descripcion: String,
    val texto: String,
    val paso: Boolean,
    val loQueEntendio: String,
    val diferencias: List<String>,
)

/**
 * Pantalla de la **Validacion B**: corre los 15 ejemplos contra la IA de verdad y
 * muestra, caso por caso, lo que se escribio frente a lo que la IA entendio.
 *
 * Solo existe en la version de depuracion; no llega al APK que usa la dueña.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPruebas(
    corriendo: Boolean,
    resultados: List<ResultadoDeCaso>,
    onEjecutar: () -> Unit,
    onVolver: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pruebas_titulo)) },
                actions = {
                    TextButton(onClick = onVolver) {
                        Text(stringResource(R.string.pruebas_volver))
                    }
                },
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
            if (resultados.isNotEmpty()) {
                val pasaron = resultados.count { it.paso }
                Text(
                    text = "$pasaron de ${resultados.size} correctos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (pasaron == resultados.size) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            if (corriendo) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onEjecutar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.pruebas_ejecutar))
                }
            }

            resultados.forEach { caso ->
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = (if (caso.paso) "✅ " else "❌ ") + caso.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Se escribió: \"${caso.texto}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = "La IA entendió: ${caso.loQueEntendio}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    caso.diferencias.forEach {
                        Text(
                            text = "· $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
                }
            }
        }
    }
}
