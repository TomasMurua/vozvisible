package cl.duoc.vozvisible.data

import android.content.Context

// guarda el correo en el dispositivo para no tener que escribirlo cada vez
class PreferenciasLocales(context: Context) {

    // el contexto de aplicacion, para no quedarse con una referencia a la Activity
    private val prefs = context.applicationContext
        .getSharedPreferences("vozvisible", Context.MODE_PRIVATE)

    var correoRecordado: String?
        get() = prefs.getString(CLAVE_CORREO, null)
        set(valor) {
            val editor = prefs.edit()
            if (valor.isNullOrBlank()) editor.remove(CLAVE_CORREO)
            else editor.putString(CLAVE_CORREO, valor)
            editor.apply()
        }

    private companion object {
        const val CLAVE_CORREO = "correo_recordado"
    }
}
