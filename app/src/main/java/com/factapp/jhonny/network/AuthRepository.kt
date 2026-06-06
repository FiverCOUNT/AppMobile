package com.factapp.jhonny.network

import android.content.Context
import com.factapp.jhonny.data.local.LoginPreferences
import com.factapp.jhonny.data.local.SesionStore
import com.factapp.jhonny.data.local.toUsuarioEntity
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.dto.ApiEnvelope
import com.factapp.jhonny.network.dto.request.LoginApiRequest
import com.factapp.jhonny.network.dto.request.RefreshTokenRequest
import com.google.gson.Gson
import retrofit2.HttpException

/**
 * Autenticación contra BackEndEasy (`/api/auth/login`, `/api/auth/refresh`)
 * y persistencia de sesión en Room vía [SesionStore] cuando el usuario marca "Recordarme".
 */
object AuthRepository {

    private val gson = Gson()

    suspend fun login(
        context: Context,
        email: String,
        contrasena: String,
        recordarSesion: Boolean,
    ): Result<Usuario> = runCatching {
        val envelope = RetrofitClient.api.login(
            LoginApiRequest(
                email = email.trim().lowercase(),
                contrasena = contrasena,
            ),
        )
        val session = envelope.requireData("Inicio de sesión")
        val usuario = session.toUsuarioEntity()
        persistirSesion(context, usuario, recordarSesion, email)
        usuario
    }

    /**
     * Renueva access/refresh con el token guardado (p. ej. tras huella dactilar).
     * Solo persiste si "Recordarme" está activo.
     */
    suspend fun refreshSesion(context: Context): Result<Usuario> = runCatching {
        val actual = SesionStore.obtenerSesionReciente(context)
            ?: error("No hay sesión guardada. Inicia sesión con email y PIN.")
        val refresh = actual.refreshToken?.takeIf { it.isNotBlank() }
            ?: error("La sesión no tiene refresh token. Vuelve a iniciar sesión.")
        val envelope = RetrofitClient.api.refresh(RefreshTokenRequest(refresh))
        val session = envelope.requireData("Renovación de sesión")
        val usuario = session.toUsuarioEntity()
        if (LoginPreferences.recordarSesion(context)) {
            SesionStore.guardar(context, usuario)
        }
        usuario
    }

    suspend fun restaurarSesionLocal(context: Context): Usuario? {
        if (!LoginPreferences.recordarSesion(context)) return null
        return SesionStore.obtenerSesionReciente(context)
    }

    suspend fun puedeUsarBiometria(context: Context): Boolean =
        LoginPreferences.puedeUsarBiometria(context)

    private suspend fun persistirSesion(
        context: Context,
        usuario: Usuario,
        recordarSesion: Boolean,
        email: String,
    ) {
        LoginPreferences.guardar(context, recordarSesion, email)
        if (recordarSesion) {
            SesionStore.guardar(context, usuario)
        } else {
            SesionStore.eliminar(context)
        }
    }

    private fun <T> ApiEnvelope<T>.requireData(accion: String): T {
        if (!success) {
            throw IllegalStateException(message ?: "$accion rechazado por el servidor")
        }
        return data ?: throw IllegalStateException("$accion sin datos en la respuesta")
    }
}

fun Throwable.mensajeAuth(): String = when (this) {
    is HttpException -> {
        val body = response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            runCatching {
                Gson().fromJson(body, com.factapp.jhonny.network.dto.ApiErrorBody::class.java)
            }.getOrNull()?.message?.takeIf { it.isNotBlank() } ?: body
        } else {
            "Error del servidor (${code()})"
        }
    }
    is IllegalStateException -> message ?: "Operación no completada"
    else -> message ?: "No se pudo conectar con el servidor. Revisa la red y la URL del API."
}
