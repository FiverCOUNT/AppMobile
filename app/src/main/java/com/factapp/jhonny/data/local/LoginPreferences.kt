package com.factapp.jhonny.data.local

import android.content.Context
import androidx.core.content.edit

/**
 * Preferencias de la pantalla de login: "Recordarme" y email recordado.
 * La sesión (tokens) vive en Room solo cuando [recordarSesion] es true.
 */
object LoginPreferences {

    private const val PREFS_NAME = "login_prefs"
    private const val KEY_RECORDAR = "recordar_sesion"
    private const val KEY_EMAIL = "email_recordado"

    fun recordarSesion(context: Context): Boolean =
        prefs(context).getBoolean(KEY_RECORDAR, true)

    fun emailRecordado(context: Context): String? =
        prefs(context).getString(KEY_EMAIL, null)?.takeIf { it.isNotBlank() }

    fun guardar(context: Context, recordar: Boolean, email: String) {
        prefs(context).edit {
            putBoolean(KEY_RECORDAR, recordar)
            if (recordar) {
                putString(KEY_EMAIL, email.trim().lowercase())
            } else {
                remove(KEY_EMAIL)
            }
        }
    }

    suspend fun puedeUsarBiometria(context: Context): Boolean {
        if (!recordarSesion(context)) return false
        val sesion = SesionStore.obtenerSesionReciente(context)
        return !sesion?.refreshToken.isNullOrBlank()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
