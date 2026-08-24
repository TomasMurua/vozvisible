package cl.duoc.vozvisible.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

// los usuarios quedan en un arreglo fijo de 5, todavia sin base de datos.
// el contador de version es para que Compose note los cambios, porque un Array
// normal no avisa cuando lo modificas.
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

    // solo los casilleros ocupados
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

    // busca el primer casillero libre y mete al usuario ahi
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

    // deja visibles solo las 2 primeras letras
    fun contrasenaEnmascarada(usuario: Usuario): String =
        usuario.contrasena.take(2) + "*".repeat((usuario.contrasena.length - 2).coerceAtLeast(0))
}

sealed interface ResultadoRegistro {
    data class Exitoso(val posicion: Int) : ResultadoRegistro
    data object CorreoDuplicado : ResultadoRegistro
    data object SinCupos : ResultadoRegistro
}
