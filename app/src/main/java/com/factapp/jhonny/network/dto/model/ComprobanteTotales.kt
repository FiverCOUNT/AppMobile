package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/**
 * Totales del comprobante (Greenter Invoice montos + API Laravel).
 */
data class ComprobanteTotales(
    @SerializedName(value = "subtotal", alternate = ["sub_total", "valor_venta"])
    val subtotal: Double,
    val igv: Double,
    @SerializedName(value = "total", alternate = ["mto_imp_venta"])
    val total: Double,
    @SerializedName(value = "moneda", alternate = ["tipo_moneda"])
    val moneda: String = "PEN",
    @SerializedName("mto_oper_gravadas")
    val mtoOperGravadas: Double? = null,
    @SerializedName("mto_oper_exoneradas")
    val mtoOperExoneradas: Double? = null,
    @SerializedName("mto_oper_inafectas")
    val mtoOperInafectas: Double? = null,
    @SerializedName("mto_oper_exportacion")
    val mtoOperExportacion: Double? = null,
    @SerializedName("total_impuestos")
    val totalImpuestos: Double? = null,
)
