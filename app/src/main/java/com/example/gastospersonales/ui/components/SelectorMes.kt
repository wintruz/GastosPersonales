package com.example.gastospersonales.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gastospersonales.util.FormatoFecha
import com.example.gastospersonales.viewmodel.MesAnio
import java.util.Calendar

/**
 * Selector de mes del encabezado: flechas ‹ Mes Año › para navegar mes a
 * mes, y un toque en el texto abre un diálogo para saltar directo a
 * cualquier mes/año.
 *
 * No conoce el ViewModel: recibe el mes actual y callbacks. Así es
 * testeable y reutilizable.
 */
@Composable
fun SelectorMes(
    mesActual: MesAnio,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onSeleccionar: (MesAnio) -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onAnterior) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Mes anterior",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { mostrarDialogo = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = FormatoFecha.mesAnio(mesActual.anio, mesActual.mes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "Elegir mes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onSiguiente) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Mes siguiente",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (mostrarDialogo) {
        DialogoMesAnio(
            mesActual = mesActual,
            onCerrar = { mostrarDialogo = false },
            onSeleccionar = {
                onSeleccionar(it)
                mostrarDialogo = false
            }
        )
    }
}

/**
 * Diálogo emergente: rejilla de 12 meses con navegación de año.
 */
@Composable
private fun DialogoMesAnio(
    mesActual: MesAnio,
    onCerrar: () -> Unit,
    onSeleccionar: (MesAnio) -> Unit
) {
    var anioMostrado by remember { mutableIntStateOf(mesActual.anio) }
    val meses = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )
    val anioActual = Calendar.getInstance().get(Calendar.YEAR)
    val mesActualDelSistema = Calendar.getInstance().get(Calendar.MONTH)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            // Cabecera de año con flechas.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { anioMostrado-- }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Año anterior")
                }
                Text(
                    text = anioMostrado.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { anioMostrado++ }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Año siguiente")
                }
            }
        },
        text = {
            // Rejilla 3x4 de meses.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meses.chunked(3).forEachIndexed { fila, grupo ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        grupo.forEachIndexed { col, etiqueta ->
                            val indiceMes = fila * 3 + col
                            val seleccionado =
                                indiceMes == mesActual.mes && anioMostrado == mesActual.anio
                            // Meses futuros sin datos: se atenúan (pero se permiten).
                            val esFuturo = anioMostrado > anioActual ||
                                    (anioMostrado == anioActual && indiceMes > mesActualDelSistema)

                            CeldaMes(
                                etiqueta = etiqueta,
                                seleccionado = seleccionado,
                                atenuado = esFuturo,
                                onClick = { onSeleccionar(MesAnio(anioMostrado, indiceMes)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSeleccionar(MesAnio(anioActual, mesActualDelSistema))
            }) { Text("Ir al mes actual") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        }
    )
}

@Composable
private fun CeldaMes(
    etiqueta: String,
    seleccionado: Boolean,
    atenuado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val texto = when {
        seleccionado -> MaterialTheme.colorScheme.onPrimary
        atenuado -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(fondo)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = etiqueta,
            color = texto,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (seleccionado) FontWeight.Medium else FontWeight.Normal
        )
    }
}