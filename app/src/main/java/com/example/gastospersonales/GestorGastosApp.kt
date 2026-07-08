package com.example.gastospersonales

import android.app.Application
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.CategoriaRepositorio
import com.example.gastospersonales.data.GastoRepositorio
import com.example.gastospersonales.data.PreferenciasRepository

class GestorGastosApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.obtenerInstancia(this)
    }

    val categoriaRepositorio: CategoriaRepositorio by lazy {
        CategoriaRepositorio(database.categoriaDao())
    }

    val gastoRepositorio: GastoRepositorio by lazy {
        GastoRepositorio(database.gastoDao())
    }

    // Sprint 4: preferencias de usuario (tema, moneda), respaldadas por
    // DataStore en vez de Room.
    val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(this)
    }
}
