package com.example.gastospersonales.data

import com.example.gastospersonales.model.Gasto
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de gastos.
 *
 * Intermediario entre GastoDao y el resto de la app. El ViewModel del
 * Sprint 2 solo habla con esta clase: le pide flujos de datos y le pide
 * ejecutar las operaciones de escritura.
 */
class GastoRepositorio(private val gastoDao: GastoDao) {

    fun obtenerPorMes(inicioMes: Long, finMes: Long): Flow<List<Gasto>> =
        gastoDao.obtenerPorMes(inicioMes, finMes)

    fun obtenerTotalPorMes(inicioMes: Long, finMes: Long): Flow<Double> =
        gastoDao.obtenerTotalPorMes(inicioMes, finMes)

    fun obtenerVistaPreviaMes(inicioMes: Long, finMes: Long, limite: Int): Flow<List<Gasto>> =
        gastoDao.obtenerVistaPreviaMes(inicioMes, finMes, limite)

    fun contarPorMes(inicioMes: Long, finMes: Long): Flow<Int> =
        gastoDao.contarPorMes(inicioMes, finMes)

    suspend fun obtenerPorId(id: Long): Gasto? =
        gastoDao.obtenerPorId(id)

    suspend fun insertar(gasto: Gasto) =
        gastoDao.insertar(gasto)

    suspend fun actualizar(gasto: Gasto) =
        gastoDao.actualizar(gasto)

    suspend fun eliminar(gasto: Gasto) =
        gastoDao.eliminar(gasto)
}
