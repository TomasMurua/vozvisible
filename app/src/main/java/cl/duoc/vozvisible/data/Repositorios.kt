package cl.duoc.vozvisible.data

import kotlinx.coroutines.flow.Flow

// las interfaces separan el "qué" del "cómo": los view models y las pruebas
// solo conocen esto, Firebase queda en una sola capa

interface AuthRepository {
    val uidActual: String?
    val correoActual: String?

    suspend fun registrar(correo: String, clave: String): String
    suspend fun ingresar(correo: String, clave: String): String
    suspend fun enviarRecuperacion(correo: String)
    suspend fun reautenticar(clave: String)
    suspend fun eliminarCuenta()
    fun cerrarSesion()
}

interface PerfilRepository {
    suspend fun guardar(usuario: Usuario)
    suspend fun obtener(uid: String): Usuario?
    suspend fun eliminar(uid: String)
}

interface FraseRepository {
    fun observar(uid: String): Flow<List<Frase>>
    suspend fun crear(uid: String, frase: Frase)
    suspend fun actualizar(uid: String, frase: Frase)
    suspend fun eliminar(uid: String, id: String)
    suspend fun eliminarTodas(uid: String)
}

// lo que se guarda en el dispositivo con SharedPreferences
interface Preferencias {
    var correoRecordado: String?
    val sesionActiva: Boolean
    val nombreSesion: String?
    fun guardarSesion(uid: String, nombre: String)
    fun limpiarSesion()
}
