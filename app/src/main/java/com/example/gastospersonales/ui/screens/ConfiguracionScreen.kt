package com.example.gastospersonales.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gastospersonales.data.PreferenciasRepository
import com.example.gastospersonales.data.TemaPreferido
import kotlinx.coroutines.launch

/**
 * Pantalla de configuración (Sprint 4). Deja elegir el tema (sistema, claro
 * u oscuro) y la moneda; ambos se guardan en DataStore de inmediato, sin
 * botón de guardar.
 *
 * No usa un ViewModel propio: PreferenciasRepository no lo tenía reservado
 * en el árbol de paquetes, y para un par de Flow simples de DataStore no
 * hacía falta uno. tema y moneda se leen con collectAsState(initial = ...)
 * -Flow "frío" normal, no StateFlow- y las escrituras (guardarTema,
 * guardarMoneda, ambas suspend) se lanzan con rememberCoroutineScope(),
 * atado al ciclo de vida de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    preferenciasRepository: PreferenciasRepository,
    onVolver: () -> Unit
) {
    val tema by preferenciasRepository.tema.collectAsState(initial = TemaPreferido.SISTEMA)
    val moneda by preferenciasRepository.moneda.collectAsState(initial = "USD")
    val scope = rememberCoroutineScope()

    val monedasDisponibles = listOf("USD", "PAB", "EUR")

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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tema",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Moneda",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    monedasDisponibles.forEach { codigo ->
                        FilterChip(
                            selected = moneda == codigo,
                            onClick = { scope.launch { preferenciasRepository.guardarMoneda(codigo) } },
                            label = { Text(codigo) }
                        )
                    }
                }
            }
        }
    }
}
