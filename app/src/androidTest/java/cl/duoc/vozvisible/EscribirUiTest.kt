package cl.duoc.vozvisible

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.vozvisible.data.HistorialFalso
import cl.duoc.vozvisible.data.TipoRegistro
import cl.duoc.vozvisible.ui.screens.EscribirScreen
import cl.duoc.vozvisible.ui.theme.VozVisibleTheme
import cl.duoc.vozvisible.ui.viewmodel.ComunicacionViewModel
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// Espresso-Intents responde en lugar del reconocedor de voz de Google,
// así la prueba no depende del micrófono ni de internet
@RunWith(AndroidJUnit4::class)
class EscribirUiTest {

    @get:Rule
    val regla = createAndroidComposeRule<ComponentActivity>()

    private val historial = HistorialFalso()

    @Before
    fun preparar() = Intents.init()

    @After
    fun limpiar() = Intents.release()

    private fun responderReconocedor(vararg opciones: String) {
        val datos = Intent().putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arrayListOf(*opciones))
        intending(hasAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, datos))
    }

    private fun mostrar() {
        val vm = ComunicacionViewModel(historial, "uid")
        regla.setContent {
            VozVisibleTheme { EscribirScreen(vm = vm, alVolver = {}, alResponder = {}) }
        }
    }

    @Test
    fun alEscuchar_muestraLoQueDijeronYLoGuardaEnElHistorial() {
        responderReconocedor("Su pedido está listo, pase por la caja dos", "su pedido esta listo")
        mostrar()

        regla.onNodeWithContentDescription("Escuchar a la otra persona").performClick()
        regla.waitForIdle()

        regla.onNodeWithText("Su pedido está listo, pase por la caja dos").assertIsDisplayed()
        intended(allOf(hasAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), hasExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CL")))
        assertEquals(TipoRegistro.ESCRIBIR, historial.registros.value.first().tipo)
        assertEquals("Su pedido está listo, pase por la caja dos", historial.registros.value.first().respuesta)

        guardarCaptura("escribir-resultado")
    }

    @Test
    fun siElReconocedorNoEntiendeNada_avisaYNoGuarda() {
        responderReconocedor("   ")
        mostrar()

        regla.onNodeWithContentDescription("Escuchar a la otra persona").performClick()
        regla.waitForIdle()

        regla.onNodeWithText("No se alcanzó a entender", substring = true).assertIsDisplayed()
        assertEquals(0, historial.registros.value.size)
    }

    // deja una imagen de la pantalla como evidencia de la prueba (se baja con adb pull)
    private fun guardarCaptura(nombre: String) {
        val carpeta = regla.activity.getExternalFilesDir("capturas") ?: return
        val imagen = regla.onRoot().captureToImage().asAndroidBitmap()
        File(carpeta, "$nombre.png").outputStream().use { imagen.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
