package com.example.gastospersonales.ui.navigation

/**
 * Rutas de navegación centralizadas, para no repetir cadenas sueltas por
 * el código y evitar errores de tipeo.
 *
 * La ruta del formulario y la de detalle transportan el id del gasto como
 * argumento:
 *   "formulario/-1" = nuevo gasto
 *   "formulario/5"  = editar el gasto con id 5
 *   "detalle/5"     = ver el detalle del gasto con id 5
 */
object Rutas {
    const val LISTA = "lista"

    const val FORMULARIO = "formulario/{id}"
    fun formulario(id: Long): String = "formulario/$id"

    // Sprint 4: pantalla de detalle, con el botón de compartir.
    const val DETALLE = "detalle/{id}"
    fun detalle(id: Long): String = "detalle/$id"

    // Sprint 4: pantalla de configuración (sin argumentos).
    const val CONFIGURACION = "configuracion"

    const val ARG_ID = "id"
}
