package com.example.gastospersonales.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.gastospersonales.data.FormatoFechaPreferido
import com.example.gastospersonales.data.PreferenciasRepository
import com.example.gastospersonales.data.TemaPreferido
import com.example.gastospersonales.util.AppInfo
import com.example.gastospersonales.util.FormatoFecha
import kotlinx.coroutines.launch

/**
 * Pantalla de configuración (Sprint 4). Agrupa las preferencias en
 * secciones con tarjeta (Apariencia, Regional, Datos, Acerca de), siguiendo
 * el aspecto de los mockups del Sprint 1. Todo se guarda de inmediato en
 * DataStore al tocar una opción, sin botón de guardar.
 *
 * No usa un ViewModel propio para las preferencias: PreferenciasRepository
 * expone Flow simples de DataStore, y se leen con collectAsState(initial=...)
 * directamente (ver nota en ViewModelFactory.kt). La gestión de categorías
 * sí tiene su propia pantalla con su propio ViewModel (GestionCategoriasScreen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    preferenciasRepository: PreferenciasRepository,
    onVolver: () -> Unit,
    onGestionarCategorias: () -> Unit
) {
    val tema by preferenciasRepository.tema.collectAsState(initial = TemaPreferido.SISTEMA)
    val moneda by preferenciasRepository.moneda.collectAsState(initial = "USD")
    val formatoFecha by preferenciasRepository.formatoFecha
        .collectAsState(initial = FormatoFechaPreferido.DIA_MES_ANIO)
    val scope = rememberCoroutineScope()

    val monedasDisponibles = listOf("USD", "PAB", "EUR")
    val hoy = FormatoFecha.hoyEnMillis()
    val opcionesFormatoFecha = listOf(
        FormatoFechaPreferido.DIA_MES_ANIO to FormatoFecha.formatear(hoy, FormatoFechaPreferido.DIA_MES_ANIO),
        FormatoFechaPreferido.MES_DIA_ANIO to FormatoFecha.formatear(hoy, FormatoFechaPreferido.MES_DIA_ANIO),
        FormatoFechaPreferido.LARGO to FormatoFecha.formatear(hoy, FormatoFechaPreferido.LARGO)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // ----- Apariencia -----
            TarjetaSeccion(titulo = "Apariencia") {
                FilaChips(icono = Icons.Filled.Palette, etiqueta = "Tema") {
                    FilterChip(
                        selected = tema == TemaPreferido.SISTEMA,
                        onClick = { scope.launch { preferenciasRepository.guardarTema(TemaPreferido.SISTEMA) } },
                        label = { Text("Sistema") }
                    )
                    FilterChip(
                        selected = tema == TemaPreferido.CLARO,
                        onClick = { scope.launch { preferenciasRepository.guardarTema(TemaPreferido.CLARO) } },
                        label = { Text("Claro") }
                    )
                    FilterChip(
                        selected = tema == TemaPreferido.OSCURO,
                        onClick = { scope.launch { preferenciasRepository.guardarTema(TemaPreferido.OSCURO) } },
                        label = { Text("Oscuro") }
                    )
                }
            }

            // ----- Regional -----
            TarjetaSeccion(titulo = "Regional") {
                FilaChips(icono = Icons.Filled.Payments, etiqueta = "Moneda") {
                    monedasDisponibles.forEach { codigo ->
                        FilterChip(
                            selected = moneda == codigo,
                            onClick = { scope.launch { preferenciasRepository.guardarMoneda(codigo) } },
                            label = { Text(codigo) }
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FilaChips(icono = Icons.Filled.CalendarMonth, etiqueta = "Formato de fecha") {
                    opcionesFormatoFecha.forEach { (opcion, ejemplo) ->
                        FilterChip(
                            selected = formatoFecha == opcion,
                            onClick = { scope.launch { preferenciasRepository.guardarFormatoFecha(opcion) } },
                            label = { Text(ejemplo) }
                        )
                    }
                }
            }

            // ----- Datos -----
            TarjetaSeccion(titulo = "Datos") {
                FilaNavegable(
                    icono = Icons.Filled.Category,
                    etiqueta = "Categorías",
                    onClick = onGestionarCategorias
                )
            }

            // ----- Acerca de -----
            TarjetaSeccion(titulo = "Acerca de") {
                FilaValor(
                    icono = Icons.Filled.Info,
                    etiqueta = "Versión",
                    valor = AppInfo.VERSION
                )
            }
        }
    }
}

/**
 * Agrupa un título de sección y una tarjeta bordeada con sus filas dentro,
 * siguiendo el aspecto de los mockups de Configuración del Sprint 1.
 */
@Composable
private fun TarjetaSeccion(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            contenido()
        }
    }
}

/** Fila con ícono, etiqueta y un grupo de chips debajo (envueltos, sin scroll oculto). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilaChips(
    icono: ImageVector,
    etiqueta: String,
    chips: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(etiqueta, color = MaterialTheme.colorScheme.onSurface)
        }
        // FlowRow sin límite de líneas: nunca esconde opciones detrás de un
        // scroll horizontal, se envuelve a una segunda fila si hace falta.
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    }
}

/** Fila navegable (con flecha), para abrir otra pantalla, como Categorías. */
@Composable
private fun FilaNavegable(
    icono: ImageVector,
    etiqueta: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(etiqueta, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Fila informativa de solo lectura, como la versión de la app. */
@Composable
private fun FilaValor(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(etiqueta, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Text(valor, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}