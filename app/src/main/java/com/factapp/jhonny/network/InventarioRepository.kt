package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.request.CrearAlmacenRequest
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.MovimientoEstado
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.lineaCatalogoConSerie
import com.factapp.jhonny.network.dto.model.lineaCatalogoSinSerie
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.ProductoSerieEstado
import com.factapp.jhonny.network.dto.demo.InventarioDemo
import com.factapp.jhonny.network.dto.model.MovimientoTipo
import com.factapp.jhonny.network.dto.request.RegistrarEntradaRequest
import com.factapp.jhonny.network.dto.request.RegistrarSalidaRequest
import com.factapp.jhonny.network.dto.model.resumenSeries
import java.time.Instant

/**
 * Almacén, series y movimientos desde el servidor.
 * Si el endpoint no existe aún, usa [InventarioDemo] sin romper la app.
 */
object InventarioRepository {

    suspend fun listarAlmacenes(
        companyRuc: String,
        token: String?,
    ): Result<List<Almacen>> = llamar(
        companyRuc = companyRuc,
        token = token,
        demo = { InventarioDemo.almacenesDemo(companyRuc) },
    ) {
        RetrofitClient.api.listarAlmacenes(companyRuc, bearer(token))
    }

    suspend fun crearAlmacen(
        companyRuc: String,
        token: String?,
        body: CrearAlmacenRequest,
    ): Result<Almacen> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (body.codigo.isBlank() || body.nombre.isBlank()) {
            return Result.failure(IllegalArgumentException("Código y nombre son obligatorios"))
        }
        if (token.isNullOrBlank()) {
            return Result.success(InventarioDemo.crearAlmacenDemo(companyRuc, body))
        }
        return try {
            Result.success(
                RetrofitClient.api.crearAlmacen(companyRuc, bearer(token), body),
            )
        } catch (_: Exception) {
            Result.success(InventarioDemo.crearAlmacenDemo(companyRuc, body))
        }
    }

    suspend fun listarSeriesDisponibles(
        companyRuc: String,
        catalogItemId: String,
        token: String?,
        almacenId: String? = null,
    ): Result<List<ProductoSerie>> = llamar(
        companyRuc = companyRuc,
        token = token,
        demo = { InventarioDemo.seriesDisponibles(companyRuc, catalogItemId, almacenId) },
    ) {
        RetrofitClient.api.listarSeriesDisponibles(
            ruc = companyRuc,
            catalogItemId = catalogItemId,
            authorization = bearer(token),
            almacenId = almacenId,
        )
    }

    suspend fun registrarEntrada(
        companyRuc: String,
        token: String?,
        body: RegistrarEntradaRequest,
    ): Result<Movimiento> = llamar(
        companyRuc = companyRuc,
        token = token,
        demo = { movimientoDemoEntrada(body) },
    ) {
        RetrofitClient.api.registrarEntrada(companyRuc, bearer(token), body)
    }

    suspend fun registrarSalida(
        companyRuc: String,
        token: String?,
        body: RegistrarSalidaRequest,
    ): Result<Movimiento> = llamar(
        companyRuc = companyRuc,
        token = token,
        demo = { salidaDemo(body) },
    ) {
        RetrofitClient.api.registrarSalida(companyRuc, bearer(token), body)
    }

    suspend fun listarSalidas(
        companyRuc: String,
        token: String?,
    ): Result<List<Movimiento>> = llamar(
        companyRuc = companyRuc,
        token = token,
        demo = { InventarioDemo.salidasDemo(companyRuc) },
    ) {
        RetrofitClient.api.listarSalidas(companyRuc, bearer(token))
    }

    suspend fun listarIngresos(
        companyRuc: String,
        token: String?,
    ): Result<List<Movimiento>> = llamar(
        companyRuc = companyRuc,
        token = token,
        demo = { InventarioDemo.movimientosEntradaDemo(companyRuc) },
    ) {
        RetrofitClient.api.listarMovimientos(
            ruc = companyRuc,
            authorization = bearer(token),
            tipo = MovimientoTipo.ENTRADA.name,
        )
    }

    suspend fun listarHistorial(
        companyRuc: String,
        token: String?,
    ): Result<List<Movimiento>> = llamar(
        companyRuc = companyRuc,
        token = token,
        demo = { InventarioDemo.historialDemo(companyRuc) },
    ) {
        val movimientos = RetrofitClient.api.listarMovimientos(companyRuc, bearer(token))
        val salidas = RetrofitClient.api.listarSalidas(companyRuc, bearer(token))
        (movimientos + salidas)
            .distinctBy { it.id }
            .sortedByDescending { it.fechaEfectiva }
    }

    private suspend fun <T> llamar(
        companyRuc: String,
        token: String?,
        demo: () -> T,
        api: suspend () -> T,
    ): Result<T> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Empresa sin RUC"))
        }
        if (token.isNullOrBlank()) {
            return Result.success(demo())
        }
        return try {
            Result.success(api())
        } catch (_: Exception) {
            Result.success(demo())
        }
    }

    private fun bearer(token: String?) = "Bearer $token"

    private fun movimientoDemoEntrada(body: RegistrarEntradaRequest): Movimiento = Movimiento(
        id = "demo-mov-${System.currentTimeMillis()}",
        companyRuc = body.companyRuc,
        almacenId = body.almacenId,
        tipo = MovimientoTipo.ENTRADA,
        referenciaTipo = "INGRESO_MANUAL",
        lineas = body.lineas.map { linea ->
            LineaCatalogoItem(
                catalogItemId = linea.catalogItemId,
                cantidad = linea.cantidad,
                numerosSerie = linea.numerosSerie.orEmpty(),
                serieIds = linea.serieIds,
            )
        },
        fecha = Instant.now().toString(),
        observaciones = body.observaciones,
    )

    private fun salidaDemo(body: RegistrarSalidaRequest): Movimiento {
        val movimientoId = "demo-ent-${System.currentTimeMillis()}"
        return Movimiento(
            id = movimientoId,
            companyRuc = body.companyRuc,
            almacenId = body.almacenId,
            almacenDestinoId = body.almacenDestinoId,
            tipo = MovimientoTipo.SALIDA,
            numero = "ENT-${movimientoId.takeLast(5).uppercase()}",
            estado = MovimientoEstado.DESPACHADA,
            comprobanteId = body.comprobanteId,
            guiaRemisionId = body.guiaRemisionId,
            lineas = body.lineas.map { linea ->
                val serieId = linea.serieIds?.firstOrNull()
                if (serieId != null) {
                    val serie = InventarioDemo.seriesDisponibles(body.companyRuc, linea.catalogItemId)
                        .firstOrNull { it.id == serieId }
                        ?.copy(
                            almacenId = body.almacenId,
                            estado = ProductoSerieEstado.ENTREGADO,
                            entregaId = movimientoId,
                        )
                        ?: ProductoSerie(
                            id = serieId,
                            companyRuc = body.companyRuc,
                            catalogItemId = linea.catalogItemId,
                            numeroSerie = serieId,
                            almacenId = body.almacenId,
                            estado = ProductoSerieEstado.ENTREGADO,
                            entregaId = movimientoId,
                        )
                    lineaCatalogoConSerie(serie = serie, cantidad = linea.cantidad)
                } else {
                    lineaCatalogoSinSerie(
                        catalogItemId = linea.catalogItemId,
                        cantidad = linea.cantidad,
                    )
                }
            },
            fecha = Instant.now().toString(),
            cliente = body.cliente,
            fechaDespacho = Instant.now().toString(),
            referenciaTipo = if (body.almacenDestinoId != null) "TRASLADO" else "SALIDA",
        )
    }
}
