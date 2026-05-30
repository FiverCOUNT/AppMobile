package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.demo.ComprasDemo
import com.factapp.jhonny.network.dto.demo.ComprobantesEmitidosDemo
import com.factapp.jhonny.network.dto.filtrarPorRango
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.request.EmitirComprobanteRequest
import java.time.LocalDate

object ComprobanteRepository {

    suspend fun emitir(
        companyRuc: String,
        token: String?,
        body: EmitirComprobanteRequest,
    ): Result<Invoice> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Sin empresa vinculada"))
        }
        if (!token.isNullOrBlank()) {
            runCatching {
                RetrofitClient.api.guardarComprobante(companyRuc, bearer(token), body)
            }.onSuccess { return Result.success(it.normalizado()) }
        }
        return Result.success(
            Invoice(
                id = "demo-emit-${System.currentTimeMillis()}",
                companyRuc = companyRuc,
                tipoDoc = body.tipo,
                serie = "DEMO",
                correlativo = "00001",
                client = body.receptor,
                details = emptyList(),
                estado = ComprobanteEstado.ENVIADO,
            ),
        )
    }

    suspend fun listarEmitidos(
        companyRuc: String,
        token: String?,
        desde: LocalDate? = null,
        hasta: LocalDate? = null,
    ): Result<List<Invoice>> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Sin empresa vinculada"))
        }
        val desdeStr = desde?.toString()
        val hastaStr = hasta?.toString()
        if (!token.isNullOrBlank()) {
            runCatching {
                RetrofitClient.api.listarComprobantesEmitidos(
                    companyRuc,
                    bearer(token),
                    desdeStr,
                    hastaStr,
                )
            }.onSuccess { remoto ->
                if (remoto.isNotEmpty()) return Result.success(remoto.map { it.normalizado() })
            }
        }
        val demo = ComprobantesEmitidosDemo.listar(companyRuc)
        return Result.success(
            if (desde != null && hasta != null) demo.filtrarPorRango(desde, hasta) else demo,
        )
    }

    suspend fun listarCompras(
        companyRuc: String,
        token: String?,
    ): Result<List<Invoice>> {
        if (companyRuc.isBlank()) {
            return Result.failure(IllegalArgumentException("Sin empresa vinculada"))
        }
        if (!token.isNullOrBlank()) {
            runCatching {
                RetrofitClient.api.listarCompras(companyRuc, bearer(token))
            }.onSuccess { remoto ->
                if (remoto.isNotEmpty()) return Result.success(remoto.map { it.normalizado() })
            }
        }
        return Result.success(ComprasDemo.listar(companyRuc))
    }

    private fun bearer(token: String) = "Bearer $token"
}
