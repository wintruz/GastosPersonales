package com.example.gastospersonales.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Traduce el nombre de ícono guardado en la columna "icono" de la categoría
 * a un ImageVector real de Material Icons.
 *
 * Guardamos un nombre estable en la base de datos (no el ImageVector, que no
 * es serializable) y lo resolvemos aquí. Si el nombre no coincide, se usa un
 * ícono por defecto para no romper la interfaz.
 */
object IconosCategoria {

    fun desde(nombreIcono: String): ImageVector = when (nombreIcono) {
        "shopping-cart" -> Icons.Filled.ShoppingCart
        "gas-station" -> Icons.Filled.LocalGasStation
        "device-tv" -> Icons.Filled.Tv
        "basket" -> Icons.Filled.LocalHospital
        "bolt" -> Icons.Filled.Bolt
        "bus" -> Icons.Filled.DirectionsBus
        "restaurant" -> Icons.Filled.Restaurant
        else -> Icons.Filled.MoreHoriz // "dots" y cualquier desconocido
    }
}