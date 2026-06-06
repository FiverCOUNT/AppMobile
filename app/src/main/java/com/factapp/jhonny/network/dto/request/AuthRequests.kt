package com.factapp.jhonny.network.dto.request

import com.google.gson.annotations.SerializedName

/** Body `POST /api/auth/login` (BackEndEasy usa `contrasena`, no `pin`). */
data class LoginApiRequest(
    val email: String,
    val contrasena: String,
)

/** Body `POST /api/auth/refresh`. */
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String,
)
