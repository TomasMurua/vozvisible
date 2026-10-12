package cl.duoc.vozvisible.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private val ESPANOL_CHILE: Locale = Locale.forLanguageTag("es-CL")

// texto a voz con el motor del teléfono. no todos los equipos lo tienen instalado,
// por eso se comprueba antes de ofrecer el botón
class Hablador(context: Context) {

    var disponible by mutableStateOf(false)
        private set

    // true mientras el teléfono está diciendo algo, para animar la pantalla
    var hablando by mutableStateOf(false)
        private set

    private var motor: TextToSpeech? = null

    init {
        motor = TextToSpeech(context.applicationContext) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                val resultado = motor?.setLanguage(ESPANOL_CHILE)
                disponible = resultado != TextToSpeech.LANG_MISSING_DATA &&
                    resultado != TextToSpeech.LANG_NOT_SUPPORTED
                motor?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { hablando = true }
                    override fun onDone(utteranceId: String?) { hablando = false }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { hablando = false }
                })
            }
        }
    }

    // velocidad 1.0 es la normal; más lento ayuda a que la otra persona entienda
    fun hablar(texto: String, velocidad: Float = 1.0f) {
        if (!disponible) return
        motor?.setSpeechRate(velocidad)
        motor?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "vozvisible")
    }

    fun callar() {
        motor?.stop()
        hablando = false
    }

    fun liberar() {
        motor?.stop()
        motor?.shutdown()
        motor = null
    }
}

@Composable
fun rememberHablador(): Hablador {
    val contexto = LocalContext.current
    val hablador = remember { Hablador(contexto) }
    DisposableEffect(hablador) { onDispose { hablador.liberar() } }
    return hablador
}

// voz a texto con el reconocedor del sistema (el de Google en la mayoría de los teléfonos).
// no hay que pedir el permiso del micrófono: lo pide la app que reconoce
class Escuchador(
    contexto: Context,
    private val lanzador: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val disponible: Boolean = SpeechRecognizer.isRecognitionAvailable(contexto)

    // false si el teléfono no tiene a quién pedirle el reconocimiento
    fun escuchar(): Boolean {
        val intento = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, ESPANOL_CHILE.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Acerca el teléfono a la persona que habla")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        return try {
            lanzador.launch(intento)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}

@Composable
fun rememberEscuchador(alEscuchar: (List<String>?) -> Unit): Escuchador {
    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        // si la persona cerró el diálogo sin hablar no se avisa nada
        if (resultado.resultCode == Activity.RESULT_OK) {
            alEscuchar(resultado.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))
        }
    }
    return remember(lanzador) { Escuchador(contexto, lanzador) }
}
