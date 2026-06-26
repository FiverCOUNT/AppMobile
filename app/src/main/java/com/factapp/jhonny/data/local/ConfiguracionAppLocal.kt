package com.factapp.jhonny.data.local

import android.content.Context
import com.factapp.jhonny.network.dto.model.ConfiguracionAppApi
import com.google.gson.Gson

/** Caché local de `GET /api/configuracion` (soporte y actualizaciones). */
object ConfiguracionAppLocal {

    private const val PREFS = "app_configuracion_global"
    private const val KEY_JSON = "config_json"
    private val gson = Gson()

    fun guardar(context: Context, config: ConfiguracionAppApi) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, gson.toJson(config))
            .apply()
    }

    fun obtener(context: Context): ConfiguracionAppApi? {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_JSON, null)
            ?: return null
        return runCatching {
            gson.fromJson(raw, ConfiguracionAppApi::class.java)
        }.getOrNull()
    }

    fun limpiar(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_JSON)
            .apply()
    }
}
