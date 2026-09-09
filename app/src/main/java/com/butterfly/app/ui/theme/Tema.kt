package com.butterfly.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Morado = Color(0xFF8E4EC6)
private val MoradoClaro = Color(0xFFB07CDA)
private val MoradoOscuro = Color(0xFF4A2A6B)

private val ColoresClaros = lightColorScheme(
    primary = Morado,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E9F7),
    onPrimaryContainer = MoradoOscuro,
    secondary = MoradoClaro,
    background = Color(0xFFFDFBFE),
    surface = Color.White,
    surfaceVariant = Color(0xFFF3EDF7),
)

private val ColoresOscuros = darkColorScheme(
    primary = MoradoClaro,
    onPrimary = MoradoOscuro,
    primaryContainer = Color(0xFF5B3A7E),
    onPrimaryContainer = Color(0xFFF3E9F7),
    secondary = Morado,
    background = Color(0xFF15121A),
    surface = Color(0xFF1D1922),
    surfaceVariant = Color(0xFF2A2433),
)

@Composable
fun TemaButterfly(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (oscuro) ColoresOscuros else ColoresClaros,
        content = content,
    )
}
