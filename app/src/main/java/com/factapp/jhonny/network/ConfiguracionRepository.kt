package com.factapp.jhonny.network

import android.content.Context
import com.factapp.jhonny.data.local.ConfiguracionAppLocal
import com.factapp.jhonny.network.dto.ApiEnvelope
import com.factapp.jhonny.network.dto.model.ConfiguracionAppApi

object ConfiguracionRepository {

    suspend fun obtener(context: Context, forzarRed: Boolean = false): Result<ConfiguracionAppApi> {
        if (!forzarRed) {
            ConfiguracionAppLocal.obtener(context)?.let { return Result.success(it) }
        }
        return try {
            val envelope = RetrofitClient.api.obtenerConfiguracionApp()
            val data = envelope.requireData("configuración de la app")
            ConfiguracionAppLocal.guardar(context, data)
            Result.success(data)
        } catch (e: Exception) {
            ConfiguracionAppLocal.obtener(context)?.let { return Result.success(it) }
            Result.failure(e)
        }
    }

    private fun <T> ApiEnvelope<T>.requireData(contexto: String): T {
        if (!success || data == null) {
            throw IllegalStateException(message?.takeIf { it.isNotBlank() } ?: "Sin datos de $contexto")
        }
        return data
    }
}
