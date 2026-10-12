package cl.duoc.vozvisible.ui.viewmodel

import androidx.lifecycle.viewModelScope
import cl.duoc.vozvisible.data.LARGO_MAXIMO_MENSAJE
import cl.duoc.vozvisible.data.Registro
import cl.duoc.vozvisible.data.RegistroRepository
import cl.duoc.vozvisible.data.TipoRegistro
import cl.duoc.vozvisible.data.contarPorTipo
import cl.duoc.vozvisible.data.filtrarPor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

const val RESPUESTA_EN_VOZ = "Dicho en voz alta y mostrado en pantalla"
const val RESPUESTA_SIN_VOZ = "Solo mostrado en pantalla, el teléfono no tiene voz en español"
const val SOLICITUD_ESCRIBIR = "Escribir lo que me dicen"

// Hablar pasa texto a voz, Escribir pasa voz a texto, y los dos dejan registro en el historial
class ComunicacionViewModel(
    private val repo: RegistroRepository,
    private val uid: String
) : BaseViewModel() {

    private val _filtro = MutableStateFlow<TipoRegistro?>(null)
    val filtro: StateFlow<TipoRegistro?> = _filtro.asStateFlow()

    private val todos = repo.observar(uid)
        .catch { avisarError(it); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // null mientras llega la primera lectura
    val historial: StateFlow<List<Registro>?> = combine(todos, _filtro) { lista, tipo ->
        lista?.filtrarPor(tipo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val resumen: StateFlow<Map<TipoRegistro, Int>> = todos
        .map { it.orEmpty().contarPorTipo() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun filtrar(tipo: TipoRegistro?) { _filtro.value = tipo }

    fun validar(texto: String): String? {
        val limpio = texto.trim()
        return when {
            limpio.isEmpty() -> "Escribe lo que quieres decir"
            limpio.length > LARGO_MAXIMO_MENSAJE -> "El mensaje puede tener hasta $LARGO_MAXIMO_MENSAJE letras"
            else -> null
        }
    }

    // la vista ya lo dijo en voz alta; aquí solo queda guardado
    fun registrarHablado(texto: String, enVoz: Boolean): Boolean {
        if (validar(texto) != null) return false
        val respuesta = if (enVoz) RESPUESTA_EN_VOZ else RESPUESTA_SIN_VOZ
        guardar(Registro(tipo = TipoRegistro.HABLAR, solicitud = texto.trim(), respuesta = respuesta))
        return true
    }

    // el reconocedor de voz entrega varias opciones ordenadas de más a menos probable
    fun registrarTranscripcion(opciones: List<String>?): String? {
        val texto = opciones.orEmpty()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
        if (texto == null) {
            avisar("No se alcanzó a entender, pide que lo repitan más cerca del teléfono", true)
            return null
        }
        guardar(Registro(tipo = TipoRegistro.ESCRIBIR, solicitud = SOLICITUD_ESCRIBIR, respuesta = texto))
        return texto
    }

    fun eliminar(id: String) = ejecutar("Eliminado del historial") { repo.eliminar(uid, id) }

    fun eliminarTodo() = ejecutar("Historial borrado") { repo.eliminarTodos(uid) }

    private fun guardar(registro: Registro) = ejecutar(null) { repo.crear(uid, registro) }
}
