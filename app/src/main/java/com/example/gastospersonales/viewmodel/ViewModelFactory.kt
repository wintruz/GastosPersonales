package com.example.gastospersonales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gastospersonales.data.CategoriaRepositorio
import com.example.gastospersonales.data.GastoRepositorio

/**
 * Fábrica única para los ViewModel de la app.
 *
 * Se queda igual que en el Sprint 2: solo GastoViewModel y CategoriaViewModel
 * pasan por aquí. Las preferencias (Sprint 4) no tienen ViewModel propio —
 * ConfiguracionScreen usa PreferenciasRepository directamente, porque no
 * estaba reservado en el árbol de paquetes y no hacía falta uno.
 */
class ViewModelFactory(
    private val gastoRepositorio: GastoRepositorio,
    private val categoriaRepositorio: CategoriaRepositorio
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(GastoViewModel::class.java) ->
                GastoViewModel(gastoRepositorio) as T

            modelClass.isAssignableFrom(CategoriaViewModel::class.java) ->
                CategoriaViewModel(categoriaRepositorio) as T

            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
