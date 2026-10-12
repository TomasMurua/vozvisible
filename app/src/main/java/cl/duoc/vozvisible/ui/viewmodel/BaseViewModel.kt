package cl.duoc.vozvisible.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vozvisible.data.comoErrorDeApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

// lo que comparten los view models que trabajan con Firestore: un mensaje para la vista,
// saber si hay una operación en curso y cortar si el servidor no responde
abstract class BaseViewModel(private val tiempoMaximoMs: Long = 10_000L) : ViewModel() {

    private val _mensaje = MutableStateFlow<Mensaje?>(null)
    val mensaje: StateFlow<Mensaje?> = _mensaje.asStateFlow()

    private val _ocupado = MutableStateFlow(false)
    val ocupado: StateFlow<Boolean> = _ocupado.asStateFlow()

    fun limpiarMensaje() { _mensaje.value = null }

    protected fun avisar(texto: String, esError: Boolean) {
        _mensaje.value = Mensaje(texto, esError)
    }

    protected fun avisarError(error: Throwable) =
        avisar(error.comoErrorDeApp().message.orEmpty(), true)

    // exito == null: no se muestra nada si sale bien
    protected fun ejecutar(exito: String?, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            _ocupado.value = true
            try {
                withTimeout(tiempoMaximoMs) { bloque() }
                if (exito != null) avisar(exito, false)
            } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) throw e
                avisarError(e)
            } finally {
                // salga bien o mal, el botón vuelve a quedar disponible
                _ocupado.value = false
            }
        }
    }
}
