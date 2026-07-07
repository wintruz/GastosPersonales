package com.example.gastospersonales

import android.app.Application
import  com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.CategoriaRepositorio
import com.example.gastospersonales.data.GastoRepositorio


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
}