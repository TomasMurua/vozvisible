package cl.duoc.vozvisible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.vozvisible.data.AuthRepository
import cl.duoc.vozvisible.data.Frase
import cl.duoc.vozvisible.data.FraseRepository
import cl.duoc.vozvisible.data.PerfilRepository
import cl.duoc.vozvisible.data.PerfilUsuario
import cl.duoc.vozvisible.data.Preferencias
import cl.duoc.vozvisible.data.HistorialFalso
import cl.duoc.vozvisible.data.Usuario
import cl.duoc.vozvisible.ui.screens.LoginScreen
import cl.duoc.vozvisible.ui.theme.VozVisibleTheme
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Espresso / Compose UI test: se maneja la pantalla de login como lo haría una persona,
// pero con repositorios falsos para no depender de internet ni de Firebase
@RunWith(AndroidJUnit4::class)
class LoginUiTest {

    @get:Rule
    val regla = createComposeRule()

    private class AuthFalso : AuthRepository {
        var claveCorrecta = "Demo1234"
        override var uidActual: String? = null
        override val correoActual: String? get() = null
        override suspend fun registrar(correo: String, clave: String) = "uid"
        override suspend fun ingresar(correo: String, clave: String): String {
            if (clave != claveCorrecta) throw cl.duoc.vozvisible.data.AuthException(
                cl.duoc.vozvisible.data.MotivoError.CREDENCIALES
            )
            uidActual = "uid"
            return "uid"
        }
        override suspend fun enviarRecuperacion(correo: String) {}
        override suspend fun reautenticar(clave: String) {}
        override suspend fun eliminarCuenta() {}
        override fun cerrarSesion() { uidActual = null }
    }

    private class PerfilesFalso : PerfilRepository {
        override suspend fun guardar(usuario: Usuario) {}
        override suspend fun obtener(uid: String) = Usuario(
            uid, "Demo", "demo@vozvisible.cl", "Biobío", PerfilUsuario.PERSONA_SORDA, emptyList()
        )
        override suspend fun eliminar(uid: String) {}
    }

    private class FrasesFalso : FraseRepository {
        override fun observar(uid: String): Flow<List<Frase>> = flowOf(emptyList())
        override suspend fun crear(uid: String, frase: Frase) {}
        override suspend fun actualizar(uid: String, frase: Frase) {}
        override suspend fun eliminar(uid: String, id: String) {}
        override suspend fun eliminarTodas(uid: String) {}
    }

    private class PrefsFalsas : Preferencias {
        override var correoRecordado: String? = null
        override var sesionActiva = false
        override var nombreSesion: String? = null
        override fun guardarSesion(uid: String, nombre: String) { sesionActiva = true; nombreSesion = nombre }
        override fun limpiarSesion() { sesionActiva = false }
    }

    private var ingreso = false

    private fun mostrarLogin() {
        val vm = SesionViewModel(AuthFalso(), PerfilesFalso(), FrasesFalso(), HistorialFalso(), PrefsFalsas())
        regla.setContent {
            VozVisibleTheme {
                LoginScreen(
                    sesion = vm,
                    alIngresar = { ingreso = true },
                    alRegistrarse = {},
                    alRecuperar = {}
                )
            }
        }
    }

    @Test
    fun alPulsarIngresarConLosCamposVacios_muestraLosDosErrores() {
        mostrarLogin()

        regla.onNodeWithText("Ingresar").performClick()

        regla.onNodeWithText("Escribe un correo con formato válido").assertIsDisplayed()
        regla.onNodeWithText("Ingresa tu contraseña").assertIsDisplayed()
    }

    @Test
    fun conUnCorreoSinFormato_muestraElErrorDeCorreo() {
        mostrarLogin()

        regla.onNodeWithText("Correo electrónico").performTextInput("esto-no-es-un-correo")
        regla.onNodeWithText("Contraseña").performTextInput("Demo1234")
        regla.onNodeWithText("Ingresar").performClick()

        regla.onNodeWithText("Escribe un correo con formato válido").assertIsDisplayed()
    }

    @Test
    fun conUnaClaveIncorrecta_muestraElMensajeDeAcceso() {
        mostrarLogin()

        regla.onNodeWithText("Correo electrónico").performTextInput("demo@vozvisible.cl")
        regla.onNodeWithText("Contraseña").performTextInput("incorrecta1")
        regla.onNodeWithText("Ingresar").performClick()
        regla.waitForIdle()

        regla.onNodeWithText("Correo o contraseña incorrectos").assertIsDisplayed()
    }

    @Test
    fun conCredencialesCorrectas_navegaAlInicio() {
        mostrarLogin()

        regla.onNodeWithText("Correo electrónico").performTextInput("demo@vozvisible.cl")
        regla.onNodeWithText("Contraseña").performTextInput("Demo1234")
        regla.onNodeWithText("Ingresar").performClick()
        regla.waitForIdle()

        assertTrue(ingreso)
    }
}
