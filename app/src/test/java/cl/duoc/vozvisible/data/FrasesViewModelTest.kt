package cl.duoc.vozvisible.data

import cl.duoc.vozvisible.ui.viewmodel.FrasesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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

// pruebas del CRUD de frases: crear, consultar, modificar y eliminar
@OptIn(ExperimentalCoroutinesApi::class)
class FrasesViewModelTest {

    private val repo: FraseRepository = mock()

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(repo.observar("u1")).thenReturn(flowOf(emptyList()))
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun crear() = FrasesViewModel(repo, "u1")

    @Test
    fun validar_rechazaFrasesMuyCortasOMuyLargas() {
        val vm = crear()
        assertNotNull(vm.validar(""))
        assertNotNull(vm.validar(" a "))
        assertNotNull(vm.validar("x".repeat(LARGO_MAXIMO_FRASE + 1)))
        assertNull(vm.validar("Hola, necesito ayuda"))
    }

    @Test
    fun guardar_sinId_creaLaFraseConElTextoLimpio() = runTest {
        var cerrado = false
        crear().guardar(null, "  Una receta, por favor  ", "Salud") { cerrado = true }

        val captura = argumentCaptor<Frase>()
        verify(repo).crear(eq("u1"), captura.capture())
        assertEquals("Una receta, por favor", captura.firstValue.texto)
        assertEquals("Salud", captura.firstValue.categoria)
        assertTrue(cerrado)
    }

    @Test
    fun guardar_conId_modificaLaFraseExistente() = runTest {
        crear().guardar("f1", "Gracias", "Saludo") {}

        val captura = argumentCaptor<Frase>()
        verify(repo).actualizar(eq("u1"), captura.capture())
        assertEquals("f1", captura.firstValue.id)
        assertEquals("Gracias", captura.firstValue.texto)
        verify(repo, never()).crear(any(), any())
    }

    @Test
    fun guardar_conTextoInvalido_noLlamaAlRepositorio() = runTest {
        crear().guardar(null, "a", "Saludo") {}

        verify(repo, never()).crear(any(), any())
    }

    @Test
    fun guardar_conCategoriaDesconocida_usaOtra() = runTest {
        crear().guardar(null, "Hola", "Inventada") {}

        val captura = argumentCaptor<Frase>()
        verify(repo).crear(eq("u1"), captura.capture())
        assertEquals("Otra", captura.firstValue.categoria)
    }

    @Test
    fun eliminar_llamaAlRepositorioYAvisa() = runTest {
        val vm = crear()
        vm.eliminar("f1")

        verify(repo).eliminar("u1", "f1")
        assertEquals("Frase eliminada", vm.mensaje.value?.texto)
    }

    @Test
    fun guardar_siElServidorFalla_muestraUnErrorYNoCierraElDialogo() = runTest {
        whenever(repo.crear(any(), any())).doThrow(RuntimeException("boom"))
        var cerrado = false

        val vm = crear()
        vm.guardar(null, "Hola", "Saludo") { cerrado = true }

        assertTrue(vm.mensaje.value!!.esError)
        assertFalse(cerrado)
    }
}
