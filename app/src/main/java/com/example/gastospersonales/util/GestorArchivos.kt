package com.example.gastospersonales.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Maneja la persistencia de las fotos de recibo en el almacenamiento
 * interno privado de la app (filesDir), fuera del alcance de otras apps
 * y que se borra solo si la app se desinstala.
 *
 * No guarda la imagen en la base de datos: Gasto.rutaRecibo solo guarda
 * la ruta absoluta del archivo (ver comentario en la entidad, Sprint 1).
 */
object GestorArchivos {

    private const val CARPETA_RECIBOS = "recibos"

    /**
     * Copia la imagen que apunta la uri temporal (elegida en la galería)
     * hacia filesDir/recibos, con un nombre único para no colisionar con
     * otro recibo. Devuelve la ruta absoluta del archivo copiado, o null
     * si la copia falla (por ejemplo, si la uri ya no es accesible).
     *
     * Recibe Context porque necesita filesDir y el contentResolver; se
     * llama desde la pantalla (Compose), no desde el ViewModel, para no
     * mezclar Android Framework dentro de la capa de estado.
     */
    fun copiarAFilesDir(context: Context, uri: Uri): String? {
        return try {
            val carpeta = File(context.filesDir, CARPETA_RECIBOS).apply { mkdirs() }
            val destino = File(carpeta, "recibo_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return null
            destino.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Borra el archivo en esa ruta, si existe. No necesita Context: trabaja
     * directo sobre la ruta absoluta guardada en Gasto.rutaRecibo. Se usa
     * en dos momentos: al eliminar un gasto (para no dejar la foto huérfana)
     * y al reemplazar la foto de un gasto existente por una nueva.
     */
    fun eliminarSiExiste(ruta: String?) {
        if (ruta.isNullOrBlank()) return
        val archivo = File(ruta)
        if (archivo.exists()) archivo.delete()
    }
}
