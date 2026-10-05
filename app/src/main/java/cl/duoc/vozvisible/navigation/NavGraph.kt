package cl.duoc.vozvisible.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.vozvisible.Contenedor
import cl.duoc.vozvisible.ui.screens.FrasesScreen
import cl.duoc.vozvisible.ui.screens.InicioScreen
import cl.duoc.vozvisible.ui.screens.LoginScreen
import cl.duoc.vozvisible.ui.screens.PerfilScreen
import cl.duoc.vozvisible.ui.screens.RecuperarScreen
import cl.duoc.vozvisible.ui.screens.RegistroScreen
import cl.duoc.vozvisible.ui.viewmodel.FrasesViewModel
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel

@Composable
fun VozVisibleNavGraph(
    contenedor: Contenedor,
    navController: NavHostController = rememberNavController()
) {
    // un solo SesionViewModel para toda la app (queda atado a la Activity)
    val sesion: SesionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SesionViewModel(contenedor.auth, contenedor.perfiles, contenedor.frases, contenedor.prefs)
            }
        }
    )

    // si ya hay sesión guardada se salta el login
    val destinoInicial = remember { if (sesion.haySesion) Rutas.INICIO else Rutas.LOGIN }

    fun irAInicioLimpiando() = navController.navigate(Rutas.INICIO) {
        popUpTo(0) { inclusive = true }
    }

    fun volverAlLogin() = navController.navigate(Rutas.LOGIN) {
        popUpTo(0) { inclusive = true }
    }

    NavHost(navController = navController, startDestination = destinoInicial) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                sesion = sesion,
                alIngresar = { irAInicioLimpiando() },
                alRegistrarse = { navController.navigate(Rutas.REGISTRO) },
                alRecuperar = { navController.navigate(Rutas.RECUPERAR) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                sesion = sesion,
                alRegistrar = { irAInicioLimpiando() },
                alVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.RECUPERAR) {
            RecuperarScreen(sesion = sesion, alVolver = { navController.popBackStack() })
        }

        composable(Rutas.INICIO) {
            InicioScreen(
                sesion = sesion,
                alAbrirFrases = { navController.navigate(Rutas.FRASES) },
                alAbrirPerfil = { navController.navigate(Rutas.PERFIL) },
                alCerrarSesion = {
                    sesion.cerrarSesion()
                    volverAlLogin()
                }
            )
        }

        composable(Rutas.FRASES) {
            val uid = sesion.uid.orEmpty()
            val frases: FrasesViewModel = viewModel(
                key = "frases-$uid",
                factory = viewModelFactory {
                    initializer { FrasesViewModel(contenedor.frases, uid) }
                }
            )
            FrasesScreen(vm = frases, alVolver = { navController.popBackStack() })
        }

        composable(Rutas.PERFIL) {
            PerfilScreen(
                sesion = sesion,
                alVolver = { navController.popBackStack() },
                alEliminarCuenta = { volverAlLogin() }
            )
        }
    }
}
