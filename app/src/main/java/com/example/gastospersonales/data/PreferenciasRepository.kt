package com.example.gastospersonales.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
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
 * Repositorio de preferencias del usuario. Es una fuente de datos más,
 * como CategoriaRepositorio o GastoRepositorio, aunque use DataStore en
 * vez de Room por debajo (ver Sprint 1, sección 1.2).
 */
class PreferenciasRepository(private val context: Context) {

    private object Claves {
        val TEMA = stringPreferencesKey("tema")
        val MONEDA = stringPreferencesKey("moneda")
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

    suspend fun guardarTema(tema: TemaPreferido) {
        context.dataStorePreferencias.edit { it[Claves.TEMA] = tema.name }
    }

    suspend fun guardarMoneda(codigo: String) {
        context.dataStorePreferencias.edit { it[Claves.MONEDA] = codigo }
    }
}
