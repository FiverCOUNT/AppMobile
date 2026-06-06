package com.factapp.jhonny.network.dto

import com.google.gson.annotations.SerializedName

/** Respuesta estándar del API BackEndEasy: `{ success, message?, data? }`. */
data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
)

data class ApiErrorBody(
    val success: Boolean = false,
    val message: String? = null,
)
