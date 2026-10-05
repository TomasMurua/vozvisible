package cl.duoc.vozvisible.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// JUnit: pruebas de las funciones de validación del registro y el login
class ValidacionesTest {

    @Test
    fun correoConFormatoValido_esAceptado() {
        assertTrue("demo@vozvisible.cl".esCorreoValido())
        assertTrue("  ana.perez+app@duocuc.cl ".esCorreoValido())
    }

    @Test
    fun correoSinArrobaOSinDominio_esRechazado() {
        assertFalse("demovozvisible.cl".esCorreoValido())
        assertFalse("demo@".esCorreoValido())
        assertFalse("demo@vozvisible".esCorreoValido())
        assertFalse("".esCorreoValido())
    }

    @Test
    fun contrasenaConLetrasNumerosYOchoCaracteres_esValida() {
        assertTrue("Demo1234".esContrasenaValida())
    }

    @Test
    fun contrasenaCortaOSinNumerosOSinLetras_esInvalida() {
        assertFalse("Ab1".esContrasenaValida())
        assertFalse("soloLetrasAqui".esContrasenaValida())
        assertFalse("1234567890".esContrasenaValida())
    }

    @Test
    fun validarCampo_devuelveNullSiLaReglaSeCumple() {
        assertNull(validarCampo("hola", "error") { it.isNotBlank() })
    }

    @Test
    fun validarCampo_devuelveElMensajeSiLaReglaFalla() {
        assertEquals("error", validarCampo("  ", "error") { it.isNotBlank() })
    }

    @Test
    fun exigirLargoMinimo_ignoraEspaciosAlRededor() {
        val regla = exigirLargoMinimo(3)
        assertTrue(regla("abc"))
        assertFalse(regla("  ab  "))
    }
}
