package com.example.gastospersonales.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.gastospersonales.model.Categoria
import com.example.gastospersonales.ui.components.DialogoConfirmacion
import com.example.gastospersonales.util.IconosCategoria
import com.example.gastospersonales.viewmodel.CategoriaViewModel

/**
 * Gestión de categorías (Sprint 4): listar, agregar, editar y eliminar.
 * Colgada de Configuración > Datos > Categorías.
 *
 * La categoría de sistema ("Otro", esSistema = true) no se puede editar ni
 * eliminar: es el destino de respaldo definido en el esquema del Sprint 1,
 * y debe existir siempre.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCategoriasScreen(
    categoriaViewModel: CategoriaViewModel,
    onVolver: () -> Unit
) {
    val categorias by categoriaViewModel.categorias.collectAsState()

    var categoriaEnEdicion by remember { mutableStateOf<Categoria?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var categoriaAEliminar by remember { mutableStateOf<Categoria?>(null) }
    var conteoAEliminar by remember { mutableStateOf<Int?>(null) }

    // Al pedir eliminar, se cuenta ANTES cuántos gastos usan la categoría,
    // para avisar con el número exacto ("se reasignarán 3 gastos") en vez
    // de un mensaje genérico. contarGastosAsociados es suspend, por eso va
    // en un LaunchedEffect atado al id de la categoría seleccionada.
    LaunchedEffect(categoriaAEliminar) {
        val categoria = categoriaAEliminar
        conteoAEliminar = if (categoria != null) {
            categoriaViewModel.contarGastosAsociados(categoria.id)
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        categoriaEnEdicion = null
                        mostrarFormulario = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar categoría")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categorias, key = { it.id }) { categoria ->
                FilaCategoria(
                    categoria = categoria,
                    onEditar = {
                        categoriaEnEdicion = categoria
                        mostrarFormulario = true
                    },
                    onEliminar = { categoriaAEliminar = categoria }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }

    // Formulario de agregar/editar.
    if (mostrarFormulario) {
        DialogoFormularioCategoria(
            categoria = categoriaEnEdicion,
            categoriasExistentes = categorias,
            onGuardar = { nombre, icono ->
                val base = categoriaEnEdicion
                if (base != null) {
                    categoriaViewModel.actualizarCategoria(base.copy(nombre = nombre, icono = icono))
                } else {
                    categoriaViewModel.agregarCategoria(Categoria(nombre = nombre, icono = icono))
                }
                mostrarFormulario = false
            },
            onCancelar = { mostrarFormulario = false }
        )
    }

    // Confirmación de borrado, con el conteo real de gastos a reasignar.
    val categoria = categoriaAEliminar
    val conteo = conteoAEliminar
    if (categoria != null && conteo != null) {
        DialogoConfirmacion(
            titulo = "Eliminar \"${categoria.nombre}\"",
            mensaje = if (conteo > 0) {
                val plural = if (conteo == 1) "gasto" else "gastos"
                "Se reasignarán $conteo $plural a la categoría \"Otro\". Esta acción no se puede deshacer."
            } else {
                "Esta categoría no tiene gastos asociados. Esta acción no se puede deshacer."
            },
            onConfirmar = {
                categoriaViewModel.eliminarCategoria(categoria)
                categoriaAEliminar = null
            },
            onCancelar = { categoriaAEliminar = null }
        )
    }
}

/**
 * Fila de una categoría en la lista. Las categorías de sistema (esSistema)
 * no muestran acciones de editar/eliminar, y en su lugar aclaran su estado.
 */
@Composable
private fun FilaCategoria(
    categoria: Categoria,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconosCategoria.desde(categoria.icono),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoria.nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (categoria.esSistema) {
                Text(
                    text = "Categoría del sistema",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!categoria.esSistema) {
            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar ${categoria.nombre}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Eliminar ${categoria.nombre}",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Íconos disponibles para elegir al crear o editar una categoría. Las claves
// coinciden con las que reconoce IconosCategoria.desde().
private val ICONOS_DISPONIBLES = listOf(
    "shopping-cart", "gas-station", "device-tv", "basket",
    "bolt", "bus", "restaurant", "dots"
)

/**
 * Diálogo de agregar/editar categoría. categoria == null significa "nueva";
 * si no es null, precarga sus datos para edición.
 *
 * Valida que el nombre no esté vacío y que no choque con otra categoría ya
 * existente (comparación sin distinguir mayúsculas ni espacios), para no
 * depender del índice único de la base de datos: insertar duplicado con
 * OnConflictStrategy.IGNORE fallaría en silencio, y actualizar duplicado
 * lanzaría una excepción de Room. Validar aquí evita ambos casos.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialogoFormularioCategoria(
    categoria: Categoria?,
    categoriasExistentes: List<Categoria>,
    onGuardar: (nombre: String, icono: String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(categoria?.nombre ?: "") }
    var iconoSeleccionado by remember { mutableStateOf(categoria?.icono ?: ICONOS_DISPONIBLES.first()) }
    var intentoGuardar by remember { mutableStateOf(false) }

    val nombreNormalizado = nombre.trim()
    val duplicado = categoriasExistentes.any {
        it.nombre.trim().equals(nombreNormalizado, ignoreCase = true) && it.id != categoria?.id
    }
    val nombreValido = nombreNormalizado.isNotBlank() && !duplicado
    val errorVacio = intentoGuardar && nombreNormalizado.isBlank()
    val errorDuplicado = intentoGuardar && !errorVacio && duplicado

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (categoria == null) "Nueva categoría" else "Editar categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej. Mascotas") },
                    singleLine = true,
                    isError = errorVacio || errorDuplicado,
                    supportingText = {
                        when {
                            errorVacio -> Text("El nombre no puede estar vacío.")
                            errorDuplicado -> Text("Ya existe una categoría con este nombre.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Ícono",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ICONOS_DISPONIBLES.forEach { clave ->
                        IconoSeleccionable(
                            icono = IconosCategoria.desde(clave),
                            seleccionado = iconoSeleccionado == clave,
                            onClick = { iconoSeleccionado = clave }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                intentoGuardar = true
                if (nombreValido) onGuardar(nombreNormalizado, iconoSeleccionado)
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

/** Círculo de ícono seleccionable, usado en el selector de ícono del formulario. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconoSeleccionable(
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (seleccionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (seleccionado) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}