package cl.duoc.vozvisible.ui

private val PATRON_CORREO = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$")

// funciones de extension sobre String, se llaman como si fueran parte de la clase
fun String.esCorreoValido(): Boolean = PATRON_CORREO.matches(trim())

// minimo 8, que tenga letras y numeros
fun String.esContrasenaValida(): Boolean =
    length >= 8 && any { it.isLetter() } && any { it.isDigit() }

// funcion de orden superior: la regla de validacion llega como lambda y el mensaje
// solo se arma cuando esa regla no se cumple
fun validarCampo(valor: String, mensajeError: String, regla: (String) -> Boolean): String? =
    if (regla(valor)) null else mensajeError

// funcion que retorna otra funcion, para armar reglas de largo minimo sin repetirlas
fun exigirLargoMinimo(minimo: Int): (String) -> Boolean = { it.trim().length >= minimo }
