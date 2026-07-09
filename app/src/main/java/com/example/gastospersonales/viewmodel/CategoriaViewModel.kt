package com.example.gastospersonales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastospersonales.data.CategoriaRepositorio
import com.example.gastospersonales.model.Categoria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de categorías. Va separado de GastoViewModel porque las
 * categorías tienen su propio ciclo de vida (se listan, crean y borran
 * de forma independiente al mes que se esté viendo en la agenda).
 */
class CategoriaViewModel(
    private val categoriaRepositorio: CategoriaRepositorio
) : ViewModel() {

    /** Lista reactiva de categorías, ya lista para un selector o un menú. */
    val categorias: StateFlow<List<Categoria>> = categoriaRepositorio.categorias
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun agregarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            categoriaRepositorio.insertar(categoria)
        }
    }

    fun actualizarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            categoriaRepositorio.actualizar(categoria)
        }
    }

    /**
     * Elimina una categoría reasignando antes sus gastos a "Otro".
     * alTerminar recibe cuántos gastos fueron reasignados, para que la
     * pantalla pueda mostrarlo en una confirmación (ej. "3 gastos pasaron
     * a Otro").
     */
    fun eliminarCategoria(categoria: Categoria, alTerminar: (cantidadReasignada: Int) -> Unit = {}) {
        viewModelScope.launch {
            val cantidad = categoriaRepositorio.eliminar(categoria)
            alTerminar(cantidad)
        }
    }

    /**
     * Cuántos gastos usan esta categoría (Sprint 4). Es suspend porque lee
     * un valor puntual, no un Flow; GestionCategoriasScreen la llama dentro
     * de un LaunchedEffect al pedir eliminar, para mostrar el número exacto
     * de gastos que se reasignarán antes de que el usuario confirme.
     */
    suspend fun contarGastosAsociados(categoriaId: Long): Int =
        categoriaRepositorio.contarGastosAsociados(categoriaId)
}