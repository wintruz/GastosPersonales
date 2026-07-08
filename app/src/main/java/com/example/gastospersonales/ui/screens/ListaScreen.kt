package com.example.gastospersonales.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gastospersonales.model.Categoria
import com.example.gastospersonales.ui.components.GastoItem
import com.example.gastospersonales.ui.components.SelectorMes
import com.example.gastospersonales.util.FormatoFecha
import com.example.gastospersonales.util.FormatoMoneda
import com.example.gastospersonales.data.PreferenciasRepository
import com.example.gastospersonales.viewmodel.CategoriaViewModel
import com.example.gastospersonales.viewmodel.GastoViewModel

/**
 * Pantalla principal (ListaScreen). Observa los StateFlow del ViewModel con
 * collectAsState: cuando cambian los gastos, el total o el mes, la interfaz
 * se recompone sola.
 *
 * No hace queries ni conoce Room; solo lee estado y dispara callbacks de
 * navegación (onAgregar, onEditar, onConfiguracion).
 *
 * Sprint 4: onEditar ahora lo interpreta NavegacionApp como "abrir el
 * detalle" (el nombre del parámetro se mantiene para no tocar más de lo
 * necesario), se conectó el botón de engranaje con onConfiguracion, y el
 * total se formatea con la moneda elegida en Configuración. La moneda se
 * lee directo de PreferenciasRepository (Flow simple, sin ViewModel: ver
 * nota en ViewModelFactory.kt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    gastoViewModel: GastoViewModel,
    categoriaViewModel: CategoriaViewModel,
    preferenciasRepository: PreferenciasRepository,
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit,
    onConfiguracion: () -> Unit
) {
    val gastos by gastoViewModel.gastosDelMes.collectAsState()
    val total by gastoViewModel.totalDelMes.collectAsState()
    val mesActual by gastoViewModel.mesActual.collectAsState()
    val categorias by categoriaViewModel.categorias.collectAsState()
    val moneda by preferenciasRepository.moneda.collectAsState(initial = "USD")

    // Mapa id -> Categoria para resolver rápido el ícono/nombre de cada gasto.
    val categoriasPorId: Map<Long, Categoria> = categorias.associateBy { it.id }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregar,
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar gasto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ----- Encabezado: selector de mes, total y configuración -----
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                SelectorMes(
                    mesActual = mesActual,
                    onAnterior = gastoViewModel::mesAnterior,
                    onSiguiente = gastoViewModel::mesSiguiente,
                    onSeleccionar = gastoViewModel::irAMes
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total del mes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatoMoneda.formatear(total, moneda),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onConfiguracion) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configuración",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ----- Cuerpo: lista o estado vacío -----
            if (gastos.isEmpty()) {
                EstadoVacio(onAgregar = onAgregar)
            } else {
                ListaDeGastos(
                    gastos = gastos,
                    categoriasPorId = categoriasPorId,
                    onEditar = onEditar
                )
            }
        }
    }
}

/**
 * LazyColumn de gastos agrupados por día. Inserta un encabezado de día
 * cada vez que cambia la fecha respecto a la fila anterior.
 */
@Composable
private fun ListaDeGastos(
    gastos: List<com.example.gastospersonales.model.Gasto>,
    categoriasPorId: Map<Long, Categoria>,
    onEditar: (Long) -> Unit
) {
    // Precalcular en qué posiciones cambia el día, para saber dónde va un
    // encabezado. Se hace fuera del LazyColumn porque los bloques de items se
    // evalúan de forma perezosa e independiente: una variable mutable dentro
    // no conservaría su valor de forma fiable entre filas.
    val indicesConEncabezado: Set<Int> = remember(gastos) {
        buildSet {
            var diaAnterior = -1
            gastos.forEachIndexed { indice, gasto ->
                val dia = FormatoFecha.diaDelMes(gasto.fecha)
                if (dia != diaAnterior) {
                    add(indice)
                    diaAnterior = dia
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        itemsIndexed(gastos, key = { _, gasto -> gasto.id }) { indice, gasto ->
            if (indice in indicesConEncabezado) {
                Text(
                    text = FormatoFecha.diaCorto(gasto.fecha),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 6.dp)
                )
            }
            GastoItem(
                gasto = gasto,
                categoria = categoriasPorId[gasto.categoriaId],
                onClick = { onEditar(gasto.id) }
            )
        }
    }
}

/**
 * Estado vacío: mensaje invitador con acción directa para el primer gasto.
 */
@Composable
private fun EstadoVacio(onAgregar: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Aún no tienes gastos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Registra tu primer gasto para empezar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
