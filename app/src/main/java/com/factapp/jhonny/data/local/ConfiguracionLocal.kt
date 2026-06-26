package com.factapp.jhonny.data.local

import android.content.Context
import com.factapp.jhonny.TipoComprobante
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.ConfiguracionSesionApi
import com.factapp.jhonny.network.dto.model.SeriesDocConfig
import com.google.gson.Gson

/**
 * Caché local de configuración de empresa (series, almacenes, datos fiscales).
 * Se actualiza en login, refresh y al pulsar Actualizar en el dashboard.
 */
object ConfiguracionLocal {

    private const val PREFS = "empresa_configuracion"
    private const val KEY_JSON = "configuracion_json"
    private val gson = Gson()

    fun guardar(context: Context, configuracion: ConfiguracionSesionApi?) {
        if (configuracion == null) return
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, gson.toJson(configuracion))
            .apply()
    }

    fun obtener(context: Context): ConfiguracionSesionApi? {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_JSON, null)
            ?: return null
        return runCatching {
            gson.fromJson(raw, ConfiguracionSesionApi::class.java)
        }.getOrNull()
    }

    fun empresa(context: Context): Company? = obtener(context)?.empresa

    fun almacenes(context: Context): List<Almacen> = obtener(context)?.almacenes.orEmpty()

    fun seriesConfig(context: Context): Map<String, SeriesDocConfig> =
        empresa(context)?.seriesConfig.orEmpty()

    fun serieDocumento(context: Context, tipoDoc: String): String? =
        seriesConfig(context)[tipoDoc]?.serie?.takeIf { it.isNotBlank() }

    fun limpiar(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_JSON)
            .apply()
    }
}

fun TipoComprobante.tipoDocSunat(): String = when (this) {
    TipoComprobante.FACTURA -> "01"
    TipoComprobante.BOLETA -> "03"
    TipoComprobante.NOTA_CREDITO -> "07"
    TipoComprobante.NOTA_DEBITO -> "08"
    TipoComprobante.GUIA_EMISION -> "09"
    TipoComprobante.GUIA_TRANSPORTISTA -> "31"
}

fun TipoComprobante.serieConfigurada(context: Context): String =
    ConfiguracionLocal.serieDocumento(context, tipoDocSunat()) ?: serie
