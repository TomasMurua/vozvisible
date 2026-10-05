package cl.duoc.vozvisible.data

// perfil del usuario. la contraseña ya no se guarda aquí: la maneja Firebase Authentication
data class Usuario(
    val uid: String = "",
    val nombre: String,
    val correo: String,
    val region: String,
    val perfil: PerfilUsuario,
    val preferencias: List<String>
)

enum class PerfilUsuario(val etiqueta: String) {
    PERSONA_SORDA("Persona sorda o con hipoacusia"),
    APOYO("Familiar, cuidador o intérprete"),
    PROFESIONAL("Profesional de salud o educación")
}

// así se guarda en Firestore (colección usuarios, documento = uid)
fun Usuario.aMapa(): Map<String, Any> = mapOf(
    "nombre" to nombre,
    "correo" to correo,
    "region" to region,
    "perfil" to perfil.name,
    "preferencias" to preferencias
)

// y así se lee de vuelta. si el documento viene incompleto no revienta, usa valores por defecto
fun mapaAUsuario(uid: String, datos: Map<String, Any?>): Usuario = Usuario(
    uid = uid,
    nombre = datos["nombre"] as? String ?: "",
    correo = datos["correo"] as? String ?: "",
    region = datos["region"] as? String ?: "",
    perfil = (datos["perfil"] as? String)
        ?.let { nombre -> PerfilUsuario.entries.firstOrNull { it.name == nombre } }
        ?: PerfilUsuario.PERSONA_SORDA,
    preferencias = (datos["preferencias"] as? List<*>)?.filterIsInstance<String>().orEmpty()
)
