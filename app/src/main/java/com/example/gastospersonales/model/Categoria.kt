package com.example.gastospersonales.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Categoria: se convierte en la tabla "categorias".
 * Cada gasto pertenece a una categoría (relación uno-a-muchos).
 *
 * El índice único sobre "nombre" impide crear dos categorías con
 * el mismo nombre a nivel de base de datos.
 */
@Entity(
    tableName = "categorias",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Categoria(
    // Clave primaria autogenerada: Room asigna 1, 2, 3… al insertar.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Nombre visible de la categoría (Comida, Transporte…). Único, no nulo.
    val nombre: String,

    // Nombre del ícono asociado que se muestra en la interfaz.
    val icono: String,

    // Marca la categoría "Otro" como del sistema: no se puede eliminar,
    // porque es el destino de respaldo al borrar otras categorías.
    val esSistema: Boolean = false
)