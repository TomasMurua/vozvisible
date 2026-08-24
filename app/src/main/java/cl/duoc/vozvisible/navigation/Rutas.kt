package cl.duoc.vozvisible.navigation

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"
    const val INICIO = "inicio"

    const val ARG_CORREO = "correo"
    const val INICIO_CON_ARGUMENTO = "$INICIO/{$ARG_CORREO}"

    fun inicioDe(correo: String) = "$INICIO/$correo"
}
