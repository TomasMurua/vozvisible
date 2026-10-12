package cl.duoc.vozvisible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.vozvisible.data.HistorialFalso
import cl.duoc.vozvisible.data.Registro
import cl.duoc.vozvisible.data.TipoRegistro
import cl.duoc.vozvisible.ui.screens.AyudaScreen
import cl.duoc.vozvisible.ui.screens.HablarScreen
import cl.duoc.vozvisible.ui.screens.HistorialScreen
import cl.duoc.vozvisible.ui.theme.VozVisibleTheme
import cl.duoc.vozvisible.ui.viewmodel.ComunicacionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// pruebas de interfaz de Hablar, Historial y Ayuda con un historial en memoria
@RunWith(AndroidJUnit4::class)
class ComunicacionUiTest {

    @get:Rule
    val regla = createComposeRule()

    private val historial = HistorialFalso()
    private var volvio = false

    private fun mostrarHablar() {
        val vm = ComunicacionViewModel(historial, "uid")
        regla.setContent { VozVisibleTheme { HablarScreen(vm = vm, alVolver = { volvio = true }) } }
    }

    private fun botonDecir() = regla.onNode(
        androidx.compose.ui.test.hasText("Decir en voz alta").or(androidx.compose.ui.test.hasText("Mostrar en grande"))
    )

    @Test
    fun hablar_sinTexto_muestraElErrorYNoGuarda() {
        mostrarHablar()

        botonDecir().performClick()

        regla.onNodeWithText("Escribe lo que quieres decir").assertIsDisplayed()
        assertTrue(historial.registros.value.isEmpty())
    }

    @Test
    fun hablar_conTexto_loMuestraEnGrandeYLoGuardaEnElHistorial() {
        mostrarHablar()

        regla.onNodeWithText("Tu mensaje").performTextInput("Necesito un taxi")
        botonDecir().performClick()
        regla.waitForIdle()

        regla.onNodeWithText("Nuevo mensaje").assertIsDisplayed()
        assertEquals(1, historial.registros.value.size)
        assertEquals("Necesito un taxi", historial.registros.value.first().solicitud)
        assertEquals(TipoRegistro.HABLAR, historial.registros.value.first().tipo)
    }

    @Test
    fun hablar_unaSugerencia_llenaElCampo() {
        mostrarHablar()

        regla.onNodeWithText("¿Me puede escribir?", substring = true).performClick()
        botonDecir().performClick()
        regla.waitForIdle()

        assertEquals("Hola, soy sordo/a. ¿Me puede escribir?", historial.registros.value.first().solicitud)
    }

    @Test
    fun hablar_terminar_vuelveAlMenu() {
        mostrarHablar()

        regla.onNodeWithContentDescription("Volver al menú").performClick()

        assertTrue(volvio)
    }

    @Test
    fun historial_muestraLosRegistrosYPermiteBorrarUno() {
        historial.registros.value = listOf(
            Registro(id = "a", tipo = TipoRegistro.HABLAR, solicitud = "Buenos días", respuesta = "Dicho en voz alta"),
            Registro(id = "b", tipo = TipoRegistro.ESCRIBIR, solicitud = "Escribir", respuesta = "Pase por la caja dos")
        )
        val vm = ComunicacionViewModel(historial, "uid")
        regla.setContent { VozVisibleTheme { HistorialScreen(vm = vm, alVolver = {}) } }

        regla.onNodeWithText("Buenos días").assertIsDisplayed()
        regla.onNodeWithText("Pase por la caja dos").assertIsDisplayed()

        regla.onNodeWithContentDescription("Eliminar del historial: Buenos días").performClick()
        regla.waitForIdle()

        assertEquals(listOf("b"), historial.registros.value.map { it.id })
    }

    @Test
    fun ayuda_alTocarUnaPregunta_seAbreLaRespuesta() {
        regla.setContent { VozVisibleTheme { AyudaScreen(alVolver = {}) } }

        regla.onNodeWithText("¿Qué hace Escribir?").performClick()

        regla.onNodeWithText("Tocas el micrófono", substring = true).assertIsDisplayed()
    }
}
