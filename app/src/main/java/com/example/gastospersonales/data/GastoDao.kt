package com.example.gastospersonales.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gastospersonales.model.Gasto
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "gastos".
 *
 * Las consultas de lectura devuelven Flow: la lista en pantalla se
 * actualiza sola cada vez que la tabla cambia. Las escrituras son suspend
 * porque Room las ejecuta fuera del hilo principal.
 */
@Dao
interface GastoDao {

    // ----- READ -----

    /**
     * Gastos de un rango de fechas (por ejemplo, un mes), del más reciente
     * al más antiguo. El ViewModel calcula inicioMes y finMes en millis.
     */
    @Query(
        """
        SELECT * FROM gastos
        WHERE fecha >= :inicioMes AND fecha < :finMes
        ORDER BY fecha DESC
        """
    )
    fun obtenerPorMes(inicioMes: Long, finMes: Long): Flow<List<Gasto>>

    /**
     * Total gastado en un rango de fechas. Es un dato DERIVADO: se calcula
     * con SUM en vez de almacenarse. COALESCE devuelve 0.0 si no hay filas.
     */
    @Query(
        """
        SELECT COALESCE(SUM(monto), 0.0) FROM gastos
        WHERE fecha >= :inicioMes AND fecha < :finMes
        """
    )
    fun obtenerTotalPorMes(inicioMes: Long, finMes: Long): Flow<Double>

    /**
     * Los primeros N gastos de un mes vecino (para la vista previa con
     * límite del scroll multi-mes). El ViewModel pasa limite = 5.
     */
    @Query(
        """
        SELECT * FROM gastos
        WHERE fecha >= :inicioMes AND fecha < :finMes
        ORDER BY fecha DESC
        LIMIT :limite
        """
    )
    fun obtenerVistaPreviaMes(inicioMes: Long, finMes: Long, limite: Int): Flow<List<Gasto>>

    /**
     * Cuántos gastos hay en un mes (para el contador "5 de 18").
     */
    @Query(
        """
        SELECT COUNT(*) FROM gastos
        WHERE fecha >= :inicioMes AND fecha < :finMes
        """
    )
    fun contarPorMes(inicioMes: Long, finMes: Long): Flow<Int>

    /**
     * Un gasto por su id (para abrir el detalle o editar).
     */
    @Query("SELECT * FROM gastos WHERE id = :gastoId")
    suspend fun obtenerPorId(gastoId: Long): Gasto?

    // ----- CREATE / UPDATE / DELETE -----

    @Insert
    suspend fun insertar(gasto: Gasto)

    @Update
    suspend fun actualizar(gasto: Gasto)

    @Delete
    suspend fun eliminar(gasto: Gasto)
}