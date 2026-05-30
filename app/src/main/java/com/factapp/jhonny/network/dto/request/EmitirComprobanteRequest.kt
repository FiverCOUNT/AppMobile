package com.factapp.jhonny.network.dto.request

import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.google.gson.annotations.SerializedName

/**
 * Body para POST emitir comprobante (desde la pantalla de emision).
 *
 * Guia de emision: [facturas] obligatorio (>=1); [lineas] suele ir vacio.
 */
data class EmitirComprobanteRequest(
    @SerializedName("company_ruc")
    val companyRuc: String,
    val tipo: String,
    val receptor: Company,
    val lineas: List<EmitirLineaRequest> = emptyList(),
    @SerializedName("documento_afectado")
    val documentoAfectado: Invoice? = null,
    /** Solo [InvoiceTipoDoc.GUIA_EMISION]: facturas vinculadas al traslado. */
    val facturas: List<Invoice>? = null,
    @SerializedName("motivo_nota")
    val motivoNota: String? = null,
    val observaciones: String? = null,
)

data class EmitirLineaRequest(
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    val cantidad: Double,
    /** IDs de series vendidas/despachadas en esta linea. */
    @SerializedName("serie_ids")
    val serieIds: List<String>? = null,
)
