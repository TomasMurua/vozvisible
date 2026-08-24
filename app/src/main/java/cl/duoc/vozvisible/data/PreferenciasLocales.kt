package cl.duoc.vozvisible.data

import android.content.Context

// guarda el correo en el dispositivo para no tener que escribirlo cada vez
class PreferenciasLocales(context: Context) {

    private val prefs = context.getSharedPreferences("vozvisible", Context.MODE_PRIVATE)

    var correoRecordado: String?
        get() = prefs.getString(CLAVE_CORREO, null)
        set(valor) {
            prefs.edit().apply {
                if (valor.isNullOrBlank()) remove(CLAVE_CORREO) else putString(CLAVE_CORREO, valor)
                apply()
            }
        }

    private companion object {
        const val CLAVE_CORREO = "correo_recordado"
    }
}
