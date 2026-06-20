package com.factapp.jhonny.network

import android.content.Context
import com.factapp.jhonny.data.local.ConfiguracionLocal
import com.factapp.jhonny.data.local.LoginPreferences
import com.factapp.jhonny.data.local.SesionStore
import com.factapp.jhonny.data.local.toUsuarioEntity
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.dto.ApiEnvelope
import com.factapp.jhonny.network.dto.request.LoginApiRequest
import com.factapp.jhonny.network.dto.request.RefreshTokenRequest
import com.google.gson.JsonSyntaxException
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
    ): Result<Usuario> = try {
        val envelope = RetrofitClient.api.login(
            LoginApiRequest(
                email = email.trim().lowercase(),
                contrasena = contrasena,
            ),
        )
        val session = envelope.requireData("Inicio de sesión")
        aplicarSesion(context, session, recordarSesion, email)
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: JsonSyntaxException) {
        Result.failure(
            IllegalStateException("Respuesta del servidor inválida. Reinicia el backend e intenta de nuevo.", e),
        )
    } catch (e: StackOverflowError) {
        Result.failure(
            IllegalStateException("Error al leer datos de la empresa en la respuesta de login.", e),
        )
    } catch (e: Throwable) {
        Result.failure(
            e as? Exception ?: Exception(e.message ?: "Error inesperado al iniciar sesión", e),
        )
    }

    /**
     * Renueva access/refresh con el token guardado (p. ej. tras huella dactilar).
     * Solo persiste si "Recordarme" está activo.
     */
    suspend fun refreshSesion(context: Context): Result<Usuario> {
        val actual = SesionStore.obtenerSesionReciente(context)
            ?: return Result.failure(
                IllegalStateException("No hay sesión guardada. Inicia sesión con email y PIN."),
            )
        val refresh = actual.refreshToken?.takeIf { it.isNotBlank() }
            ?: return Result.failure(
                IllegalStateException("La sesión no tiene refresh token. Vuelve a iniciar sesión."),
            )
        return try {
            val envelope = RetrofitClient.api.refresh(RefreshTokenRequest(refresh))
            aplicarSesion(context, envelope.requireData("Renovación de sesión"), LoginPreferences.recordarSesion(context), actual.email)
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 403) {
                limpiarSesionLocal(context)
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recarga perfil, empresa, series y almacenes desde el servidor (p. ej. botón Actualizar del dashboard).
     */
    suspend fun sincronizarSesion(
        context: Context,
        sesionActual: Usuario? = null,
    ): Result<Usuario> {
        val actual = sesionActual ?: SesionStore.obtenerSesionReciente(context)
            ?: return Result.failure(
                IllegalStateException("No hay sesión activa. Inicia sesión de nuevo."),
            )
        val token = actual.token?.takeIf { it.isNotBlank() }
            ?: return Result.failure(
                IllegalStateException("La sesión no tiene token válido."),
            )
        return try {
            val envelope = RetrofitClient.api.me("Bearer $token")
            aplicarSesion(
                context,
                envelope.requireData("Sincronización"),
                LoginPreferences.recordarSesion(context),
                actual.email,
            )
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 403) {
                limpiarSesionLocal(context)
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun limpiarSesionLocal(context: Context) {
        ConfiguracionLocal.limpiar(context)
        SesionStore.eliminar(context)
    }

    /** Cierra sesión: borra tokens en Room y desactiva "Recordarme" / huella. */
    suspend fun cerrarSesion(context: Context) {
        limpiarSesionLocal(context)
        LoginPreferences.guardar(context, recordar = false, email = "")
    }

    suspend fun restaurarSesionLocal(context: Context): Usuario? {
        if (!LoginPreferences.recordarSesion(context)) return null
        return SesionStore.obtenerSesionReciente(context)
    }

    suspend fun puedeUsarBiometria(context: Context): Boolean =
        LoginPreferences.puedeUsarBiometria(context)

    private suspend fun aplicarSesion(
        context: Context,
        session: com.factapp.jhonny.network.dto.model.UsuarioSesionApi,
        persistir: Boolean,
        email: String,
    ): Result<Usuario> {
        ConfiguracionLocal.guardar(context, session.configuracion)
        val usuario = session.toUsuarioEntity().requireTokenValido()
        if (!persistir) {
            return Result.success(usuario)
        }
        return persistirSesion(context, usuario, true, email).fold(
            onSuccess = { Result.success(usuario) },
            onFailure = { error ->
                Result.failure(
                    IllegalStateException(
                        "Datos recibidos pero no se pudo guardar la sesión local. ${error.message ?: ""}".trim(),
                        error,
                    ),
                )
            },
        )
    }

    private suspend fun persistirSesion(
        context: Context,
        usuario: Usuario,
        recordarSesion: Boolean,
        email: String,
    ): Result<Unit> = runCatching {
        LoginPreferences.guardar(context, recordarSesion, email)
        if (!recordarSesion) {
            SesionStore.eliminar(context)
            return@runCatching
        }
        if (usuario.company?.ruc.isNullOrBlank()) {
            SesionStore.guardarSinEmpresa(context, usuario.copy(company = null))
        } else {
            SesionStore.guardar(context, usuario)
        }
    }

    private fun <T> ApiEnvelope<T>.requireData(accion: String): T {
        if (!success) {
            throw IllegalStateException(message ?: "$accion rechazado por el servidor")
        }
        return data ?: throw IllegalStateException("$accion sin datos en la respuesta")
    }

    private fun Usuario.requireTokenValido(): Usuario {
        if (token.isNullOrBlank()) {
            throw IllegalStateException("El servidor no devolvió token de sesión")
        }
        return this
    }
}

fun Throwable.mensajeAuth(): String = runCatching { mensajeAuthInterno() }
    .getOrElse { "No se pudo completar la operación. Revisa la red y que BackEndEasy esté en marcha." }

private fun Throwable.mensajeAuthInterno(): String = when (this) {
    is HttpException -> {
        val code = code()
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
        if (!body.isNullOrBlank()) {
            runCatching {
                Gson().fromJson(body, com.factapp.jhonny.network.dto.ApiErrorBody::class.java)
            }.getOrNull()?.message?.takeIf { it.isNotBlank() } ?: body
        } else {
            message()?.takeIf { it.isNotBlank() }
                ?: "Error del servidor ($code)"
        }
    }
    is IllegalStateException -> message?.takeIf { it.isNotBlank() } ?: "Operación no completada"
    else -> message?.takeIf { it.isNotBlank() }
        ?: "No se pudo conectar con el servidor. Revisa la red y la URL del API."
}
