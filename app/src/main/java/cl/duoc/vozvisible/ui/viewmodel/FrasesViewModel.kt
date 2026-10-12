package cl.duoc.vozvisible.ui.viewmodel

import androidx.lifecycle.viewModelScope
import cl.duoc.vozvisible.data.CATEGORIAS_FRASE
import cl.duoc.vozvisible.data.Frase
import cl.duoc.vozvisible.data.FraseRepository
import cl.duoc.vozvisible.data.LARGO_MAXIMO_FRASE
import cl.duoc.vozvisible.data.LARGO_MINIMO_FRASE
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

// CRUD de frases rápidas del usuario. consultar es un Flow que se actualiza solo
class FrasesViewModel(
    private val repo: FraseRepository,
    private val uid: String
) : BaseViewModel() {

    // null mientras llega la primera lectura
    val frases: StateFlow<List<Frase>?> = repo.observar(uid)
        .catch { avisarError(it); emit(emptyList()) }
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
}
