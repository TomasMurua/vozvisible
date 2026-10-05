package cl.duoc.vozvisible.data

// frase guardada para decirla con el teléfono en situaciones repetidas
data class Frase(
    val id: String = "",
    val texto: String,
    val categoria: String,
    val creada: Long = System.currentTimeMillis()
)

val CATEGORIAS_FRASE = listOf("Saludo", "Salud", "Compras", "Transporte", "Otra")

const val LARGO_MINIMO_FRASE = 2
const val LARGO_MAXIMO_FRASE = 200

fun Frase.aMapa(): Map<String, Any> = mapOf(
    "texto" to texto,
    "categoria" to categoria,
    "creada" to creada
)

fun mapaAFrase(id: String, datos: Map<String, Any?>): Frase = Frase(
    id = id,
    texto = datos["texto"] as? String ?: "",
    categoria = datos["categoria"] as? String ?: CATEGORIAS_FRASE.last(),
    creada = (datos["creada"] as? Number)?.toLong() ?: 0L
)
