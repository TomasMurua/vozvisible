package cl.duoc.vozvisible.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroTest {

    @Test
    fun aMapaYDeVuelta_conservaLosDatos() {
        val original = Registro(
            id = "x1",
            tipo = TipoRegistro.ESCRIBIR,
            solicitud = "Escribir lo que me dicen",
            respuesta = "Su número es el 45",
            fecha = 1_760_000_000_000L
        )

        assertEquals(original, mapaARegistro("x1", original.aMapa()))
    }

    @Test
    fun mapaIncompleto_usaValoresPorDefecto() {
        val leido = mapaARegistro("y", mapOf("tipo" to "INVENTADO"))

        assertEquals(TipoRegistro.HABLAR, leido.tipo)
        assertEquals("", leido.solicitud)
        assertEquals(0L, leido.fecha)
    }

    @Test
    fun contarPorTipo_incluyeLosTiposSinRegistros() {
        val lista = listOf(
            Registro(tipo = TipoRegistro.HABLAR, solicitud = "a", respuesta = "b"),
            Registro(tipo = TipoRegistro.HABLAR, solicitud = "c", respuesta = "d")
        )

        assertEquals(mapOf(TipoRegistro.HABLAR to 2, TipoRegistro.ESCRIBIR to 0), lista.contarPorTipo())
    }

    @Test
    fun filtrarPor_nullDevuelveTodo() {
        val lista = listOf(
            Registro(tipo = TipoRegistro.HABLAR, solicitud = "a", respuesta = "b"),
            Registro(tipo = TipoRegistro.ESCRIBIR, solicitud = "c", respuesta = "d")
        )

        assertEquals(2, lista.filtrarPor(null).size)
        assertTrue(lista.filtrarPor(TipoRegistro.ESCRIBIR).all { it.tipo == TipoRegistro.ESCRIBIR })
    }

    @Test
    fun comoFecha_usaDiaMesAnio() {
        assertTrue(Regex("""\d{2}-\d{2}-\d{4} \d{2}:\d{2}""").matches(0L.comoFecha()))
    }
}
