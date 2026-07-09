package com.example.gastospersonales.data

import com.example.gastospersonales.model.Categoria
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de categorías.
 *
 * Es la única puerta de entrada a CategoriaDao: el ViewModel no conoce
 * Room ni SQL, solo conoce estas funciones. Esto permite, por ejemplo,
 * cambiar de Room a otra fuente de datos sin tocar el ViewModel.
 */
class CategoriaRepositorio(private val categoriaDao: CategoriaDao) {

    /** Lista reactiva de categorías, ordenadas por nombre. */
    val categorias: Flow<List<Categoria>> = categoriaDao.obtenerTodas()

    suspend fun obtenerPorId(id: Long): Categoria? =
        categoriaDao.obtenerPorId(id)

    suspend fun insertar(categoria: Categoria): Long =
        categoriaDao.insertar(categoria)

    suspend fun actualizar(categoria: Categoria) =
        categoriaDao.actualizar(categoria)

    /**
     * Elimina la categoría reasignando primero sus gastos a "Otro".
     * Devuelve cuántos gastos fueron reasignados, para que la interfaz
     * pueda mostrarlo en una confirmación.
     */
    suspend fun eliminar(categoria: Categoria): Int =
        categoriaDao.eliminarConReasignacion(categoria)

    /**
     * Cuántos gastos usan esta categoría (Sprint 4). Se usa ANTES de pedir
     * confirmación de borrado, para poder avisar con el número exacto de
     * gastos que se reasignarán a "Otro" ("Se reasignarán 3 gastos"),
     * en vez de un mensaje genérico.
     */
    suspend fun contarGastosAsociados(categoriaId: Long): Int =
        categoriaDao.contarGastosDeCategoria(categoriaId)
}