package com.example.gastospersonales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gastospersonales.data.TemaPreferido
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
 *
 * Sprint 4: preferenciasRepository se lee directo desde GestorGastosApp
 * (sin ViewModel propio, sin pasar por la Factory) para decidir el tema y
 * para pasárselo a la navegación.
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
            // Los ViewModel se crean con la Factory y quedan asociados a
            // esta Activity, sobreviviendo a las rotaciones.
            val gastoViewModel: GastoViewModel = viewModel(factory = factory)
            val categoriaViewModel: CategoriaViewModel = viewModel(factory = factory)

            // El tema preferido decide si se fuerza claro/oscuro o si se
            // sigue el del sistema, como hacía antes del Sprint 4.
            // collectAsState(initial = ...) alcanza aquí: es un Flow simple
            // de DataStore, no hace falta StateFlow ni ViewModel para leerlo.
            val temaPreferido by app.preferenciasRepository.tema.collectAsState(initial = TemaPreferido.SISTEMA)
            val oscuroDelSistema = isSystemInDarkTheme()
            val oscuro = when (temaPreferido) {
                TemaPreferido.CLARO -> false
                TemaPreferido.OSCURO -> true
                TemaPreferido.SISTEMA -> oscuroDelSistema
            }

            GastosPersonalesTheme(oscuro = oscuro) {
                NavegacionApp(
                    gastoViewModel = gastoViewModel,
                    categoriaViewModel = categoriaViewModel,
                    preferenciasRepository = app.preferenciasRepository
                )
            }
        }
    }
}
