package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.modelos.EstadoUsuario
import com.factapp.jhonny.modelos.RolUsuario
import com.factapp.jhonny.network.gson.EstadoUsuarioTypeAdapter
import com.factapp.jhonny.network.gson.LongFromJsonAdapter
import com.factapp.jhonny.network.gson.RolUsuarioTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

/**
 * Modelo de usuario para respuestas/peticiones JSON con Retrofit + Gson.
 * Misma información que [com.factapp.jhonny.modelos.Usuario], con tipos típicos de API.
 *
 * En login/refresh el backend envía el usuario en `data.user` y los tokens en el mismo `data`;
 * ver [UsuarioSesionApi] y [com.factapp.jhonny.data.local.toUsuarioEntity].
 */
data class Usuario(
    val id: Int? = null,
    val email: String = "",
    @SerializedName("contrasena")
    val contrasena: String = "",
    @SerializedName("accessToken", alternate = ["token"])
    val token: String? = null,
    @SerializedName("refreshToken", alternate = ["refresh_token"])
    val refreshToken: String? = null,
    @SerializedName("lastUpdated", alternate = ["last_updated"])
    @JsonAdapter(LongFromJsonAdapter::class)
    val lastUpdated: Long = 0L,
    @JsonAdapter(EstadoUsuarioTypeAdapter::class)
    val estado: EstadoUsuario = EstadoUsuario.ACTIVO,
    @JsonAdapter(RolUsuarioTypeAdapter::class)
    val rol: RolUsuario = RolUsuario.USUARIO,
    @SerializedName("almacenId", alternate = ["almacen_id"])
    val almacenId: String? = null,
    @SerializedName("almacenNombre", alternate = ["almacen_nombre"])
    val almacenNombre: String? = null,
    @SerializedName("company")
    val company: Company? = null,
    @SerializedName("companyId")
    val companyId: String? = null,
    @SerializedName("companyRuc")
    val companyRuc: String? = null,
    @SerializedName("companyNombre")
    val companyNombre: String? = null,
)

/** Bloque `data` devuelto por `POST /api/auth/login` y `POST /api/auth/refresh`. */
data class UsuarioSesionApi(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("tokenType")
    val tokenType: String = "Bearer",
    val user: Usuario,
)
