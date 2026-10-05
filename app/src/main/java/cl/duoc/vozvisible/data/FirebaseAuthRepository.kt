package cl.duoc.vozvisible.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val uidActual: String? get() = auth.currentUser?.uid
    override val correoActual: String? get() = auth.currentUser?.email

    override suspend fun registrar(correo: String, clave: String): String =
        traducir { auth.createUserWithEmailAndPassword(correo, clave).await().user!!.uid }

    override suspend fun ingresar(correo: String, clave: String): String =
        traducir { auth.signInWithEmailAndPassword(correo, clave).await().user!!.uid }

    override suspend fun enviarRecuperacion(correo: String) =
        traducir { auth.sendPasswordResetEmail(correo).await(); Unit }

    // borrar la cuenta o cambiar datos sensibles exige una sesión reciente
    override suspend fun reautenticar(clave: String) = traducir {
        val usuario = auth.currentUser ?: throw AuthException(MotivoError.SESION_EXPIRADA)
        val credencial = EmailAuthProvider.getCredential(usuario.email.orEmpty(), clave)
        usuario.reauthenticate(credencial).await()
        Unit
    }

    override suspend fun eliminarCuenta() = traducir {
        val usuario = auth.currentUser ?: throw AuthException(MotivoError.SESION_EXPIRADA)
        usuario.delete().await()
        Unit
    }

    override fun cerrarSesion() = auth.signOut()

    // convierte las excepciones de Firebase en la propia del proyecto
    private suspend fun <T> traducir(bloque: suspend () -> T): T = try {
        bloque()
    } catch (e: CancellationException) {
        throw e
    } catch (e: AuthException) {
        throw e
    } catch (e: FirebaseAuthUserCollisionException) {
        throw AuthException(MotivoError.CORREO_EN_USO, e)
    } catch (e: FirebaseAuthWeakPasswordException) {
        throw AuthException(MotivoError.CLAVE_DEBIL, e)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        throw AuthException(MotivoError.CREDENCIALES, e)
    } catch (e: FirebaseAuthInvalidUserException) {
        throw AuthException(MotivoError.CREDENCIALES, e)
    } catch (e: FirebaseAuthRecentLoginRequiredException) {
        throw AuthException(MotivoError.SESION_EXPIRADA, e)
    } catch (e: FirebaseNetworkException) {
        throw AuthException(MotivoError.SIN_RED, e)
    } catch (e: Exception) {
        throw AuthException(MotivoError.DESCONOCIDO, e)
    }
}
