package cl.duoc.vozvisible

import android.app.Application
import cl.duoc.vozvisible.data.AuthRepository
import cl.duoc.vozvisible.data.FirebaseAuthRepository
import cl.duoc.vozvisible.data.FirestoreFraseRepository
import cl.duoc.vozvisible.data.FirestorePerfilRepository
import cl.duoc.vozvisible.data.FraseRepository
import cl.duoc.vozvisible.data.PerfilRepository
import cl.duoc.vozvisible.data.Preferencias
import cl.duoc.vozvisible.data.PreferenciasLocales

// junta las dependencias en un solo lugar. en las pruebas se reemplazan por versiones falsas
class Contenedor(
    val auth: AuthRepository,
    val perfiles: PerfilRepository,
    val frases: FraseRepository,
    val prefs: Preferencias
)

class VozVisibleApp : Application() {
    val contenedor: Contenedor by lazy {
        Contenedor(
            auth = FirebaseAuthRepository(),
            perfiles = FirestorePerfilRepository(),
            frases = FirestoreFraseRepository(),
            prefs = PreferenciasLocales(this)
        )
    }
}
