package com.example.gastospersonales.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gastospersonales.data.PreferenciasRepository
import com.example.gastospersonales.ui.screens.ConfiguracionScreen
import com.example.gastospersonales.ui.screens.DetalleScreen
import com.example.gastospersonales.ui.screens.FormularioScreen
import com.example.gastospersonales.ui.screens.GestionCategoriasScreen
import com.example.gastospersonales.ui.screens.ListaScreen
import com.example.gastospersonales.viewmodel.CategoriaViewModel
import com.example.gastospersonales.viewmodel.GastoViewModel

/**
 * Grafo de navegación de la app.
 *
 * Sprint 4 agrega dos pantallas: Detalle (con el botón de compartir) y
 * Configuración. Al tocar un gasto en la lista ahora se abre el Detalle en
 * vez de ir directo al formulario; desde el Detalle se puede editar, lo
 * que sí navega al formulario. Los ViewModel se crean una vez en
 * MainActivity y se comparten entre pantallas; preferenciasRepository viaja
 * igual, pero sin ViewModel de por medio (ver nota en ViewModelFactory.kt).
 */
@Composable
fun NavegacionApp(
    gastoViewModel: GastoViewModel,
    categoriaViewModel: CategoriaViewModel,
    preferenciasRepository: PreferenciasRepository
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.LISTA) {

        // Pantalla de lista.
        composable(Rutas.LISTA) {
            ListaScreen(
                gastoViewModel = gastoViewModel,
                categoriaViewModel = categoriaViewModel,
                preferenciasRepository = preferenciasRepository,
                onAgregar = { navController.navigate(Rutas.formulario(-1L)) },
                // Antes iba directo al formulario; desde el Sprint 4 abre el detalle.
                onEditar = { id -> navController.navigate(Rutas.detalle(id)) },
                onConfiguracion = { navController.navigate(Rutas.CONFIGURACION) }
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
                preferenciasRepository = preferenciasRepository,
                onVolver = { navController.popBackStack() },
                // Al eliminar, se vuelve hasta la Lista de una sola vez, sin
                // importar si se llegó por Lista -> Formulario (una pantalla
                // de por medio) o por Lista -> Detalle -> Formulario (dos).
                // Un popBackStack() simple solo retrocedería una pantalla y
                // dejaría el Detalle mostrando un gasto que ya no existe.
                onEliminado = {
                    navController.popBackStack(route = Rutas.LISTA, inclusive = false)
                }
            )
        }

        // Sprint 4: pantalla de detalle, con el botón de compartir.
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Rutas.ARG_ID) ?: return@composable
            DetalleScreen(
                gastoId = id,
                gastoViewModel = gastoViewModel,
                categoriaViewModel = categoriaViewModel,
                preferenciasRepository = preferenciasRepository,
                onEditar = { navController.navigate(Rutas.formulario(id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        // Sprint 4: pantalla de configuración (tema, moneda y formato de fecha).
        composable(Rutas.CONFIGURACION) {
            ConfiguracionScreen(
                preferenciasRepository = preferenciasRepository,
                onVolver = { navController.popBackStack() },
                onGestionarCategorias = { navController.navigate(Rutas.GESTION_CATEGORIAS) }
            )
        }

        // Sprint 4: gestión de categorías, colgada de Configuración.
        composable(Rutas.GESTION_CATEGORIAS) {
            GestionCategoriasScreen(
                categoriaViewModel = categoriaViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}