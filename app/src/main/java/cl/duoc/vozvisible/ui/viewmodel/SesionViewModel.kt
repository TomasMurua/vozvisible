package cl.duoc.vozvisible.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vozvisible.data.AuthRepository
import cl.duoc.vozvisible.data.FraseRepository
import cl.duoc.vozvisible.data.PerfilRepository
import cl.duoc.vozvisible.data.PerfilUsuario
import cl.duoc.vozvisible.data.Preferencias
import cl.duoc.vozvisible.data.Usuario
import cl.duoc.vozvisible.data.comoErrorDeApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class Mensaje(val texto: String, val esError: Boolean)

data class EstadoSesion(
    val cargando: Boolean = false,
    val usuario: Usuario? = null,
    val mensaje: Mensaje? = null
)

private const val TIEMPO_MAXIMO_MS = 15_000L

// maneja el acceso (login, registro, recuperar, cerrar sesión) y el perfil del usuario.
// vive a nivel de Activity, así las pantallas comparten la misma sesión
class SesionViewModel(
    private val auth: AuthRepository,
    private val perfiles: PerfilRepository,
    private val frases: FraseRepository,
    private val prefs: Preferencias
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoSesion())
    val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    // Firebase recuerda la sesión por su cuenta; SharedPreferences confirma que fue una sesión nuestra
    val haySesion: Boolean get() = auth.uidActual != null && prefs.sesionActiva

    val uid: String? get() = auth.uidActual
    val correoRecordado: String? get() = prefs.correoRecordado

    init {
        if (haySesion) cargarPerfil()
    }

    private fun cargarPerfil() = lanzar(mostrarCarga = false) {
        val perfil = perfiles.obtener(auth.uidActual!!)
        _estado.update { it.copy(usuario = perfil) }
    }

    fun ingresar(correo: String, clave: String, recordar: Boolean, alExito: () -> Unit) =
        lanzar {
            val uid = auth.ingresar(correo.trim(), clave)
            val perfil = perfiles.obtener(uid)
            prefs.correoRecordado = if (recordar) correo.trim() else null
            prefs.guardarSesion(uid, perfil?.nombre.orEmpty())
            _estado.update { it.copy(usuario = perfil) }
            alExito()
        }

    fun registrar(usuario: Usuario, clave: String, alExito: () -> Unit) = lanzar {
        val uid = auth.registrar(usuario.correo.trim(), clave)
        val perfil = usuario.copy(uid = uid, correo = usuario.correo.trim())
        try {
            perfiles.guardar(perfil)
        } catch (e: Exception) {
            // si el perfil no se pudo guardar no dejamos una cuenta a medias
            if (e is CancellationException) throw e
            runCatching { auth.eliminarCuenta() }
            throw e
        }
        prefs.guardarSesion(uid, perfil.nombre)
        _estado.update { it.copy(usuario = perfil) }
        alExito()
    }

    fun recuperar(correo: String) = lanzar {
        auth.enviarRecuperacion(correo.trim())
        _estado.update {
            it.copy(
                mensaje = Mensaje(
                    "Si ${correo.trim()} tiene una cuenta, te enviamos las instrucciones para recuperarla",
                    esError = false
                )
            )
        }
    }

    fun actualizarPerfil(
        nombre: String,
        region: String,
        perfil: PerfilUsuario,
        preferencias: List<String>,
        alExito: () -> Unit
    ) = lanzar {
        val actual = _estado.value.usuario ?: return@lanzar
        val nuevo = actual.copy(
            nombre = nombre.trim(),
            region = region,
            perfil = perfil,
            preferencias = preferencias
        )
        perfiles.guardar(nuevo)
        prefs.guardarSesion(nuevo.uid, nuevo.nombre)
        _estado.update { it.copy(usuario = nuevo, mensaje = Mensaje("Perfil actualizado", false)) }
        alExito()
    }

    // borra frases, perfil y por último la cuenta (las reglas de Firestore piden estar autenticado)
    fun eliminarCuenta(clave: String, alExito: () -> Unit) = lanzar {
        auth.reautenticar(clave)
        val uid = auth.uidActual!!
        frases.eliminarTodas(uid)
        perfiles.eliminar(uid)
        auth.eliminarCuenta()
        prefs.limpiarSesion()
        _estado.value = EstadoSesion()
        alExito()
    }

    fun cerrarSesion() {
        auth.cerrarSesion()
        prefs.limpiarSesion()
        _estado.value = EstadoSesion()
    }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null) }

    private fun lanzar(mostrarCarga: Boolean = true, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = mostrarCarga, mensaje = null) }
            try {
                withTimeout(TIEMPO_MAXIMO_MS) { bloque() }
                _estado.update { it.copy(cargando = false) }
            } catch (e: Exception) {
                if (e is CancellationException && e !is kotlinx.coroutines.TimeoutCancellationException) throw e
                _estado.update {
                    it.copy(cargando = false, mensaje = Mensaje(e.comoErrorDeApp().message.orEmpty(), true))
                }
            }
        }
    }
}
