package com.example.gastospersonales.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de color de la app, tomados directamente del sistema de color
 * definido en el Sprint 1 (paleta neutra de grises cálidos + acento azul).
 *
 * El sufijo "L" marca los valores del tema claro y "D" los del oscuro.
 * Theme.kt los agrupa en un ColorScheme para cada modo.
 */

// ----- Tema claro -----
val Surface0L = Color(0xFFF1EFE8) // fondos de mayor jerarquía / hundidos
val Surface1L = Color(0xFFFAF9F5) // fondo general de la app
val Surface2L = Color(0xFFFFFFFF) // tarjetas y superficies elevadas
val TextPrimaryL = Color(0xFF2C2C2A)
val TextSecondaryL = Color(0xFF5F5E5A)
val TextMutedL = Color(0xFF888780)
val BorderL = Color(0xFFD3D1C7)
val AccentL = Color(0xFF185FA5)
val BgAccentL = Color(0xFFE6F1FB)
val DangerL = Color(0xFFA32D2D)
val BgDangerL = Color(0xFFFCEBEB)

// ----- Tema oscuro -----
val Surface0D = Color(0xFF1F1E1D)
val Surface1D = Color(0xFF262624)
val Surface2D = Color(0xFF30302E)
val TextPrimaryD = Color(0xFFF1EFE8)
val TextSecondaryD = Color(0xFFB4B2A9)
val TextMutedD = Color(0xFF888780)
val BorderD = Color(0xFF444441)
val AccentD = Color(0xFF85B7EB)
val BgAccentD = Color(0xFF1C3345)
val DangerD = Color(0xFFF09595)
val BgDangerD = Color(0xFF3D2626)