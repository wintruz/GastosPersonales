package com.example.gastospersonales.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Tema Material 3 de la app. Mapea los tokens de Color.kt a los roles de
 * un ColorScheme, de modo que todos los componentes (Scaffold, Button,
 * Card, TextField…) tomen los colores correctos automáticamente.
 *
 * Por ahora sigue el tema del sistema (isSystemInDarkTheme). En el Sprint 4,
 * la preferencia de tema guardada en DataStore podrá forzar claro u oscuro
 * pasando un valor explícito a "oscuro".
 */

private val EsquemaClaro = lightColorScheme(
    primary = AccentL,
    onPrimary = Surface2L,
    primaryContainer = BgAccentL,
    onPrimaryContainer = AccentL,
    background = Surface1L,
    onBackground = TextPrimaryL,
    surface = Surface2L,
    onSurface = TextPrimaryL,
    surfaceVariant = Surface0L,
    onSurfaceVariant = TextSecondaryL,
    outline = BorderL,
    outlineVariant = BorderL,
    error = DangerL,
    onError = Surface2L,
    errorContainer = BgDangerL,
    onErrorContainer = DangerL
)

private val EsquemaOscuro = darkColorScheme(
    primary = AccentD,
    onPrimary = Surface0D,
    primaryContainer = BgAccentD,
    onPrimaryContainer = AccentD,
    background = Surface1D,
    onBackground = TextPrimaryD,
    surface = Surface2D,
    onSurface = TextPrimaryD,
    surfaceVariant = Surface0D,
    onSurfaceVariant = TextSecondaryD,
    outline = BorderD,
    outlineVariant = BorderD,
    error = DangerD,
    onError = Surface0D,
    errorContainer = BgDangerD,
    onErrorContainer = DangerD
)

@Composable
fun GastosPersonalesTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}