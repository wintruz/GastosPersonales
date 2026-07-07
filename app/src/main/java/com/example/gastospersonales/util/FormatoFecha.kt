package com.example.gastospersonales.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Conversión entre milisegundos epoch (como se guardan en la BD) y texto
 * legible. Centralizar esto evita repetir SimpleDateFormat por todo el código.
 */
object FormatoFecha {

    private val LOCALE = Locale.Builder().setLanguage("es").setRegion("ES").build()

    // Fecha larga: "4 de julio, 2026"
    private val fechaLarga = SimpleDateFormat("d 'de' MMMM, yyyy", LOCALE)

    // Encabezado de mes: "Julio 2026"
    private val mesAnio = SimpleDateFormat("MMMM yyyy", LOCALE)

    // Etiqueta de día corta: "4 jul"
    private val diaCorto = SimpleDateFormat("d MMM", LOCALE)

    fun fechaLarga(millis: Long): String =
        fechaLarga.format(Date(millis)).replaceFirstChar { it.titlecase(LOCALE) }

    fun mesAnio(anio: Int, mes: Int): String {
        val cal = Calendar.getInstance().apply { set(anio, mes, 1) }
        return mesAnio.format(cal.time).replaceFirstChar { it.titlecase(LOCALE) }
    }

    fun diaCorto(millis: Long): String =
        diaCorto.format(Date(millis))

    /** Milisegundos correspondientes al día de hoy a las 00:00. */
    fun hoyEnMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Extrae el día del mes (1..31) de un instante en millis. */
    fun diaDelMes(millis: Long): Int = Calendar.getInstance().apply {
        timeInMillis = millis
    }.get(Calendar.DAY_OF_MONTH)
}