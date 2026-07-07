package com.example.gastospersonales.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gastospersonales.ui.screens.FormularioScreen
import com.example.gastospersonales.ui.screens.ListaScreen
import com.example.gastospersonales.viewmodel.CategoriaViewModel
import com.example.gastospersonales.viewmodel.GastoViewModel

/**
 * Grafo de navegación de la app. Define el "mapa" de pantallas y cómo se
 * viaja entre ellas. La lista es la pantalla inicial; desde ella se navega
 * al formulario, pasándole el id del gasto (o -1 para uno nuevo).
 *
 * Ambos ViewModel se crean una vez arriba (en MainActivity) y se comparten
 * con las pantallas, para que lista y formulario vean el mismo estado.
 */
@Composable
fun NavegacionApp(
    gastoViewModel: GastoViewModel,
    categoriaViewModel: CategoriaViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.LISTA) {

        // Pantalla de lista.
        composable(Rutas.LISTA) {
            ListaScreen(
                gastoViewModel = gastoViewModel,
                categoriaViewModel = categoriaViewModel,
                onAgregar = { navController.navigate(Rutas.formulario(-1L)) },
                onEditar = { id -> navController.navigate(Rutas.formulario(id)) }
            )
        }

        // Pantalla de formulario, con el id como argumento de tipo Long.
        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Rutas.ARG_ID) ?: -1L
            FormularioScreen(
                gastoId = id,
                gastoViewModel = gastoViewModel,
                categoriaViewModel = categoriaViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}