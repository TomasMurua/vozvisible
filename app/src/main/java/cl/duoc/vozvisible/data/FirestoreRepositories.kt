package cl.duoc.vozvisible.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// estructura: usuarios/{uid}  y  usuarios/{uid}/frases/{id}
class FirestorePerfilRepository(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PerfilRepository {

    private fun documento(uid: String) = bd.collection("usuarios").document(uid)

    override suspend fun guardar(usuario: Usuario) {
        documento(usuario.uid).set(usuario.aMapa()).await()
    }

    override suspend fun obtener(uid: String): Usuario? {
        val instantanea = documento(uid).get().await()
        return instantanea.data?.let { mapaAUsuario(uid, it) }
    }

    override suspend fun eliminar(uid: String) {
        documento(uid).delete().await()
    }
}

class FirestoreFraseRepository(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FraseRepository {

    private fun coleccion(uid: String) =
        bd.collection("usuarios").document(uid).collection("frases")

    // escucha en tiempo real: cualquier cambio en Firestore vuelve a emitir la lista
    override fun observar(uid: String): Flow<List<Frase>> = callbackFlow {
        val registro = coleccion(uid)
            .orderBy("creada", Query.Direction.DESCENDING)
            .addSnapshotListener { instantanea, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = instantanea?.documents.orEmpty().mapNotNull { doc ->
                    doc.data?.let { mapaAFrase(doc.id, it) }
                }
                trySend(lista)
            }
        awaitClose { registro.remove() }
    }

    override suspend fun crear(uid: String, frase: Frase) {
        coleccion(uid).add(frase.aMapa()).await()
    }

    override suspend fun actualizar(uid: String, frase: Frase) {
        coleccion(uid).document(frase.id).update(frase.aMapa()).await()
    }

    override suspend fun eliminar(uid: String, id: String) {
        coleccion(uid).document(id).delete().await()
    }

    override suspend fun eliminarTodas(uid: String) {
        coleccion(uid).get().await().documents.forEach { it.reference.delete().await() }
    }
}
