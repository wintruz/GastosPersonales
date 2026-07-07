package com.example.gastospersonales.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formatea montos como moneda. Por ahora usa el símbolo de dólar y la
 * configuración regional del dispositivo. En el Sprint 4, la moneda
 * elegida en Configuración (DataStore) podrá cambiar el símbolo.
 */
object FormatoMoneda {

    private val formato: NumberFormat =
        NumberFormat.getCurrencyInstance(
            Locale.Builder().setLanguage("es").setRegion("PA").build()
        ).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

    /** Devuelve, por ejemplo, "$62.30". */
    fun formatear(monto: Double): String = formato.format(monto)
}