package cl.duoc.vozvisible.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import cl.duoc.vozvisible.ui.screens.AyudaScreen
import cl.duoc.vozvisible.ui.screens.EscribirScreen
import cl.duoc.vozvisible.ui.screens.FrasesScreen
import cl.duoc.vozvisible.ui.screens.HablarScreen
import cl.duoc.vozvisible.ui.screens.HistorialScreen
import cl.duoc.vozvisible.ui.screens.InicioScreen
import cl.duoc.vozvisible.ui.screens.LoginScreen
import cl.duoc.vozvisible.ui.screens.PerfilScreen
import cl.duoc.vozvisible.ui.screens.RecuperarScreen
import cl.duoc.vozvisible.ui.screens.RegistroScreen
import cl.duoc.vozvisible.ui.viewmodel.ComunicacionViewModel
import cl.duoc.vozvisible.ui.viewmodel.FrasesViewModel
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel

private const val DURACION_MS = 280

@Composable
fun VozVisibleNavGraph(
    contenedor: Contenedor,
    navController: NavHostController = rememberNavController()
) {
    // un solo SesionViewModel para toda la app (queda atado a la Activity)
    val sesion: SesionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SesionViewModel(
                    contenedor.auth, contenedor.perfiles, contenedor.frases, contenedor.historial, contenedor.prefs
                )
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

    // termina lo que se estaba haciendo y deja solo el menú en la pila
    fun volverAlMenu() {
        if (!navController.popBackStack(Rutas.INICIO, inclusive = false)) irAInicioLimpiando()
    }

    @Composable
    fun comunicacion(): ComunicacionViewModel {
        val uid = sesion.uid.orEmpty()
        return viewModel(
            key = "comunicacion-$uid",
            factory = viewModelFactory { initializer { ComunicacionViewModel(contenedor.historial, uid) } }
        )
    }

    // las pantallas nuevas entran desde la derecha y al volver salen por el mismo lado
    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        enterTransition = { fadeIn(tween(DURACION_MS)) + slideInHorizontally(tween(DURACION_MS)) { it / 4 } },
        exitTransition = { fadeOut(tween(DURACION_MS)) },
        popEnterTransition = { fadeIn(tween(DURACION_MS)) },
        popExitTransition = { fadeOut(tween(DURACION_MS)) + slideOutHorizontally(tween(DURACION_MS)) { it / 4 } }
    ) {

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
                alAbrir = { ruta -> navController.navigate(ruta) { launchSingleTop = true } },
                alCerrarSesion = {
                    sesion.cerrarSesion()
                    volverAlLogin()
                }
            )
        }

        composable(Rutas.HABLAR) {
            HablarScreen(vm = comunicacion(), alVolver = { volverAlMenu() })
        }

        composable(Rutas.ESCRIBIR) {
            EscribirScreen(
                vm = comunicacion(),
                alVolver = { volverAlMenu() },
                alResponder = { navController.navigate(Rutas.HABLAR) { launchSingleTop = true } }
            )
        }

        composable(Rutas.HISTORIAL) {
            HistorialScreen(vm = comunicacion(), alVolver = { volverAlMenu() })
        }

        composable(Rutas.AYUDA) {
            AyudaScreen(alVolver = { volverAlMenu() })
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
