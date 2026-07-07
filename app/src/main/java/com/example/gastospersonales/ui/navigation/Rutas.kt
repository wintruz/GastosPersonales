package com.example.gastospersonales.ui.navigation

/**
 * Rutas de navegación centralizadas, para no repetir cadenas sueltas por
 * el código y evitar errores de tipeo.
 *
 * La ruta del formulario transporta el id del gasto como argumento:
 *   "formulario/-1" = nuevo gasto
 *   "formulario/5"  = editar el gasto con id 5
 */
object Rutas {
    const val LISTA = "lista"
    const val FORMULARIO = "formulario/{id}"

    // Constructor de la ruta del formulario con un id concreto.
    fun formulario(id: Long): String = "formulario/$id"

    const val ARG_ID = "id"
}