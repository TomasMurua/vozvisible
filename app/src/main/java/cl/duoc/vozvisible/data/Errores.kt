package cl.duoc.vozvisible.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.TimeoutCancellationException

enum class MotivoError(val mensaje: String) {
    CREDENCIALES("Correo o contraseña incorrectos"),
    CORREO_EN_USO("Ese correo ya tiene una cuenta activa"),
    CLAVE_DEBIL("La contraseña es muy débil, usa al menos 8 caracteres con letras y números"),
    SIN_RED("No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo"),
    SESION_EXPIRADA("Por seguridad, vuelve a iniciar sesión para hacer este cambio"),
    DESCONOCIDO("No pudimos completar la operación, inténtalo de nuevo")
}

// error propio del proyecto: traduce las excepciones de Firebase a algo que la vista pueda mostrar
class AuthException(val motivo: MotivoError, causa: Throwable? = null) :
    Exception(motivo.mensaje, causa)

// cualquier excepción que llegue desde Firebase termina como AuthException con un motivo claro
fun Throwable.comoErrorDeApp(): AuthException = when (this) {
    is AuthException -> this
    is TimeoutCancellationException -> AuthException(MotivoError.SIN_RED, this)
    is FirebaseNetworkException -> AuthException(MotivoError.SIN_RED, this)
    is FirebaseFirestoreException ->
        if (code == FirebaseFirestoreException.Code.UNAVAILABLE) AuthException(MotivoError.SIN_RED, this)
        else AuthException(MotivoError.DESCONOCIDO, this)
    else -> AuthException(MotivoError.DESCONOCIDO, this)
}
