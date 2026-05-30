package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.demo.CatalogoDemo

/**
 * Catálogo desde el servidor. Si no hay red o el API falla, usa [CatalogoDemo] (solo respaldo).
 */
object CatalogRepository {

    suspend fun listarPorEmpresa(
        companyRuc: String,
        token: String?,
    ): Result<List<CatalogItem>> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (token.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Sesión sin token"))
        }
        return try {
            val items = RetrofitClient.api
                .listarCatalogo(companyRuc, "Bearer $token")
                .filter { it.activo }
            Result.success(items)
        } catch (e: Exception) {
            Result.success(CatalogoDemo.items(companyRuc))
        }
    }

    /** Catálogo completo para gestión (incluye ítems inactivos). */
    suspend fun listarParaGestion(
        companyRuc: String,
        token: String?,
    ): Result<List<CatalogItem>> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (token.isNullOrBlank()) {
            return Result.success(CatalogoDemo.items(companyRuc))
        }
        return try {
            val items = RetrofitClient.api.listarCatalogo(companyRuc, "Bearer $token")
            Result.success(items)
        } catch (e: Exception) {
            Result.success(CatalogoDemo.items(companyRuc))
        }
    }

    /**
     * Catálogo con [stockActual] del almacén indicado (query `almacen_id` en el API).
     * Usar al armar salidas desde una bodega concreta.
     */
    suspend fun listarPorAlmacen(
        companyRuc: String,
        token: String?,
        almacenId: String,
    ): Result<List<CatalogItem>> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (almacenId.isBlank()) {
            return Result.failure(IllegalArgumentException("Almacén requerido"))
        }
        if (token.isNullOrBlank()) {
            return Result.success(CatalogoDemo.itemsPorAlmacen(companyRuc, almacenId))
        }
        return try {
            val items = RetrofitClient.api.listarCatalogo(
                ruc = companyRuc,
                authorization = "Bearer $token",
                almacenId = almacenId,
            )
            Result.success(items)
        } catch (e: Exception) {
            Result.success(CatalogoDemo.itemsPorAlmacen(companyRuc, almacenId))
        }
    }
}
