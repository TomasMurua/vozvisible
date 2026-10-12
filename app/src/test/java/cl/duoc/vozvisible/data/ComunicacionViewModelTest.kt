package cl.duoc.vozvisible.data

import cl.duoc.vozvisible.ui.viewmodel.ComunicacionViewModel
import cl.duoc.vozvisible.ui.viewmodel.RESPUESTA_EN_VOZ
import cl.duoc.vozvisible.ui.viewmodel.RESPUESTA_SIN_VOZ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// Hablar, Escribir y el historial: lo que se guarda, los filtros y los errores
@OptIn(ExperimentalCoroutinesApi::class)
class ComunicacionViewModelTest {

    private val repo: RegistroRepository = mock()
    private val datos = MutableStateFlow<List<Registro>>(emptyList())

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(repo.observar("u1")).thenReturn(datos)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun crear() = ComunicacionViewModel(repo, "u1")

    private fun registro(id: String, tipo: TipoRegistro) =
        Registro(id = id, tipo = tipo, solicitud = "s$id", respuesta = "r$id")

    @Test
    fun validar_rechazaVacioYMuyLargo() {
        val vm = crear()
        assertNotNull(vm.validar("   "))
        assertNotNull(vm.validar("x".repeat(LARGO_MAXIMO_MENSAJE + 1)))
        assertNull(vm.validar("Hola"))
    }

    @Test
    fun registrarHablado_guardaLoQueEscribioConLaRespuestaDeVoz() = runTest {
        assertTrue(crear().registrarHablado("  Necesito ayuda  ", enVoz = true))

        val captura = argumentCaptor<Registro>()
        verify(repo).crear(eq("u1"), captura.capture())
        assertEquals(TipoRegistro.HABLAR, captura.firstValue.tipo)
        assertEquals("Necesito ayuda", captura.firstValue.solicitud)
        assertEquals(RESPUESTA_EN_VOZ, captura.firstValue.respuesta)
    }

    @Test
    fun registrarHablado_sinVozInstalada_loDejaAnotado() = runTest {
        crear().registrarHablado("Hola", enVoz = false)

        val captura = argumentCaptor<Registro>()
        verify(repo).crear(eq("u1"), captura.capture())
        assertEquals(RESPUESTA_SIN_VOZ, captura.firstValue.respuesta)
    }

    @Test
    fun registrarHablado_textoVacio_noGuarda() = runTest {
        assertFalse(crear().registrarHablado("  ", enVoz = true))

        verify(repo, never()).crear(any(), any())
    }

    @Test
    fun registrarTranscripcion_usaLaPrimeraOpcionConTexto() = runTest {
        val texto = crear().registrarTranscripcion(listOf(" ", "  pase por caja  ", "pase por casa"))

        assertEquals("pase por caja", texto)
        val captura = argumentCaptor<Registro>()
        verify(repo).crear(eq("u1"), captura.capture())
        assertEquals(TipoRegistro.ESCRIBIR, captura.firstValue.tipo)
        assertEquals("pase por caja", captura.firstValue.respuesta)
    }

    @Test
    fun registrarTranscripcion_sinResultados_avisaYNoGuarda() = runTest {
        val vm = crear()

        assertNull(vm.registrarTranscripcion(null))
        assertTrue(vm.mensaje.value!!.esError)
        verify(repo, never()).crear(any(), any())
    }

    @Test
    fun historial_seFiltraPorTipoYCuentaCadaUno() = runTest {
        datos.value = listOf(
            registro("1", TipoRegistro.HABLAR),
            registro("2", TipoRegistro.ESCRIBIR),
            registro("3", TipoRegistro.HABLAR)
        )
        val vm = crear()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.historial.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.resumen.collect {} }

        assertEquals(3, vm.historial.first { it != null }!!.size)
        assertEquals(mapOf(TipoRegistro.HABLAR to 2, TipoRegistro.ESCRIBIR to 1), vm.resumen.value)

        vm.filtrar(TipoRegistro.ESCRIBIR)
        assertEquals(listOf("2"), vm.historial.value!!.map { it.id })

        vm.filtrar(null)
        assertEquals(3, vm.historial.value!!.size)
    }

    @Test
    fun eliminarYEliminarTodo_llamanAlRepositorio() = runTest {
        val vm = crear()
        vm.eliminar("7")
        vm.eliminarTodo()

        verify(repo).eliminar("u1", "7")
        verify(repo).eliminarTodos("u1")
        assertEquals("Historial borrado", vm.mensaje.value?.texto)
    }

    @Test
    fun siFirestoreFalla_seMuestraElErrorYSeLiberaElBoton() = runTest {
        whenever(repo.crear(any(), any())).doThrow(RuntimeException("sin servidor"))
        val vm = crear()

        vm.registrarHablado("Hola", enVoz = true)

        assertTrue(vm.mensaje.value!!.esError)
        assertFalse(vm.ocupado.value)
    }
}
