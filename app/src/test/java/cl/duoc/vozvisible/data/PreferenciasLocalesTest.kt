package cl.duoc.vozvisible.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric: SharedPreferences real, pero corriendo en el computador, sin emulador
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreferenciasLocalesTest {

    private lateinit var prefs: PreferenciasLocales

    @Before
    fun preparar() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        contexto.getSharedPreferences("vozvisible", Context.MODE_PRIVATE).edit().clear().commit()
        prefs = PreferenciasLocales(contexto)
    }

    @Test
    fun sinNadaGuardado_noHaySesionNiCorreo() {
        assertFalse(prefs.sesionActiva)
        assertNull(prefs.correoRecordado)
        assertNull(prefs.nombreSesion)
    }

    @Test
    fun guardarSesion_dejaUidYNombreDisponibles() {
        prefs.guardarSesion("uid-1", "Ana")

        assertTrue(prefs.sesionActiva)
        assertEquals("Ana", prefs.nombreSesion)
    }

    @Test
    fun laSesionSobreviveAUnaNuevaInstancia() {
        prefs.guardarSesion("uid-1", "Ana")

        val otra = PreferenciasLocales(ApplicationProvider.getApplicationContext())

        assertTrue(otra.sesionActiva)
    }

    @Test
    fun limpiarSesion_noBorraElCorreoRecordado() {
        prefs.correoRecordado = "ana@duocuc.cl"
        prefs.guardarSesion("uid-1", "Ana")

        prefs.limpiarSesion()

        assertFalse(prefs.sesionActiva)
        assertEquals("ana@duocuc.cl", prefs.correoRecordado)
    }

    @Test
    fun correoRecordado_enBlancoLoElimina() {
        prefs.correoRecordado = "ana@duocuc.cl"
        prefs.correoRecordado = "  "

        assertNull(prefs.correoRecordado)
    }
}
