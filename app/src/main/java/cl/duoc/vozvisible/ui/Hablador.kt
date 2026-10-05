package cl.duoc.vozvisible.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

// texto a voz con el motor del teléfono. no todos los equipos lo tienen instalado,
// por eso se comprueba antes de ofrecer el botón
class Hablador(context: Context) {

    var disponible by mutableStateOf(false)
        private set

    private var motor: TextToSpeech? = null

    init {
        motor = TextToSpeech(context.applicationContext) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                val resultado = motor?.setLanguage(Locale.forLanguageTag("es-CL"))
                disponible = resultado != TextToSpeech.LANG_MISSING_DATA &&
                    resultado != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun hablar(texto: String) {
        if (disponible) motor?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "frase")
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
