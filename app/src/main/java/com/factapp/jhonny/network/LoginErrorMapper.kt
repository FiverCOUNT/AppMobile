package com.factapp.jhonny.network

import retrofit2.HttpException

fun Throwable.mensajeLogin(): String = when (this) {
    is HttpException -> {
        val cuerpo = runCatching { response()?.errorBody()?.string() }.getOrNull()
        if (!cuerpo.isNullOrBlank()) cuerpo else "Error del servidor: ${code()}"
    }
    else -> message ?: "No se pudo conectar. Revisa la URL y la red."
}
