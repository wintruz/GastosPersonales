package com.example.gastospersonales.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Diálogo de confirmación reutilizable, para acciones importantes o
 * irreversibles (como eliminar un gasto). Sigue el patrón AlertDialog de
 * Material 3 usado en el curso.
 *
 * @param titulo       encabezado del diálogo.
 * @param mensaje      explicación de la consecuencia.
 * @param textoConfirmar etiqueta del botón de acción (ej. "Eliminar").
 * @param destructivo  si es true, el botón de confirmar se pinta en color de error.
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String = "Eliminar",
    textoCancelar: String = "Cancelar",
    destructivo: Boolean = true,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    text = textoConfirmar,
                    color = if (destructivo) {
                        androidx.compose.material3.MaterialTheme.colorScheme.error
                    } else {
                        Color.Unspecified
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(textoCancelar)
            }
        }
    )
}