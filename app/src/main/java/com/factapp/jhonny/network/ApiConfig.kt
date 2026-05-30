package com.factapp.jhonny.network

/**
 * Paso 1: URL base del API.
 * Debe terminar en `/` para que las rutas del servicio (`@GET("users")`, etc.) se concatenen bien.
 */
object ApiConfig {
    const val BASE_URL: String = "https://api.example.com/"
}
