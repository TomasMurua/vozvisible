package cl.duoc.vozvisible.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TipoRegistro(val etiqueta: String) {
    HABLAR("Hablar"),
    ESCRIBIR("Escribir")
}

// cada vez que se usa Hablar o Escribir queda una entrada en el historial.
// solicitud es lo que pidió la persona y respuesta es lo que hizo la app
data class Registro(
    val id: String = "",
    val tipo: TipoRegistro,
    val solicitud: String,
    val respuesta: String,
    val fecha: Long = System.currentTimeMillis()
)

const val LARGO_MAXIMO_MENSAJE = 500

fun Registro.aMapa(): Map<String, Any> = mapOf(
    "tipo" to tipo.name,
    "solicitud" to solicitud,
    "respuesta" to respuesta,
    "fecha" to fecha
)

fun mapaARegistro(id: String, datos: Map<String, Any?>): Registro = Registro(
    id = id,
    tipo = (datos["tipo"] as? String)
        ?.let { nombre -> TipoRegistro.entries.firstOrNull { it.name == nombre } }
        ?: TipoRegistro.HABLAR,
    solicitud = datos["solicitud"] as? String ?: "",
    respuesta = datos["respuesta"] as? String ?: "",
    fecha = (datos["fecha"] as? Number)?.toLong() ?: 0L
)

// null = todos los tipos
fun List<Registro>.filtrarPor(tipo: TipoRegistro?): List<Registro> =
    if (tipo == null) this else filter { it.tipo == tipo }

fun List<Registro>.contarPorTipo(): Map<TipoRegistro, Int> {
    val cuenta = TipoRegistro.entries.associateWith { 0 }.toMutableMap()
    for (registro in this) {
        cuenta[registro.tipo] = cuenta.getValue(registro.tipo) + 1
    }
    return cuenta
}

fun Long.comoFecha(): String =
    SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(this))
