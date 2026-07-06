package com.example.gastospersonales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gastospersonales.data.CategoriaRepositorio
import com.example.gastospersonales.data.GastoRepositorio

/**
 * Fábrica única para todos los ViewModel de la app.
 *
 * Android solo sabe crear ViewModel con constructor vacío. Como
 * GastoViewModel y CategoriaViewModel reciben su repositorio por
 * constructor, hace falta esta fábrica para decirle a Android cómo
 * construir cada uno.
 *
 * Se usará en el Sprint 3 así, desde una pantalla Compose:
 *   val app = LocalContext.current.applicationContext as GestorGastosApp
 *   val factory = ViewModelFactory(app.gastoRepositorio, app.categoriaRepositorio)
 *   val gastoViewModel: GastoViewModel = viewModel(factory = factory)
 *   val categoriaViewModel: CategoriaViewModel = viewModel(factory = factory)
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
