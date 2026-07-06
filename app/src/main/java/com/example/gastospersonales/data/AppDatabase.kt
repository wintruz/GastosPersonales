package com.example.gastospersonales.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gastospersonales.model.Categoria
import com.example.gastospersonales.model.Gasto

/**
 * Punto de entrada de la base de datos. Une las dos entidades (tablas) con
 * sus DAO y crea la conexión con el archivo SQLite físico.
 *
 * version = 1 porque es el primer esquema. Se incrementa al cambiar la
 * estructura de las tablas (y entonces se añade una migración).
 */
@Database(
    entities = [Categoria::class, Gasto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun gastoDao(): GastoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Patrón singleton: una sola instancia en toda la app.
        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestor_gastos_database"
                )
                    // Precarga las categorías por defecto la primera vez que
                    // se crea la base de datos.
                    .addCallback(PRECARGA_CALLBACK)
                    .build()
                INSTANCE = instancia
                instancia
            }
        }

        /**
         * Callback que inserta las categorías iniciales al crear la base de
         * datos por primera vez. La categoría "Otro" se marca como de sistema
         * (esSistema = true) para que no pueda eliminarse: es el destino de
         * respaldo cuando se borra otra categoría con gastos.
         *
         * Se usa execSQL porque en onCreate la base de datos aún no expone los
         * DAO; es una inserción directa de datos semilla.
         */
        private val PRECARGA_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val categorias = listOf(
                    // nombre, icono, esSistema
                    Triple("Comida", "shopping-cart", 0),
                    Triple("Transporte", "gas-station", 0),
                    Triple("Ocio", "device-tv", 0),
                    Triple("Salud", "basket", 0),
                    Triple("Servicios", "bolt", 0),
                    Triple("Otro", "dots", 1) // categoría de sistema
                )
                for ((nombre, icono, esSistema) in categorias) {
                    db.execSQL(
                        "INSERT INTO categorias (nombre, icono, esSistema) VALUES (?, ?, ?)",
                        arrayOf(nombre, icono, esSistema)
                    )
                }
            }
        }
    }
}