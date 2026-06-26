package com.factapp.jhonny.network.dto.request

import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.google.gson.annotations.SerializedName

/**
 * Body para POST emitir comprobante (desde la pantalla de emision).
 *
 * GRE remitente: [facturas] obligatorio (>=1); [lineas] suele ir vacio.
 * GRE transportista: [remitente] y [lineas] obligatorios.
 */
data class EmitirComprobanteRequest(
    @SerializedName("company_ruc")
    val companyRuc: String,
    val tipo: String,
    val receptor: Company,
    val lineas: List<EmitirLineaRequest> = emptyList(),
    @SerializedName("documento_afectado")
    val documentoAfectado: Invoice? = null,
    /** Solo GRE remitente: facturas vinculadas al traslado. */
    val facturas: List<Invoice>? = null,
    /** Solo GRE transportista: quien remite las mercancías. */
    val remitente: Company? = null,
    /** Datos del traslado (GRE remitente / transportista). */
    val envio: GreEnvioRequest? = null,
    /** GRE transportista: referencia opcional a GRE remitente (09). */
    @SerializedName("guia_remitente")
    val guiaRemitente: Invoice? = null,
    @SerializedName("motivo_nota")
    val motivoNota: String? = null,
    @SerializedName("motivo_codigo")
    val motivoCodigo: String? = null,
    val observaciones: String? = null,
    @SerializedName("almacen_id")
    val almacenId: String? = null,
)

data class EmitirLineaRequest(
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    val cantidad: Double,
    /** Monto unitario con IGV a acreditar (NC por descuento / disminución). */
    @SerializedName("precio_unitario")
    val precioUnitario: Double? = null,
    /** Línea del documento afectado (evita reutilizar ítems ya devueltos/acreditados). */
    @SerializedName("sale_detail_id")
    val saleDetailId: String? = null,
    @SerializedName("producto_serie_id")
    val productoSerieId: String? = null,
    @SerializedName("numero_serie")
    val numeroSerie: String? = null,
)
