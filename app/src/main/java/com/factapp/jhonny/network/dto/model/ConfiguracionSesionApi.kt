package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Configuración de empresa y almacenes devuelta en login, refresh y /auth/me. */
data class ConfiguracionSesionApi(
    val empresa: Company? = null,
    val almacenes: List<Almacen>? = null,
    @SerializedName("actualizado_en")
    val actualizadoEn: String? = null,
)
