package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.demo.ClientesDemo
import com.factapp.jhonny.network.dto.request.CrearClienteRequest
import com.factapp.jhonny.network.dto.model.TIPO_DOC_DNI
import com.factapp.jhonny.network.dto.model.dniValido

object ClienteRepository {

    suspend fun listar(
        companyRuc: String,
        token: String?,
    ): Result<List<Cliente>> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (!token.isNullOrBlank()) {
            runCatching {
                RetrofitClient.api.listarClientes(companyRuc, bearer(token))
            }.onSuccess { remoto ->
                if (remoto.isNotEmpty()) return Result.success(remoto)
            }
        }
        return Result.success(ClientesDemo.listar(companyRuc))
    }

    suspend fun crear(
        companyRuc: String,
        token: String?,
        body: CrearClienteRequest,
    ): Result<Cliente> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
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

        if (token.isNullOrBlank()) {
            return runCatching { ClientesDemo.crear(companyRuc, body) }
        }
        return try {
            Result.success(
                RetrofitClient.api.crearCliente(companyRuc, bearer(token), body),
            )
        } catch (_: Exception) {
            runCatching { ClientesDemo.crear(companyRuc, body) }
        }
    }

    private fun bearer(token: String) = "Bearer $token"
}
