package cl.duoc.vozvisible.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * Almacen en memoria de los perfiles registrados.
 *
 * El enunciado de la entrega fija la capacidad en cinco cuentas, por eso el respaldo es un
 * arreglo de tamano fijo y no una lista dinamica. [version] existe para que Compose vuelva a
 * componer las pantallas cuando el arreglo cambia, ya que un Array no es observable por si mismo.
 */
object UsuarioRepository {

    const val CAPACIDAD = 5

    private val usuarios = arrayOfNulls<Usuario>(CAPACIDAD)

    private var version by mutableIntStateOf(0)

    init {
        usuarios[0] = Usuario(
            nombre = "Cuenta de demostración",
            correo = "demo@vozvisible.cl",
            contrasena = "Demo1234",
            region = "Región Metropolitana",
            perfil = PerfilUsuario.PERSONA_SORDA,
            preferencias = listOf("Subtítulos automáticos", "Alerta por vibración")
        )
    }

    /** Perfiles ocupados del arreglo, en orden de registro. */
    fun registrados(): List<Usuario> {
        version
        return usuarios.filterNotNull()
    }

    fun cuposDisponibles(): Int {
        version
        return usuarios.count { it == null }
    }

    fun correoRegistrado(correo: String): Boolean =
        registrados().any { it.correo.equals(correo.trim(), ignoreCase = true) }

    /**
     * Coloca al usuario en el primer casillero libre del arreglo.
     * Devuelve el resultado para que la vista de registro decida que mensaje mostrar.
     */
    fun registrar(usuario: Usuario): ResultadoRegistro {
        if (correoRegistrado(usuario.correo)) return ResultadoRegistro.CorreoDuplicado
        val libre = usuarios.indexOfFirst { it == null }
        if (libre == -1) return ResultadoRegistro.SinCupos
        usuarios[libre] = usuario
        version++
        return ResultadoRegistro.Exitoso(libre + 1)
    }

    fun autenticar(correo: String, contrasena: String): Usuario? =
        registrados().firstOrNull {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.contrasena == contrasena
        }

    /** Enmascara la contrasena para poder mostrarla en pantalla sin exponerla completa. */
    fun contrasenaEnmascarada(usuario: Usuario): String =
        usuario.contrasena.take(2) + "*".repeat((usuario.contrasena.length - 2).coerceAtLeast(0))
}

sealed interface ResultadoRegistro {
    data class Exitoso(val posicion: Int) : ResultadoRegistro
    data object CorreoDuplicado : ResultadoRegistro
    data object SinCupos : ResultadoRegistro
}
