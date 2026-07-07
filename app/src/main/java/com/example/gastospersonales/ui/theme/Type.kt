package com.example.gastospersonales.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía de la app. Se apoya en la fuente por defecto del sistema y
 * ajusta tamaños/pesos para los estilos que más se usan en las pantallas
 * (títulos de barra, montos destacados, cuerpo de listas).
 */
val Tipografia = Typography(
    // Monto grande del encabezado y del detalle.
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    // Títulos de barra superior.
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // Nombre del gasto en la fila.
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    // Categoría y textos secundarios.
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // Etiquetas pequeñas (fechas, contadores).
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)