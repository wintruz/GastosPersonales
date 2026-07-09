package com.example.gastospersonales.util

import com.example.gastospersonales.data.FormatoFechaPreferido
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

    // Etiqueta de día corta: "4 jul" (se usa en encabezados de agrupación de
    // la lista; no depende de la preferencia de formato del usuario, porque
    // ahí cumple un rol distinto: separar visualmente los días, no comunicar
    // la fecha completa).
    private val diaCorto = SimpleDateFormat("d MMM", LOCALE)

    // Formatos cortos numéricos para la preferencia de Configuración.
    private val diaMesAnio = SimpleDateFormat("dd/MM/yyyy", LOCALE)
    private val mesDiaAnio = SimpleDateFormat("MM/dd/yyyy", LOCALE)

    fun fechaLarga(millis: Long): String =
        fechaLarga.format(Date(millis)).replaceFirstChar { it.titlecase(LOCALE) }

    fun mesAnio(anio: Int, mes: Int): String {
        val cal = Calendar.getInstance().apply { set(anio, mes, 1) }
        return mesAnio.format(cal.time).replaceFirstChar { it.titlecase(LOCALE) }
    }

    fun diaCorto(millis: Long): String =
        diaCorto.format(Date(millis))

    /**
     * Formatea una fecha según la preferencia elegida en Configuración.
     * Es la función que deben usar los campos que muestran una fecha
     * completa al usuario (el campo Fecha del formulario, el detalle de un
     * gasto). Los encabezados de agrupación de la lista siguen usando
     * diaCorto(), que es un formato de agrupación, no de lectura de fecha.
     */
    fun formatear(millis: Long, formato: FormatoFechaPreferido): String = when (formato) {
        FormatoFechaPreferido.DIA_MES_ANIO -> diaMesAnio.format(Date(millis))
        FormatoFechaPreferido.MES_DIA_ANIO -> mesDiaAnio.format(Date(millis))
        FormatoFechaPreferido.LARGO -> fechaLarga(millis)
    }

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

    /**
     * Milisegundos del primer día de un mes, a las 00:00. Se usa para abrir
     * el selector de fecha en un mes concreto (por ejemplo, el que se
     * estaba viendo en la lista) sin preseleccionar ningún día dentro de él.
     */
    fun primerDiaDeMes(anio: Int, mes: Int): Long = Calendar.getInstance().apply {
        set(anio, mes, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}