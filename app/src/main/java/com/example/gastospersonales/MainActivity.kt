package com.example.gastospersonales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastospersonales.ui.navigation.NavegacionApp
import com.example.gastospersonales.ui.theme.GastosPersonalesTheme
import com.example.gastospersonales.viewmodel.CategoriaViewModel
import com.example.gastospersonales.viewmodel.GastoViewModel
import com.example.gastospersonales.viewmodel.ViewModelFactory

/**
 * Punto de entrada de la app. Arma la cadena de dependencias y lanza la
 * navegación dentro del tema.
 *
 * Los repositorios ya viven en GestorGastosApp (creados con by lazy); aquí
 * solo se obtienen y se pasan a la Factory, que construye los ViewModel.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenedor de dependencias de la app.
        val app = application as GestorGastosApp
        val factory = ViewModelFactory(
            gastoRepositorio = app.gastoRepositorio,
            categoriaRepositorio = app.categoriaRepositorio
        )

        setContent {
            GastosPersonalesTheme {
                // Los ViewModel se crean con la Factory y quedan asociados a
                // esta Activity, sobreviviendo a las rotaciones.
                val gastoViewModel: GastoViewModel = viewModel(factory = factory)
                val categoriaViewModel: CategoriaViewModel = viewModel(factory = factory)

                NavegacionApp(
                    gastoViewModel = gastoViewModel,
                    categoriaViewModel = categoriaViewModel
                )
            }
        }
    }
}