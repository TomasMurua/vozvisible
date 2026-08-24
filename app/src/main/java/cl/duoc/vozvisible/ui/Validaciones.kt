package cl.duoc.vozvisible.ui

private val PATRON_CORREO = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$")

fun correoValido(correo: String): Boolean = PATRON_CORREO.matches(correo.trim())

// minimo 8, que tenga letras y numeros
fun contrasenaValida(contrasena: String): Boolean =
    contrasena.length >= 8 && contrasena.any { it.isLetter() } && contrasena.any { it.isDigit() }
