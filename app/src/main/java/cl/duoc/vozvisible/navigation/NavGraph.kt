package cl.duoc.vozvisible.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.vozvisible.ui.screens.InicioScreen
import cl.duoc.vozvisible.ui.screens.LoginScreen
import cl.duoc.vozvisible.ui.screens.RecuperarScreen
import cl.duoc.vozvisible.ui.screens.RegistroScreen

@Composable
fun VozVisibleNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                alIngresar = { correo ->
                    navController.navigate(Rutas.inicioDe(correo)) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                alRegistrarse = { navController.navigate(Rutas.REGISTRO) },
                alRecuperar = { navController.navigate(Rutas.RECUPERAR) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(alVolver = { navController.popBackStack() })
        }

        composable(Rutas.RECUPERAR) {
            RecuperarScreen(alVolver = { navController.popBackStack() })
        }

        composable(
            route = Rutas.INICIO_CON_ARGUMENTO,
            arguments = listOf(navArgument(Rutas.ARG_CORREO) { type = NavType.StringType })
        ) { entrada ->
            InicioScreen(
                correo = entrada.arguments?.getString(Rutas.ARG_CORREO).orEmpty(),
                alCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
