package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.TIPO_DOC_DNI
import com.factapp.jhonny.network.dto.model.dniValido
import com.factapp.jhonny.network.dto.request.CrearClienteRequest

object ClienteRepository {

    suspend fun listar(
        companyRuc: String,
        token: String?,
    ): Result<List<Cliente>> = apiCall(companyRuc, token, "consultar clientes") { authToken ->
        RetrofitClient.api.listarClientes(companyRuc, bearer(authToken))
    }

    suspend fun crear(
        companyRuc: String,
        token: String?,
        body: CrearClienteRequest,
    ): Result<Cliente> {
        if (body.tipoDoc != TIPO_DOC_DNI) {
            return Result.failure(
                IllegalArgumentException("Solo puedes registrar clientes con DNI desde la app"),
            )
        }
        if (!dniValido(body.numeroDoc)) {
            return Result.failure(IllegalArgumentException("El DNI debe tener 8 dígitos"))
        }
        if (body.razonSocial.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre es obligatorio"))
        }
        return apiCall(companyRuc, token, "crear clientes") { authToken ->
            RetrofitClient.api.crearCliente(companyRuc, bearer(authToken), body)
        }
    }

    private suspend fun <T> apiCall(
        companyRuc: String,
        token: String?,
        accion: String,
        block: suspend (String) -> T,
    ): Result<T> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (token.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Inicia sesión para $accion"))
        }
        return try {
            Result.success(block(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun bearer(token: String) = "Bearer $token"
}
