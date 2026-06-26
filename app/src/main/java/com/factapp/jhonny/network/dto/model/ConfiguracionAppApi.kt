package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Configuración global de la app (`GET /api/configuracion`). */
data class ConfiguracionAppApi(
    val soporte: SoporteAppApi? = null,
    val actualizaciones: ActualizacionesAppApi? = null,
    @SerializedName("actualizado_en")
    val actualizadoEn: String? = null,
)

data class SoporteAppApi(
    val telefonos: List<String>? = null,
    val whatsapp: String? = null,
    val email: String? = null,
    val horario: String? = null,
    val desarrollador: String? = null,
)

data class ActualizacionesAppApi(
    val url: String? = null,
    @SerializedName("url_apk")
    val urlApk: String? = null,
    @SerializedName("version_actual")
    val versionActual: String? = null,
    @SerializedName("version_minima")
    val versionMinima: String? = null,
)

fun SoporteAppApi.tieneDatos(): Boolean =
    !telefonos.isNullOrEmpty() ||
        !whatsapp.isNullOrBlank() ||
        !email.isNullOrBlank() ||
        !horario.isNullOrBlank() ||
        !desarrollador.isNullOrBlank()

fun ActualizacionesAppApi.urlEfectiva(): String? =
    url?.takeIf { it.isNotBlank() } ?: urlApk?.takeIf { it.isNotBlank() }
