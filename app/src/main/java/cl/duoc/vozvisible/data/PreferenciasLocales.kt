package cl.duoc.vozvisible.data

import android.content.Context

// SharedPreferences: el correo recordado y los datos básicos de la sesión
class PreferenciasLocales(context: Context) : Preferencias {

    // el contexto de aplicacion, para no quedarse con una referencia a la Activity
    private val prefs = context.applicationContext
        .getSharedPreferences("vozvisible", Context.MODE_PRIVATE)

    override var correoRecordado: String?
        get() = prefs.getString(CLAVE_CORREO, null)
        set(valor) {
            val editor = prefs.edit()
            if (valor.isNullOrBlank()) editor.remove(CLAVE_CORREO)
            else editor.putString(CLAVE_CORREO, valor)
            editor.apply()
        }

    override val sesionActiva: Boolean
        get() = prefs.getString(CLAVE_UID, null) != null

    override val nombreSesion: String?
        get() = prefs.getString(CLAVE_NOMBRE, null)

    override fun guardarSesion(uid: String, nombre: String) {
        prefs.edit()
            .putString(CLAVE_UID, uid)
            .putString(CLAVE_NOMBRE, nombre)
            .apply()
    }

    // cierra la sesión pero deja el correo recordado, que es otra preferencia
    override fun limpiarSesion() {
        prefs.edit()
            .remove(CLAVE_UID)
            .remove(CLAVE_NOMBRE)
            .apply()
    }

    private companion object {
        const val CLAVE_CORREO = "correo_recordado"
        const val CLAVE_UID = "sesion_uid"
        const val CLAVE_NOMBRE = "sesion_nombre"
    }
}
