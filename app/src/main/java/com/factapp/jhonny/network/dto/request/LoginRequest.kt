package com.factapp.jhonny.network.dto.request

/**
 * Cuerpo JSON del login (email + PIN de 6 digitos).
 */
data class LoginRequest(
    val email: String,
    val pin: String,
)
