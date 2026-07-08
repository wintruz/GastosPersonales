package com.example.gastospersonales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastospersonales.data.GastoRepositorio
import com.example.gastospersonales.model.Gasto
import com.example.gastospersonales.util.GestorArchivos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Representa el mes visible en pantalla. mes usa la convención de
 * Calendar.MONTH (0 = enero .. 11 = diciembre) para no reconvertir al
 * llamar a la API de Calendar.
 */
data class MesAnio(val anio: Int, val mes: Int)

/**
 * ViewModel de la agenda de gastos. Solo conoce GastoRepositorio: la
 * lista de categorías vive aparte, en CategoriaViewModel.
 *
 * Sobrevive a los cambios de configuración (como una rotación) porque
 * Android conserva la misma instancia mientras la Activity se recrea;
 * solo se destruye cuando la pantalla desaparece de verdad.
 *
 * Patrón general de cada propiedad expuesta:
 *   Flow del repositorio -> stateIn -> StateFlow de solo lectura
 * stateIn convierte un Flow "frío" (que no hace nada hasta que alguien
 * lo colecta) en un StateFlow "caliente" que siempre tiene un último
 * valor listo para leer, incluso si la pantalla todavía no se suscribió.
 */
class GastoViewModel(
    private val gastoRepositorio: GastoRepositorio
) : ViewModel() {

    // ----- Mes visible -----

    private val _mesActual = MutableStateFlow(mesDeHoy())
    val mesActual: StateFlow<MesAnio> = _mesActual.asStateFlow()

    // ----- Gastos y total del mes visible -----
    //
    // flatMapLatest reacciona a dos cosas a la vez: un cambio de mes
    // (nueva emisión de _mesActual) o un cambio en la tabla gastos
    // (nueva emisión del Flow de Room). "Latest" cancela la colecta del
    // mes anterior al cambiar de mes, para no seguir escuchando datos
    // que ya no se muestran.

    @OptIn(ExperimentalCoroutinesApi::class)
    val gastosDelMes: StateFlow<List<Gasto>> = _mesActual
        .flatMapLatest { mes ->
            val (inicio, fin) = rangoDeMes(mes)
            gastoRepositorio.obtenerPorMes(inicio, fin)
        }
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed(5000): sigue coleccionando 5s después de que
            // la última pantalla se desuscribe, para sobrevivir a una
            // rotación sin relanzar la consulta desde cero.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalDelMes: StateFlow<Double> = _mesActual
        .flatMapLatest { mes ->
            val (inicio, fin) = rangoDeMes(mes)
            gastoRepositorio.obtenerTotalPorMes(inicio, fin)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val contadorDelMes: StateFlow<Int> = _mesActual
        .flatMapLatest { mes ->
            val (inicio, fin) = rangoDeMes(mes)
            gastoRepositorio.contarPorMes(inicio, fin)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    // ----- Navegación entre meses -----

    fun mesSiguiente() = moverMes(1)

    fun mesAnterior() = moverMes(-1)

    private fun moverMes(delta: Int) {
        _mesActual.update { actual ->
            val calendario = Calendar.getInstance().apply {
                set(actual.anio, actual.mes, 1)
                add(Calendar.MONTH, delta)
            }
            MesAnio(calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH))
        }
    }

    /**
     * Salta directamente a un mes/año concreto (elegido en el selector
     * emergente). Reemplaza el mes visible; los Flow con flatMapLatest
     * recargan solos los gastos y el total del nuevo mes.
     */
    fun irAMes(mesAnio: MesAnio) {
        _mesActual.value = mesAnio
    }

    // ----- Lectura puntual -----

    /**
     * Devuelve un gasto por su id, o null si no existe. Es suspend porque
     * lee un valor puntual (no un Flow); el formulario y el detalle la
     * llaman dentro de un LaunchedEffect.
     */
    suspend fun obtenerPorId(id: Long): Gasto? = gastoRepositorio.obtenerPorId(id)

    // ----- Escritura de gastos -----
    //
    // Cada función lanza una corrutina en viewModelScope: la escritura
    // ocurre en segundo plano y viewModelScope la cancela sola si el
    // ViewModel se destruye antes de terminar.

    fun agregarGasto(gasto: Gasto) {
        viewModelScope.launch {
            gastoRepositorio.insertar(gasto)
        }
    }

    fun actualizarGasto(gasto: Gasto) {
        viewModelScope.launch {
            gastoRepositorio.actualizar(gasto)
        }
    }

    /**
     * Elimina el gasto y, si tenía una foto de recibo, borra también el
     * archivo en filesDir (Sprint 4): así no queda un archivo huérfano sin
     * ningún gasto que lo referencie. GestorArchivos.eliminarSiExiste no
     * necesita Context: trabaja directo sobre la ruta absoluta guardada.
     */
    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch {
            gastoRepositorio.eliminar(gasto)
            GestorArchivos.eliminarSiExiste(gasto.rutaRecibo)
        }
    }

    // ----- Cálculo de rango de mes -----

    private fun mesDeHoy(): MesAnio {
        val calendario = Calendar.getInstance()
        return MesAnio(calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH))
    }

    /**
     * Convierte un MesAnio en el rango [inicio, fin) en milisegundos que
     * esperan las consultas del DAO: desde el día 1 del mes a las 00:00,
     * hasta el día 1 del mes siguiente (exclusivo).
     */
    private fun rangoDeMes(mesAnio: MesAnio): Pair<Long, Long> {
        val inicio = Calendar.getInstance().apply {
            set(mesAnio.anio, mesAnio.mes, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fin = (inicio.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        return inicio.timeInMillis to fin.timeInMillis
    }
}
