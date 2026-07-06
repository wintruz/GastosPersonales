package com.example.gastospersonales.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gastospersonales.model.Categoria
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "categorias".
 *
 * Además del CRUD, contiene la lógica de borrado seguro: una transacción
 * que reasigna los gastos a la categoría "Otro" antes de eliminar, de modo
 * que la clave foránea RESTRICT nunca se dispare.
 */
@Dao
interface CategoriaDao {

    // ----- READ -----

    /** Todas las categorías, ordenadas por nombre. Reactivo. */
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<Categoria>>

    /** Una categoría por id. */
    @Query("SELECT * FROM categorias WHERE id = :categoriaId")
    suspend fun obtenerPorId(categoriaId: Long): Categoria?

    /** La categoría de sistema "Otro", destino de respaldo de reasignaciones. */
    @Query("SELECT * FROM categorias WHERE esSistema = 1 LIMIT 1")
    suspend fun obtenerCategoriaOtro(): Categoria?

    /** Cuántos gastos usan una categoría (para decidir si hay que reasignar). */
    @Query("SELECT COUNT(*) FROM gastos WHERE categoriaId = :categoriaId")
    suspend fun contarGastosDeCategoria(categoriaId: Long): Int

    // ----- CREATE / UPDATE -----

    // IGNORE evita que un nombre duplicado (índice único) rompa la app;
    // simplemente no inserta. Útil al precargar las categorías iniciales.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(categoria: Categoria): Long

    @Update
    suspend fun actualizar(categoria: Categoria)

    // ----- Operaciones internas de la transacción -----

    // Reasigna en bloque todos los gastos de una categoría a otra.
    @Query("UPDATE gastos SET categoriaId = :destinoId WHERE categoriaId = :origenId")
    suspend fun reasignarGastos(origenId: Long, destinoId: Long)

    // Borra la categoría por id. Se llama solo cuando ya no tiene gastos.
    @Query("DELETE FROM categorias WHERE id = :categoriaId")
    suspend fun eliminarPorId(categoriaId: Long)

    // ----- DELETE seguro (orquestador transaccional) -----

    /**
     * Elimina una categoría de forma segura.
     *
     * @Transaction agrupa todo en una sola operación atómica: o se completan
     * todos los pasos, o no se aplica ninguno (si algo falla, se revierte).
     *
     * Pasos:
     *  1. No permite borrar la categoría de sistema "Otro".
     *  2. Si la categoría tiene gastos, los reasigna a "Otro".
     *  3. Elimina la categoría, que a esa altura ya no tiene gastos, por lo
     *     que la clave foránea RESTRICT no se activa.
     *
     * Devuelve el número de gastos reasignados, para que la interfaz pueda
     * confirmarlo en la alerta ("N gastos pasaron a Otro").
     */
    @Transaction
    suspend fun eliminarConReasignacion(categoria: Categoria): Int {
        // 1. La categoría "Otro" no se elimina.
        if (categoria.esSistema) return 0

        // 2. Reasignar los gastos, si los hay.
        val cantidad = contarGastosDeCategoria(categoria.id)
        if (cantidad > 0) {
            val otro = obtenerCategoriaOtro()
                ?: throw IllegalStateException(
                    "No existe la categoría de sistema 'Otro' para reasignar."
                )
            reasignarGastos(origenId = categoria.id, destinoId = otro.id)
        }

        // 3. Borrar la categoría ya vacía.
        eliminarPorId(categoria.id)
        return cantidad
    }
}