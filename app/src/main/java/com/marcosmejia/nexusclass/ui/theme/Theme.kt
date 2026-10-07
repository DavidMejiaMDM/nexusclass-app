package com.marcosmejia.nexusclass.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulUA,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE7F5),
    onPrimaryContainer = AzulUAOscuro,
    secondary = IndigoSecundario,
    onSecondary = Color.White,
    tertiary = AcentoCielo,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = Color.White,
    onSurface = TextoPrincipal,
    surfaceVariant = Color(0xFFEEF2F7),
    onSurfaceVariant = TextoSecundario,
    outline = Color(0xFFCBD5E1),
    outlineVariant = BordeSuave,
    error = RojoError,
    onError = Color.White,
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF002A5C),
    primaryContainer = AzulUAOscuro,
    onPrimaryContainer = Color(0xFFDBE7F5),
    secondary = Color(0xFFA5A1FF),
    tertiary = Color(0xFF7DD3FC),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF273449),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    error = Color(0xFFF87171),
)

@Composable
fun NexusClassTheme(
    darkTheme: Boolean = false, // más adelante lo conectamos con Ajustes
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}