package com.example.gastospersonales.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Formatea montos como moneda.
 *
 * La configuración regional (separadores, orden de símbolo y número) se
 * mantiene fija en es-PA. Lo que sí cambia, desde el Sprint 4, es la
 * moneda en sí (símbolo y redondeo), según el código ISO 4217 que llega
 * desde PreferenciasRepository.moneda (guardado en Configuración).
 */
object FormatoMoneda {

    private val localeRegional: Locale = Locale.Builder().setLanguage("es").setRegion("PA").build()

    /**
     * Devuelve, por ejemplo, "$62.30" (USD) o "B/.62.30" (PAB), según
     * codigoMoneda. Si el código no es válido, cae a USD para no romper
     * la pantalla por una preferencia corrupta.
     */

     fun simbolo(codigoMoneda: String = "USD"): String {
        val moneda = try {
            Currency.getInstance(codigoMoneda)
        } catch (e: IllegalArgumentException) {
            Currency.getInstance("USD")
        }
        return moneda.getSymbol(localeRegional)
    }

    fun formatear(monto: Double, codigoMoneda: String = "USD"): String {
        val moneda = try {
            Currency.getInstance(codigoMoneda)
        } catch (e: IllegalArgumentException) {
            Currency.getInstance("USD")
        }
        val formato = NumberFormat.getCurrencyInstance(localeRegional).apply {
            currency = moneda
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
        return formato.format(monto)
    }
}
