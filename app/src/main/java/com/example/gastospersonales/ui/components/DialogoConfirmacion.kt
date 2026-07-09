package com.example.gastospersonales.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Diálogo de confirmación reutilizable para acciones importantes o
 * irreversibles (como eliminar un gasto o una categoría).
 *
 * Recupera el diseño de los mockups del Sprint 1: un ícono destacado dentro
 * de un círculo de color arriba, título y mensaje centrados, y botones con
 * jerarquía clara (la acción destructiva como botón relleno en color de
 * error; cancelar como texto secundario).
 *
 * La firma se mantiene compatible con las llamadas existentes; el ícono y el
 * color del círculo tienen valores por defecto pensados para "eliminar".
 *
 * @param titulo         encabezado del diálogo.
 * @param mensaje        explicación de la consecuencia.
 * @param textoConfirmar etiqueta del botón de acción (ej. "Eliminar").
 * @param textoCancelar  etiqueta del botón secundario.
 * @param destructivo    si es true, la acción se pinta en color de error.
 * @param icono          ícono mostrado en el círculo superior.
 * @param onConfirmar    acción al confirmar.
 * @param onCancelar     acción al cancelar o descartar.
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String = "Eliminar",
    textoCancelar: String = "Cancelar",
    destructivo: Boolean = true,
    icono: ImageVector = Icons.Filled.DeleteOutline,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    // Colores según el tipo de acción.
    val colorContenido =
        if (destructivo) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary
    val colorFondoCirculo =
        if (destructivo) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer

    AlertDialog(
        onDismissRequest = onCancelar,
        // Ícono destacado dentro de un círculo, centrado arriba.
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colorFondoCirculo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorContenido,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = titulo,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = mensaje,
                textAlign = TextAlign.Center
            )
        },
        // Acción destructiva como botón relleno, para darle peso visual.
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorContenido,
                    contentColor = if (destructivo) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(textoCancelar)
            }
        }
    )
}