package com.example.gastospersonales.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Gasto: se convierte en la tabla "gastos".
 *
 * La clave foránea enlaza cada gasto con su categoría:
 *  - onDelete = RESTRICT impide borrar una categoría que aún tenga gastos.
 *    Es la red de seguridad: nunca quedará un gasto huérfano.
 *  - El índice sobre "categoriaId" acelera las consultas que filtran o
 *    agrupan por categoría, y Room lo recomienda para toda clave foránea.
 */
@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["categoriaId"])]
)
data class Gasto(
    // Clave primaria autogenerada.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Cantidad del gasto. La validación (> 0) se refuerza en la interfaz.
    val monto: Double,

    // Texto breve que describe el gasto.
    val descripcion: String,

    // Instante del gasto en milisegundos epoch. Se guarda como Long
    // porque SQLite no tiene tipo de fecha nativo; así ordenar y filtrar
    // por rango de mes es directo y eficiente.
    val fecha: Long,

    // Ruta del recibo en el almacenamiento interno. Nullable porque el
    // recibo es opcional. Guarda la ruta al archivo, no la imagen.
    val rutaRecibo: String? = null,

    // Clave foránea: referencia al id de la categoría. No nulo.
    val categoriaId: Long
)