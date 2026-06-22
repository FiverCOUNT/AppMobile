package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.ordenadosPorFechaReciente
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.InventarioSaldo
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.MovimientoTipo
import com.factapp.jhonny.network.dto.model.sanitizarDesdeApi
import com.factapp.jhonny.network.dto.model.ProductoDevolucionCliente
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.delAlmacen
import com.factapp.jhonny.network.dto.model.soloDisponiblesParaEmision
import com.factapp.jhonny.network.dto.model.UbicacionProducto
import com.factapp.jhonny.network.dto.request.CrearAlmacenRequest
import com.factapp.jhonny.network.dto.request.RegistrarEntradaRequest
import com.factapp.jhonny.network.dto.request.RegistrarSalidaRequest

/** Almacén, series y movimientos contra BackEndEasy (solo API real). */
object InventarioRepository {

    suspend fun listarInventario(
        companyRuc: String,
        token: String?,
        almacenId: String? = null,
        catalogItemId: String? = null,
        soloConStock: Boolean = true,
    ): Result<List<InventarioSaldo>> = apiCall(companyRuc, token, "consultar inventario") { authToken ->
        RetrofitClient.api.listarInventario(
            ruc = companyRuc,
            authorization = bearer(authToken),
            almacenId = almacenId?.takeIf { it.isNotBlank() },
            catalogItemId = catalogItemId?.takeIf { it.isNotBlank() },
            soloConStock = soloConStock,
        )
    }

    suspend fun listarAlmacenes(
        companyRuc: String,
        token: String?,
        soloActivos: Boolean = true,
        todos: Boolean = false,
    ): Result<List<Almacen>> = apiCall(companyRuc, token, "consultar almacenes") { authToken ->
        RetrofitClient.api.listarAlmacenes(
            ruc = companyRuc,
            authorization = bearer(authToken),
            soloActivos = soloActivos,
            todos = if (todos) true else null,
        ).filter { it.activo || !soloActivos }
    }

    suspend fun crearAlmacen(
        companyRuc: String,
        token: String?,
        body: CrearAlmacenRequest,
    ): Result<Almacen> {
        if (body.codigo.isBlank() || body.nombre.isBlank()) {
            return Result.failure(IllegalArgumentException("Código y nombre son obligatorios"))
        }
        return apiCall(companyRuc, token, "crear almacenes") { authToken ->
            RetrofitClient.api.crearAlmacen(companyRuc, bearer(authToken), body)
        }
    }

    suspend fun listarSeriesDisponibles(
        companyRuc: String,
        catalogItemId: String,
        token: String?,
        almacenId: String? = null,
    ): Result<List<ProductoSerie>> = apiCall(companyRuc, token, "consultar series") { authToken ->
        RetrofitClient.api.listarSeriesDisponibles(
            ruc = companyRuc,
            catalogItemId = catalogItemId,
            authorization = bearer(authToken),
            almacenId = almacenId?.takeIf { it.isNotBlank() },
        ).filter { it.id.isNotBlank() || it.numeroSerie.isNotBlank() }
            .soloDisponiblesParaEmision(almacenId)
            .delAlmacen(almacenId)
    }

    suspend fun listarProductosDevolucion(
        companyRuc: String,
        token: String?,
        clienteId: String,
    ): Result<List<ProductoDevolucionCliente>> = apiCall(companyRuc, token, "consultar devoluciones") { authToken ->
        RetrofitClient.api.listarProductosDevolucion(
            ruc = companyRuc,
            authorization = bearer(authToken),
            clienteId = clienteId,
        )
    }

    suspend fun registrarEntrada(
        companyRuc: String,
        token: String?,
        body: RegistrarEntradaRequest,
    ): Result<Movimiento> = apiCall(companyRuc, token, "registrar ingresos") { authToken ->
        RetrofitClient.api.registrarEntrada(companyRuc, bearer(authToken), body.toApiBody())
            .sanitizarDesdeApi()
    }

    suspend fun registrarSalida(
        companyRuc: String,
        token: String?,
        body: RegistrarSalidaRequest,
    ): Result<Movimiento> = apiCall(companyRuc, token, "registrar salidas") { authToken ->
        RetrofitClient.api.registrarSalida(companyRuc, bearer(authToken), body)
            .sanitizarDesdeApi()
    }

    suspend fun listarSalidas(
        companyRuc: String,
        token: String?,
    ): Result<List<Movimiento>> = apiCall(companyRuc, token, "ver salidas") { authToken ->
        RetrofitClient.api.listarSalidas(companyRuc, bearer(authToken))
            .map { it.sanitizarDesdeApi() }
            .ordenadosPorFechaReciente()
    }

    suspend fun listarIngresos(
        companyRuc: String,
        token: String?,
    ): Result<List<Movimiento>> = apiCall(companyRuc, token, "ver ingresos") { authToken ->
        RetrofitClient.api.listarMovimientos(
            ruc = companyRuc,
            authorization = bearer(authToken),
            tipo = MovimientoTipo.ENTRADA.name,
        ).map { it.sanitizarDesdeApi() }.ordenadosPorFechaReciente()
    }

    suspend fun listarMovimientosCliente(
        companyRuc: String,
        token: String?,
        clienteId: String,
    ): Result<List<Movimiento>> = apiCall(companyRuc, token, "ver historial del cliente") { authToken ->
        RetrofitClient.api.listarMovimientos(
            ruc = companyRuc,
            authorization = bearer(authToken),
            clienteId = clienteId,
        ).map { it.sanitizarDesdeApi() }.ordenadosPorFechaReciente()
    }

    suspend fun buscarUbicaciones(
        companyRuc: String,
        token: String?,
        query: String,
        modo: String,
    ): Result<List<UbicacionProducto>> = apiCall(companyRuc, token, "consultar historial del ítem") { authToken ->
        val q = query.trim()
        if (q.length < 2) return@apiCall emptyList()
        RetrofitClient.api.buscarUbicaciones(
            ruc = companyRuc,
            authorization = bearer(authToken),
            query = q,
            modo = modo,
        ).ordenadosPorFechaReciente()
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
