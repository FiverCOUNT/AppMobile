package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.ordenadosPorFechaEmisionReciente
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.SeriesComprobanteItem
import com.factapp.jhonny.network.dto.request.EmitirComprobanteRequest
import com.factapp.jhonny.network.dto.request.GreBajaRequest
import com.factapp.jhonny.network.dto.request.GreEventoRequest
import com.factapp.jhonny.network.dto.request.GreOperacionResponse
import java.time.LocalDate

object ComprobanteRepository {

    suspend fun emitir(
        companyRuc: String,
        token: String?,
        body: EmitirComprobanteRequest,
    ): Result<Invoice> = apiCall(companyRuc, token, "emitir comprobantes") { authToken ->
        RetrofitClient.api.guardarComprobante(companyRuc, bearer(authToken), body).normalizado()
    }

    suspend fun reenviar(
        companyRuc: String,
        token: String?,
        comprobanteId: String,
    ): Result<Invoice> = apiCall(companyRuc, token, "reenviar comprobante") { authToken ->
        RetrofitClient.api.reenviarComprobante(companyRuc, comprobanteId, bearer(authToken)).normalizado()
    }

    suspend fun registrarGreEvento(
        companyRuc: String,
        token: String?,
        comprobanteId: String,
        codigoEvento: String,
        detalle: String? = null,
    ): Result<GreOperacionResponse> = apiCall(companyRuc, token, "registrar evento GRE") { authToken ->
        RetrofitClient.api.registrarGreEvento(
            companyRuc,
            comprobanteId,
            bearer(authToken),
            GreEventoRequest(codigoEvento = codigoEvento, detalle = detalle),
        )
    }

    suspend fun comunicarGreBaja(
        companyRuc: String,
        token: String?,
        comprobanteId: String,
        motivo: String,
    ): Result<GreOperacionResponse> = apiCall(companyRuc, token, "comunicar baja GRE") { authToken ->
        RetrofitClient.api.comunicarGreBaja(
            companyRuc,
            comprobanteId,
            bearer(authToken),
            GreBajaRequest(motivo = motivo),
        )
    }

    suspend fun listarEmitidos(
        companyRuc: String,
        token: String?,
        desde: LocalDate? = null,
        hasta: LocalDate? = null,
    ): Result<List<Invoice>> = apiCall(companyRuc, token, "consultar comprobantes emitidos") { authToken ->
        RetrofitClient.api.listarComprobantesEmitidos(
            companyRuc,
            bearer(authToken),
            desde?.toString(),
            hasta?.toString(),
        ).map { it.normalizado() }.ordenadosPorFechaEmisionReciente()
    }

    suspend fun listarCompras(
        companyRuc: String,
        token: String?,
        desde: java.time.LocalDate? = null,
        hasta: java.time.LocalDate? = null,
    ): Result<List<Invoice>> = apiCall(companyRuc, token, "consultar compras") { authToken ->
        RetrofitClient.api.listarCompras(
            companyRuc,
            bearer(authToken),
            desde?.toString(),
            hasta?.toString(),
        ).map { it.normalizado() }.ordenadosPorFechaEmisionReciente()
    }

    suspend fun listarSeriesEntregadas(
        companyRuc: String,
        token: String?,
        comprobanteId: String,
    ): Result<List<SeriesComprobanteItem>> = apiCall(companyRuc, token, "consultar series entregadas") { authToken ->
        RetrofitClient.api.listarSeriesEntregadasComprobante(
            companyRuc,
            comprobanteId,
            bearer(authToken),
        )
    }

    suspend fun descargarArchivo(
        companyRuc: String,
        token: String?,
        comprobanteId: String,
        tipo: String,
        formatoPdf: PdfFormato? = null,
    ): Result<ByteArray> = apiCall(companyRuc, token, "descargar $tipo del comprobante") { authToken ->
        RetrofitClient.api.descargarArchivoComprobante(
            companyRuc,
            comprobanteId,
            tipo,
            bearer(authToken),
            formato = formatoPdf?.query,
        ).use { body -> body.bytes() }
    }

    enum class PdfFormato(val query: String) {
        A4("a4"),
        TICKET("ticket"),
    }

    private suspend fun <T> apiCall(
        companyRuc: String,
        token: String?,
        accion: String,
        block: suspend (String) -> T,
    ): Result<T> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Sin empresa vinculada"))
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
