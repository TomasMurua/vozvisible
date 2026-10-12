package cl.duoc.vozvisible.data

import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// Mockito + JUnit: el view model se prueba con repositorios simulados, sin Firebase
@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    private val auth: AuthRepository = mock()
    private val perfiles: PerfilRepository = mock()
    private val frases: FraseRepository = mock()
    private val historial: RegistroRepository = mock()
    private val prefs: Preferencias = mock()

    private val perfilAna = Usuario(
        uid = "uid-ana", nombre = "Ana", correo = "ana@duocuc.cl",
        region = "Biobío", perfil = PerfilUsuario.PERSONA_SORDA, preferencias = emptyList()
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun crear() = SesionViewModel(auth, perfiles, frases, historial, prefs)

    @Test
    fun ingresar_conCredencialesCorrectas_guardaLaSesionYAvisa() = runTest {
        whenever(auth.ingresar("ana@duocuc.cl", "Clave123")).thenReturn("uid-ana")
        whenever(perfiles.obtener("uid-ana")).thenReturn(perfilAna)
        var avisado = false

        crear().ingresar(" ana@duocuc.cl ", "Clave123", recordar = true) { avisado = true }

        assertTrue(avisado)
        verify(prefs).guardarSesion("uid-ana", "Ana")
        verify(prefs).correoRecordado = "ana@duocuc.cl"
    }

    @Test
    fun ingresar_sinRecordar_borraElCorreoGuardado() = runTest {
        whenever(auth.ingresar(any(), any())).thenReturn("uid-ana")
        whenever(perfiles.obtener(any())).thenReturn(perfilAna)

        crear().ingresar("ana@duocuc.cl", "Clave123", recordar = false) {}

        verify(prefs).correoRecordado = null
    }

    @Test
    fun ingresar_conCredencialesMalas_muestraErrorYNoGuardaSesion() = runTest {
        whenever(auth.ingresar(any(), any())).doThrow(AuthException(MotivoError.CREDENCIALES))
        var avisado = false

        val vm = crear()
        vm.ingresar("ana@duocuc.cl", "mala", recordar = false) { avisado = true }

        assertFalse(avisado)
        assertEquals(MotivoError.CREDENCIALES.mensaje, vm.estado.value.mensaje?.texto)
        assertTrue(vm.estado.value.mensaje!!.esError)
        assertFalse(vm.estado.value.cargando)
        verify(prefs, never()).guardarSesion(any(), any())
    }

    @Test
    fun registrar_creaLaCuentaYGuardaElPerfilConElUid() = runTest {
        whenever(auth.registrar("ana@duocuc.cl", "Clave123")).thenReturn("uid-ana")

        val vm = crear()
        vm.registrar(perfilAna.copy(uid = ""), "Clave123") {}

        verify(perfiles).guardar(perfilAna)
        verify(prefs).guardarSesion("uid-ana", "Ana")
        assertEquals(perfilAna, vm.estado.value.usuario)
    }

    @Test
    fun registrar_siFallaElPerfil_deshaceLaCuentaCreada() = runTest {
        whenever(auth.registrar(any(), any())).thenReturn("uid-ana")
        whenever(perfiles.guardar(any())).doThrow(RuntimeException("sin permisos"))
        var avisado = false

        val vm = crear()
        vm.registrar(perfilAna, "Clave123") { avisado = true }

        assertFalse(avisado)
        verify(auth).eliminarCuenta()
        verify(prefs, never()).guardarSesion(any(), any())
        assertTrue(vm.estado.value.mensaje!!.esError)
    }

    @Test
    fun registrar_conCorreoYaUsado_muestraElMensajeDelMotivo() = runTest {
        whenever(auth.registrar(any(), any())).doThrow(AuthException(MotivoError.CORREO_EN_USO))

        val vm = crear()
        vm.registrar(perfilAna, "Clave123") {}

        assertEquals(MotivoError.CORREO_EN_USO.mensaje, vm.estado.value.mensaje?.texto)
    }

    @Test
    fun cerrarSesion_limpiaPreferenciasYEstado() = runTest {
        whenever(auth.ingresar(any(), any())).thenReturn("uid-ana")
        whenever(perfiles.obtener(any())).thenReturn(perfilAna)
        val vm = crear()
        vm.ingresar("ana@duocuc.cl", "Clave123", true) {}

        vm.cerrarSesion()

        verify(auth).cerrarSesion()
        verify(prefs).limpiarSesion()
        assertNull(vm.estado.value.usuario)
    }

    @Test
    fun haySesion_necesitaFirebaseYPreferenciasDeAcuerdo() {
        whenever(auth.uidActual).thenReturn("uid-ana")
        whenever(prefs.sesionActiva).thenReturn(false)
        assertFalse(crear().haySesion)
    }

    @Test
    fun eliminarCuenta_borraDatosAntesQueLaCuenta() = runTest {
        whenever(auth.uidActual).thenReturn("uid-ana")
        var avisado = false

        crear().eliminarCuenta("Clave123") { avisado = true }

        val orden = inOrder(auth, frases, historial, perfiles)
        orden.verify(auth).reautenticar("Clave123")
        orden.verify(frases).eliminarTodas("uid-ana")
        orden.verify(historial).eliminarTodos("uid-ana")
        orden.verify(perfiles).eliminar("uid-ana")
        orden.verify(auth).eliminarCuenta()
        assertTrue(avisado)
        verify(prefs).limpiarSesion()
    }

    @Test
    fun eliminarCuenta_conClaveIncorrecta_noBorraNada() = runTest {
        whenever(auth.reautenticar(any())).doThrow(AuthException(MotivoError.CREDENCIALES))

        crear().eliminarCuenta("mala") {}

        verify(frases, never()).eliminarTodas(any())
        verify(historial, never()).eliminarTodos(any())
        verify(perfiles, never()).eliminar(any())
        verify(auth, never()).eliminarCuenta()
    }

    @Test
    fun recuperar_avisaSinConfirmarSiElCorreoExiste() = runTest {
        val vm = crear()
        vm.recuperar("ana@duocuc.cl")

        verify(auth).enviarRecuperacion(eq("ana@duocuc.cl"))
        assertFalse(vm.estado.value.mensaje!!.esError)
    }
}
