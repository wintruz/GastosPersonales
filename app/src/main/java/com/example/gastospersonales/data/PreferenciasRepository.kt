package com.example.gastospersonales.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Delegate a nivel de archivo: crea el DataStore de preferencias una sola
 * vez por proceso, la primera vez que algo lo usa.
 */
private val Context.dataStorePreferencias by preferencesDataStore(name = "preferencias_app")

/** Preferencia de tema: SISTEMA sigue isSystemInDarkTheme(), como hasta ahora. */
enum class TemaPreferido { SISTEMA, CLARO, OSCURO }

/**
 * Preferencia de formato de fecha. DIA_MES_ANIO = "09/07/2026",
 * MES_DIA_ANIO = "07/09/2026", LARGO = "9 de julio, 2026". FormatoFecha.kt
 * traduce cada valor al patrón de fecha correspondiente.
 */
enum class FormatoFechaPreferido { DIA_MES_ANIO, MES_DIA_ANIO, LARGO }

/**
 * Repositorio de preferencias del usuario. Es una fuente de datos más,
 * como CategoriaRepositorio o GastoRepositorio, aunque use DataStore en
 * vez de Room por debajo (ver Sprint 1, sección 1.2).
 */
class PreferenciasRepository(private val context: Context) {

    private object Claves {
        val TEMA = stringPreferencesKey("tema")
        val MONEDA = stringPreferencesKey("moneda")
        val FORMATO_FECHA = stringPreferencesKey("formato_fecha")
    }

    val tema: Flow<TemaPreferido> = context.dataStorePreferencias.data.map { prefs ->
        when (prefs[Claves.TEMA]) {
            TemaPreferido.CLARO.name -> TemaPreferido.CLARO
            TemaPreferido.OSCURO.name -> TemaPreferido.OSCURO
            else -> TemaPreferido.SISTEMA
        }
    }

    /** Código de moneda para FormatoMoneda (por ejemplo "USD" o "PAB"). */
    val moneda: Flow<String> = context.dataStorePreferencias.data.map { prefs ->
        prefs[Claves.MONEDA] ?: "USD"
    }

    val formatoFecha: Flow<FormatoFechaPreferido> = context.dataStorePreferencias.data.map { prefs ->
        when (prefs[Claves.FORMATO_FECHA]) {
            FormatoFechaPreferido.MES_DIA_ANIO.name -> FormatoFechaPreferido.MES_DIA_ANIO
            FormatoFechaPreferido.LARGO.name -> FormatoFechaPreferido.LARGO
            else -> FormatoFechaPreferido.DIA_MES_ANIO
        }
    }

    suspend fun guardarTema(tema: TemaPreferido) {
        context.dataStorePreferencias.edit { it[Claves.TEMA] = tema.name }
    }

    suspend fun guardarMoneda(codigo: String) {
        context.dataStorePreferencias.edit { it[Claves.MONEDA] = codigo }
    }

    suspend fun guardarFormatoFecha(formato: FormatoFechaPreferido) {
        context.dataStorePreferencias.edit { it[Claves.FORMATO_FECHA] = formato.name }
    }
}