package cl.duoc.vozvisible.data

// error de negocio: el correo no corresponde a ninguna cuenta registrada
class CorreoNoRegistradoException(mensaje: String) : Exception(mensaje)

// simula el servicio externo que envía el correo de recuperación
object ServicioRecuperacion {

    fun enviarInstrucciones(correo: String): String {
        if (!UsuarioRepository.correoRegistrado(correo)) {
            throw CorreoNoRegistradoException("Ese correo no figura entre las cuentas registradas")
        }
        return "Enviamos las instrucciones de recuperación a $correo"
    }
}
