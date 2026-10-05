package cl.duoc.vozvisible.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// JUnit: la conversión entre los modelos y los mapas que se guardan en Firestore
class UsuarioFraseTest {

    private val usuario = Usuario(
        uid = "abc123",
        nombre = "Ana Pérez",
        correo = "ana@duocuc.cl",
        region = "Valparaíso",
        perfil = PerfilUsuario.APOYO,
        preferencias = listOf("Alto contraste", "Texto ampliado")
    )

    @Test
    fun usuario_idaYVueltaConservaTodosLosCampos() {
        val vuelta = mapaAUsuario("abc123", usuario.aMapa())
        assertEquals(usuario, vuelta)
    }

    @Test
    fun usuario_noGuardaLaContrasenaNiElUid() {
        val mapa = usuario.aMapa()
        assertTrue(mapa.keys.none { it.contains("contrasena") || it.contains("clave") })
        assertTrue("uid" !in mapa)
    }

    @Test
    fun usuario_conDocumentoIncompletoUsaValoresPorDefecto() {
        val parcial = mapaAUsuario("x", mapOf("nombre" to "Solo nombre"))
        assertEquals("Solo nombre", parcial.nombre)
        assertEquals(PerfilUsuario.PERSONA_SORDA, parcial.perfil)
        assertTrue(parcial.preferencias.isEmpty())
    }

    @Test
    fun usuario_conPerfilDesconocidoNoRevienta() {
        val mapa = usuario.aMapa() + ("perfil" to "ALGO_QUE_NO_EXISTE")
        assertEquals(PerfilUsuario.PERSONA_SORDA, mapaAUsuario("x", mapa).perfil)
    }

    @Test
    fun frase_idaYVuelta() {
        val frase = Frase(id = "f1", texto = "Necesito ayuda", categoria = "Salud", creada = 1234L)
        assertEquals(frase, mapaAFrase("f1", frase.aMapa()))
    }

    @Test
    fun frase_categoriaDesconocidaCaeEnOtra() {
        val frase = mapaAFrase("f1", mapOf("texto" to "Hola"))
        assertEquals("Otra", frase.categoria)
    }
}
