package cl.duoc.vozvisible.data

// datos que se piden en el registro
data class Usuario(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val region: String,
    val perfil: PerfilUsuario,
    val preferencias: List<String>
)

enum class PerfilUsuario(val etiqueta: String) {
    PERSONA_SORDA("Persona sorda o con hipoacusia"),
    APOYO("Familiar, cuidador o intérprete"),
    PROFESIONAL("Profesional de salud o educación")
}
