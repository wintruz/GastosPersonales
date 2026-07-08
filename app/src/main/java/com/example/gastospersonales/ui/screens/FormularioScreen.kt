package com.example.gastospersonales.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.gastospersonales.model.Gasto
import com.example.gastospersonales.ui.components.DialogoConfirmacion
import com.example.gastospersonales.util.FormatoFecha
import com.example.gastospersonales.util.GestorArchivos
import com.example.gastospersonales.viewmodel.CategoriaViewModel
import com.example.gastospersonales.viewmodel.GastoViewModel
import java.io.File

/**
 * Formulario para crear o editar un gasto.
 *
 * El modo lo determina gastoId: -1 = nuevo, cualquier otro = editar ese id.
 * En modo edición, LaunchedEffect carga los datos una sola vez al aparecer.
 *
 * Sprint 4: se agrega la foto del recibo. rutaRecibo vive en el estado del
 * formulario (no solo en gastoActual) porque cambia antes de guardar: al
 * elegir una foto nueva se copia a filesDir de inmediato y, si ya había una
 * foto previa, se borra para no dejar un archivo huérfano.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    gastoId: Long,
    gastoViewModel: GastoViewModel,
    categoriaViewModel: CategoriaViewModel,
    onVolver: () -> Unit
) {
    val esEdicion = gastoId != -1L
    val categorias by categoriaViewModel.categorias.collectAsState()
    val context = LocalContext.current

    // ----- Estado del formulario -----
    var montoTexto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Long?>(null) }
    var fechaMillis by remember { mutableLongStateOf(FormatoFecha.hoyEnMillis()) }
    var rutaRecibo by remember { mutableStateOf<String?>(null) }
    var gastoActual by remember { mutableStateOf<Gasto?>(null) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    // Cargar datos existentes una sola vez si estamos editando.
    LaunchedEffect(gastoId) {
        if (esEdicion) {
            gastoViewModel.obtenerPorId(gastoId)?.let { g ->
                gastoActual = g
                montoTexto = g.monto.toString()
                descripcion = g.descripcion
                categoriaSeleccionada = g.categoriaId
                fechaMillis = g.fecha
                rutaRecibo = g.rutaRecibo
            }
        }
    }

    // ----- Selector de foto -----
    //
    // El picker de imágenes en sí (GetContent) no exige permiso en todos
    // los dispositivos, pero pedirlo explícitamente cubre el caso general
    // que exige el curso: READ_MEDIA_IMAGES en Android 13+, o
    // READ_EXTERNAL_STORAGE en versiones anteriores (ver AndroidManifest).
    val permisoGaleria = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val rutaNueva = GestorArchivos.copiarAFilesDir(context, it)
            if (rutaNueva != null) {
                // Si ya había una foto, se borra: la nueva la reemplaza.
                GestorArchivos.eliminarSiExiste(rutaRecibo)
                rutaRecibo = rutaNueva
            }
        }
    }

    val solicitarPermisoGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido -> if (concedido) selectorImagen.launch("image/*") }

    val abrirGaleria = {
        val yaConcedido = ContextCompat.checkSelfPermission(context, permisoGaleria) ==
            PackageManager.PERMISSION_GRANTED
        if (yaConcedido) selectorImagen.launch("image/*") else solicitarPermisoGaleria.launch(permisoGaleria)
    }

    // Validaciones.
    val montoValido = montoTexto.toDoubleOrNull()?.let { it > 0 } == true
    val formularioValido = montoValido && descripcion.isNotBlank() && categoriaSeleccionada != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar gasto" else "Nuevo gasto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (esEdicion) {
                        IconButton(onClick = { mostrarDialogoEliminar = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monto.
            OutlinedTextField(
                value = montoTexto,
                onValueChange = { montoTexto = it.replace(',', '.') },
                label = { Text("Monto") },
                prefix = { Text("$") },
                singleLine = true,
                isError = montoTexto.isNotEmpty() && !montoValido,
                supportingText = {
                    if (montoTexto.isNotEmpty() && !montoValido) {
                        Text("Ingresa un monto mayor a cero.")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Descripción.
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Categoría (chips de selección simple).
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { categoria ->
                    FilterChip(
                        selected = categoriaSeleccionada == categoria.id,
                        onClick = { categoriaSeleccionada = categoria.id },
                        label = { Text(categoria.nombre) }
                    )
                }
            }

            // Fecha.
            Text(
                text = "Fecha",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { mostrarDatePicker = true }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Text(
                    text = FormatoFecha.fechaLarga(fechaMillis),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ----- Foto del recibo (Sprint 4) -----
            Text(
                text = "Foto del recibo (opcional)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (rutaRecibo != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = File(rutaRecibo!!),
                        contentDescription = "Foto del recibo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { abrirGaleria() }
                    )
                    IconButton(
                        onClick = {
                            // Se borra de inmediato: es un recibo sin gasto que
                            // lo referencie mientras el formulario está abierto.
                            GestorArchivos.eliminarSiExiste(rutaRecibo)
                            rutaRecibo = null
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Quitar foto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { abrirGaleria() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Agregar foto desde la galería",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botón guardar / actualizar.
            Button(
                onClick = {
                    val gasto = Gasto(
                        id = if (esEdicion) gastoId else 0,
                        monto = montoTexto.toDouble(),
                        descripcion = descripcion.trim(),
                        fecha = fechaMillis,
                        rutaRecibo = rutaRecibo,
                        categoriaId = categoriaSeleccionada!!
                    )
                    if (esEdicion) gastoViewModel.actualizarGasto(gasto)
                    else gastoViewModel.agregarGasto(gasto)
                    onVolver()
                },
                enabled = formularioValido,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (esEdicion) "Actualizar gasto" else "Guardar gasto")
            }
        }
    }

    // DatePicker de Material 3.
    if (mostrarDatePicker) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = fechaMillis)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { fechaMillis = it }
                    mostrarDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estado)
        }
    }

    // Confirmación de borrado (solo edición).
    if (mostrarDialogoEliminar) {
        DialogoConfirmacion(
            titulo = "Eliminar gasto",
            mensaje = "Se borrará \"${descripcion}\". Esta acción no se puede deshacer.",
            onConfirmar = {
                gastoActual?.let { gastoViewModel.eliminarGasto(it) }
                mostrarDialogoEliminar = false
                onVolver()
            },
            onCancelar = { mostrarDialogoEliminar = false }
        )
    }
}
