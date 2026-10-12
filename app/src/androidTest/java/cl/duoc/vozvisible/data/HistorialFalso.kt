package cl.duoc.vozvisible.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// historial en memoria para las pruebas de interfaz, sin Firestore
class HistorialFalso(iniciales: List<Registro> = emptyList()) : RegistroRepository {

    val registros = MutableStateFlow(iniciales)
    private var siguiente = iniciales.size

    override fun observar(uid: String): Flow<List<Registro>> = registros

    override suspend fun crear(uid: String, registro: Registro) {
        registros.update { listOf(registro.copy(id = "r${siguiente++}")) + it }
    }

    override suspend fun eliminar(uid: String, id: String) {
        registros.update { lista -> lista.filterNot { it.id == id } }
    }

    override suspend fun eliminarTodos(uid: String) {
        registros.value = emptyList()
    }
}
