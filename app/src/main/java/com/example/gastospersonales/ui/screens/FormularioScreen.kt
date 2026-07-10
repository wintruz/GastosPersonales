package com.example.gastospersonales.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.gastospersonales.data.FormatoFechaPreferido
import com.example.gastospersonales.data.PreferenciasRepository
import com.example.gastospersonales.model.Categoria
import com.example.gastospersonales.model.Gasto
import com.example.gastospersonales.ui.components.DialogoConfirmacion
import com.example.gastospersonales.util.FormatoFecha
import com.example.gastospersonales.util.GestorArchivos
import com.example.gastospersonales.viewmodel.CategoriaViewModel
import com.example.gastospersonales.viewmodel.GastoViewModel
import java.io.File
import com.example.gastospersonales.util.FormatoMoneda

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
    preferenciasRepository: PreferenciasRepository,
    onVolver: () -> Unit,
    // Se dispara al eliminar el gasto. Es DISTINTO de onVolver: onVolver
    // retrocede una sola pantalla (cancelar, guardar), mientras que al
    // eliminar hay que volver hasta la Lista sin importar desde cuántas
    // pantallas se llegó (Lista -> Detalle -> Formulario -> Eliminar).
    // Por defecto usa onVolver, para no romper llamadas que no lo pasen.
    onEliminado: () -> Unit = onVolver
) {
    val esEdicion = gastoId != -1L
    val categorias by categoriaViewModel.categorias.collectAsState()
    val formatoFecha by preferenciasRepository.formatoFecha
        .collectAsState(initial = FormatoFechaPreferido.DIA_MES_ANIO)
    val monedaPreferida by preferenciasRepository.moneda
    .collectAsState(initial = "USD")
    // gastoViewModel es la MISMA instancia que usa ListaScreen (se crea una
    // sola vez en MainActivity y se comparte). Leer su mesActual aquí da,
    // sin ningún parámetro de navegación extra, el mes que el usuario estaba
    // viendo cuando pulsó "+", que puede no coincidir con el mes real del
    // dispositivo (ej. viendo mayo en la lista aunque hoy sea julio).
    val mesListaActual by gastoViewModel.mesActual.collectAsState()
    val context = LocalContext.current

    // ----- Estado del formulario -----
    var montoTexto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Long?>(null) }
    // Sin fecha por defecto: null significa "el usuario aún no eligió".
    // Antes se precargaba con hoyEnMillis() (la fecha real del dispositivo),
    // lo que podía registrar el gasto en el mes equivocado si se estaba
    // viendo un mes distinto en la lista, sin que el usuario lo notara.
    var fechaMillis by remember { mutableStateOf<Long?>(null) }
    var rutaRecibo by remember { mutableStateOf<String?>(null) }
    var gastoActual by remember { mutableStateOf<Gasto?>(null) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    // Control de validación: un campo muestra su error cuando el usuario ya
    // interactuó con él (lo "tocó" y salió), o cuando se intentó guardar con
    // el formulario incompleto. Así el error no aparece de golpe al abrir la
    // pantalla, pero sí avisa cuando corresponde.
    var montoTocado by remember { mutableStateOf(false) }
    var descripcionTocada by remember { mutableStateOf(false) }
    var montoEnfocado by remember { mutableStateOf(false) }
    var descripcionEnfocada by remember { mutableStateOf(false) }
    var intentoGuardar by remember { mutableStateOf(false) }

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
    val descripcionValida = descripcion.isNotBlank()
    val categoriaValida = categoriaSeleccionada != null
    val fechaValida = fechaMillis != null
    val formularioValido = montoValido && descripcionValida && categoriaValida && fechaValida

    // ¿Debe mostrarse el error de cada campo? Cuando el campo fue tocado o se
    // intentó guardar, y su valor es inválido.
    val errorMonto = (montoTocado || intentoGuardar) && !montoValido
    val errorDescripcion = (descripcionTocada || intentoGuardar) && !descripcionValida
    val errorCategoria = intentoGuardar && !categoriaValida
    val errorFecha = intentoGuardar && !fechaValida

    // Mes en que se abre el selector si aún no hay fecha elegida: el que se
    // estaba viendo en la lista (mesListaActual), NO el mes real del
    // dispositivo. Si ya hay una fecha (editando, o ya elegida), se abre en
    // el mes de esa fecha, como es de esperar.
    val mesInicialSelector = fechaMillis
        ?: FormatoFecha.primerDiaDeMes(mesListaActual.anio, mesListaActual.mes)

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
            // Monto. La etiqueta va ENCIMA (no como label interno) para que
            // el placeholder "0.00" sea visible siempre, incluso sin foco.
            CampoConEtiqueta(texto = "Monto", enError = errorMonto) {
                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it.replace(',', '.') },
                    placeholder = { Text("0.00") },
                    prefix = { Text(FormatoMoneda.simbolo(monedaPreferida)) }, // antes = prefix = { Text("$") },
                    singleLine = true,
                    isError = errorMonto,
                    supportingText = {
                        if (errorMonto) {
                            Text("Ingresa un monto mayor a cero.")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { estado ->
                            if (estado.isFocused) montoEnfocado = true
                            else if (montoEnfocado) montoTocado = true
                        }
                )
            }

            // Descripción.
            CampoConEtiqueta(texto = "Descripción", enError = errorDescripcion) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    placeholder = { Text("Ej. Supermercado semanal") },
                    singleLine = true,
                    isError = errorDescripcion,
                    supportingText = {
                        if (errorDescripcion) {
                            Text("La descripción no puede estar vacía.")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { estado ->
                            if (estado.isFocused) descripcionEnfocada = true
                            else if (descripcionEnfocada) descripcionTocada = true
                        }
                )
            }

            // Categoría (chips en hasta 2 filas; el resto en un diálogo).
            CampoConEtiqueta(texto = "Categoría", enError = errorCategoria) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SelectorCategorias(
                        categorias = categorias,
                        seleccionadaId = categoriaSeleccionada,
                        onSeleccionar = { categoriaSeleccionada = it }
                    )
                    if (errorCategoria) {
                        Text(
                            text = "Elige una categoría.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Fecha. Sin valor por defecto: null significa que el usuario aún
            // no eligió una. Mientras tanto se muestra un placeholder gris
            // marcado "Ej." con el formato elegido en Configuración (mismo
            // patrón que Monto y Descripción), y no un valor que pueda
            // confundirse con una fecha ya seleccionada.
            CampoConEtiqueta(texto = "Fecha", enError = errorFecha) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (errorFecha) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { mostrarDatePicker = true }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        val fechaElegida = fechaMillis
                        if (fechaElegida != null) {
                            Text(
                                text = FormatoFecha.formatear(fechaElegida, formatoFecha),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Ej. " + FormatoFecha.formatear(FormatoFecha.hoyEnMillis(), formatoFecha),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (errorFecha) {
                        Text(
                            text = "Elige una fecha.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ----- Foto del recibo (Sprint 4) -----
            CampoConEtiqueta(texto = "Foto del recibo (opcional)") {
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
                                // Se borra de inmediato: es un recibo sin gasto
                                // que lo referencie mientras el formulario está
                                // abierto.
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
            }

            // Botón guardar / actualizar.
            //
            // Se deja siempre habilitado a propósito: si el formulario está
            // incompleto, al pulsarlo se activa intentoGuardar y se revelan
            // todos los errores (un botón deshabilitado no recibiría el clic
            // y el usuario no sabría por qué no puede guardar).
            Button(
                onClick = {
                    intentoGuardar = true
                    if (formularioValido) {
                        val gasto = Gasto(
                            id = if (esEdicion) gastoId else 0,
                            monto = montoTexto.toDouble(),
                            descripcion = descripcion.trim(),
                            // Seguro usar !!: formularioValido ya exigió
                            // fechaValida (fechaMillis != null) para entrar aquí.
                            fecha = fechaMillis!!,
                            rutaRecibo = rutaRecibo,
                            categoriaId = categoriaSeleccionada!!
                        )
                        if (esEdicion) gastoViewModel.actualizarGasto(gasto)
                        else gastoViewModel.agregarGasto(gasto)
                        onVolver()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (esEdicion) "Actualizar gasto" else "Guardar gasto")
            }
        }
    }

    // DatePicker de Material 3.
    if (mostrarDatePicker) {
        val estado = rememberDatePickerState(
            // null si aún no hay fecha elegida: no preselecciona ningún día.
            initialSelectedDateMillis = fechaMillis,
            // Independiente de si hay selección: en qué mes se abre el
            // calendario. Usa mesInicialSelector, que prioriza el mes que
            // se estaba viendo en la lista sobre el mes real del dispositivo.
            initialDisplayedMonthMillis = mesInicialSelector
        )
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
                onEliminado()
            },
            onCancelar = { mostrarDialogoEliminar = false }
        )
    }
}

/**
 * Agrupa una etiqueta y su campo en un bloque. La etiqueta va ENCIMA del
 * control (no como label interno), lo que permite que los placeholders sean
 * visibles siempre, incluso sin foco. El espaciado interno es pequeño (4dp)
 * para que la etiqueta quede pegada a su campo, mientras que la separación
 * entre bloques la da el Column padre.
 */
@Composable
private fun CampoConEtiqueta(
    texto: String,
    enError: Boolean = false,
    contenido: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        contenido()
    }
}

/**
 * Selector de categorías con chips que se ENVUELVEN en hasta 2 filas (no un
 * carrusel horizontal que esconde opciones). Si hay más categorías de las que
 * caben en 2 filas, un último chip "+N" abre un diálogo con todas.
 *
 * Se usa ContextualFlowRow (no FlowRow): compone los items de forma diferida a
 * partir de itemCount, lo que permite que expandIndicator lea totalItemCount y
 * shownItemCount de forma segura. Con FlowRow normal esto crashea, porque no
 * conoce el total de items por adelantado.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorCategorias(
    categorias: List<Categoria>,
    seleccionadaId: Long?,
    onSeleccionar: (Long) -> Unit
) {
    var mostrarTodas by remember { mutableStateOf(false) }

    // La categoría seleccionada se coloca primero, para que nunca quede
    // escondida tras el "+N" y el usuario siempre vea su elección.
    val ordenadas = remember(categorias, seleccionadaId) {
        categorias.sortedByDescending { it.id == seleccionadaId }
    }

    ContextualFlowRow(
        itemCount = ordenadas.size,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxLines = 2,
        overflow = ContextualFlowRowOverflow.expandIndicator {
            // Chip "+N" que abre el diálogo con todas las categorías.
            val restantes = totalItemCount - shownItemCount
            AssistChip(
                onClick = { mostrarTodas = true },
                label = { Text("+$restantes") }
            )
        }
    ) { index ->
        val categoria = ordenadas[index]
        FilterChip(
            selected = seleccionadaId == categoria.id,
            onClick = { onSeleccionar(categoria.id) },
            label = { Text(categoria.nombre) }
        )
    }

    // Diálogo con la lista completa de categorías.
    if (mostrarTodas) {
        AlertDialog(
            onDismissRequest = { mostrarTodas = false },
            title = { Text("Elegir categoría") },
            text = {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categorias.forEach { categoria ->
                        FilterChip(
                            selected = seleccionadaId == categoria.id,
                            onClick = {
                                onSeleccionar(categoria.id)
                                mostrarTodas = false
                            },
                            label = { Text(categoria.nombre) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarTodas = false }) { Text("Cerrar") }
            }
        )
    }
}