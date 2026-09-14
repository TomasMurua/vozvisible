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

// propiedad de extension: no es un campo de la clase, pero se usa como si lo fuera.
// deja visibles solo las 2 primeras letras de la contraseña
val Usuario.contrasenaEnmascarada: String
    get() = contrasena.take(2) + "*".repeat((contrasena.length - 2).coerceAtLeast(0))
