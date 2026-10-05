package cl.duoc.vozvisible.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vozvisible.data.CATEGORIAS_FRASE
import cl.duoc.vozvisible.data.Frase
import cl.duoc.vozvisible.data.FraseRepository
import cl.duoc.vozvisible.data.LARGO_MAXIMO_FRASE
import cl.duoc.vozvisible.data.LARGO_MINIMO_FRASE
import cl.duoc.vozvisible.data.comoErrorDeApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TIEMPO_MAXIMO_MS = 10_000L

// CRUD de frases rápidas del usuario. consultar es un Flow que se actualiza solo
class FrasesViewModel(
    private val repo: FraseRepository,
    private val uid: String
) : ViewModel() {

    private val _mensaje = MutableStateFlow<Mensaje?>(null)
    val mensaje: StateFlow<Mensaje?> = _mensaje.asStateFlow()

    private val _cargando = MutableStateFlow(true)

    // null mientras llega la primera lectura
    val frases: StateFlow<List<Frase>?> = repo.observar(uid)
        .catch { _mensaje.value = Mensaje(it.comoErrorDeApp().message.orEmpty(), true); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // devuelve el error de validación o null si todo está bien
    fun validar(texto: String): String? {
        val limpio = texto.trim()
        return when {
            limpio.length < LARGO_MINIMO_FRASE -> "Escribe una frase de al menos $LARGO_MINIMO_FRASE letras"
            limpio.length > LARGO_MAXIMO_FRASE -> "La frase puede tener hasta $LARGO_MAXIMO_FRASE letras"
            else -> null
        }
    }

    // id == null: crear. con id: modificar
    fun guardar(id: String?, texto: String, categoria: String, alExito: () -> Unit) {
        if (validar(texto) != null) return
        val cat = if (categoria in CATEGORIAS_FRASE) categoria else CATEGORIAS_FRASE.last()
        ejecutar(if (id == null) "Frase guardada" else "Frase actualizada") {
            if (id == null) repo.crear(uid, Frase(texto = texto.trim(), categoria = cat))
            else repo.actualizar(uid, Frase(id = id, texto = texto.trim(), categoria = cat))
            alExito()
        }
    }

    fun eliminar(id: String) = ejecutar("Frase eliminada") { repo.eliminar(uid, id) }

    fun limpiarMensaje() { _mensaje.value = null }

    private fun ejecutar(exito: String, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                withTimeout(TIEMPO_MAXIMO_MS) { bloque() }
                _mensaje.value = Mensaje(exito, false)
            } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) throw e
                _mensaje.value = Mensaje(e.comoErrorDeApp().message.orEmpty(), true)
            }
        }
    }
}
