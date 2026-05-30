package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.modelos.EstadoUsuario
import com.google.gson.annotations.SerializedName

/**
 * Modelo de usuario para respuestas/peticiones JSON con Retrofit + Gson.
 * Misma información que [com.factapp.jhonny.modelos.Usuario], con tipos típicos de API.
 *
 * - [lastUpdated]: epoch en milisegundos (ajusta [SerializedName] si tu API envía ISO como string).
 */
data class Usuario(
    val email: String,
    @SerializedName("contrasena")
    val contrasena: String,
    val token: String?,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    @SerializedName("last_updated")
    val lastUpdated: Long,
    val estado: EstadoUsuario,
    @SerializedName("company")
    val company: Company? = null,
)
