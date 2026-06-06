package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.request.ActualizarCatalogItemRequest
import com.factapp.jhonny.network.dto.request.CrearCatalogItemRequest
import com.factapp.jhonny.network.dto.request.PatchCatalogItemRequest

/**
 * Catálogo contra BackEndEasy (`catalogItemApiController` → `/api/empresas/{ruc}/catalogo`).
 * Usa el DTO [CatalogItem] tal como lo devuelve el API (snake_case vía Gson).
 */
object CatalogRepository {

    private fun authHeader(token: String?) = "Bearer $token"

    suspend fun listarPorEmpresa(
        companyRuc: String,
        token: String?,
    ): Result<List<CatalogItem>> =
        listarParaGestion(companyRuc, token).map { items -> items.filter { it.activo } }

    /** Catálogo completo para gestión (incluye ítems inactivos). */
    suspend fun listarParaGestion(
        companyRuc: String,
        token: String?,
    ): Result<List<CatalogItem>> = apiCall(companyRuc, token) {
        RetrofitClient.api.listarCatalogo(companyRuc, authHeader(token))
    }

    /**
     * Catálogo con [CatalogItem.stockActual] del almacén indicado (query `almacen_id` en el API).
     */
    suspend fun listarPorAlmacen(
        companyRuc: String,
        token: String?,
        almacenId: String,
    ): Result<List<CatalogItem>> {
        if (almacenId.isBlank()) {
            return Result.failure(IllegalArgumentException("Almacén requerido"))
        }
        return apiCall(companyRuc, token) {
            RetrofitClient.api.listarCatalogo(
                ruc = companyRuc,
                authorization = authHeader(token),
                almacenId = almacenId,
            )
        }
    }

    suspend fun crear(
        companyRuc: String,
        token: String?,
        body: CrearCatalogItemRequest,
    ): Result<CatalogItem> = apiCall(companyRuc, token) {
        RetrofitClient.api.crearCatalogItem(companyRuc, authHeader(token), body)
    }

    suspend fun actualizar(
        companyRuc: String,
        token: String?,
        id: String,
        body: ActualizarCatalogItemRequest,
    ): Result<CatalogItem> = apiCall(companyRuc, token) {
        RetrofitClient.api.actualizarCatalogItem(companyRuc, id, authHeader(token), body)
    }

    suspend fun cambiarActivo(
        companyRuc: String,
        token: String?,
        id: String,
        activo: Boolean,
    ): Result<CatalogItem> = apiCall(companyRuc, token) {
        RetrofitClient.api.patchCatalogItem(
            companyRuc,
            id,
            authHeader(token),
            PatchCatalogItemRequest(activo = activo),
        )
    }

    suspend fun eliminar(
        companyRuc: String,
        token: String?,
        id: String,
    ): Result<Unit> = apiCall(companyRuc, token) {
        RetrofitClient.api.eliminarCatalogItem(companyRuc, id, authHeader(token))
        Unit
    }

    private suspend fun <T> apiCall(
        companyRuc: String,
        token: String?,
        block: suspend () -> T,
    ): Result<T> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (token.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Inicia sesión para usar el catálogo"))
        }
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
