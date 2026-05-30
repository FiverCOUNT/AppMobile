package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/**
 * Línea de detalle Greenter (`SaleDetail`) — snapshot persistido en [Invoice].
 *
 * Para operar catálogo antes de emitir (pantalla, inventario), usar [LineaCatalogoItem].
 */
data class SaleDetail(
    val id: String? = null,
    @SerializedName("invoice_id")
    val invoiceId: String? = null,
    @SerializedName(value = "catalog_item_id", alternate = ["cod_producto", "codigo"])
    val catalogItemId: String? = null,
    @SerializedName(value = "descripcion", alternate = ["nombre"])
    val descripcion: String? = null,
    val nombre: String? = null,
    val cantidad: Double,
    val unidad: String? = "NIU",
    @SerializedName(value = "mto_precio_unitario", alternate = ["precio_unitario"])
    val mtoPrecioUnitario: Double? = null,
    @SerializedName(value = "tip_afe_igv", alternate = ["afectacion_igv"])
    val tipAfeIgv: String = "10",
    @SerializedName(value = "mto_valor_venta", alternate = ["subtotal"])
    val mtoValorVenta: Double? = null,
    @SerializedName(value = "mto_igv", alternate = ["igv"])
    val mtoIgv: Double? = null,
    @SerializedName(value = "total", alternate = ["total_impuestos"])
    val totalFactura: Double? = null,
    @SerializedName("mto_valor_unitario")
    val mtoValorUnitario: Double? = null,
    @SerializedName("mto_base_igv")
    val mtoBaseIgv: Double? = null,
    @SerializedName("porcentaje_igv")
    val porcentajeIgv: Double? = null,
    @SerializedName(value = "producto_serie", alternate = ["serie"])
    val productoSerie: ProductoSerie? = null,
) {
    val precioUnitario: Double
        get() = mtoPrecioUnitario ?: 0.0

    val subtotal: Double
        get() = mtoValorVenta ?: (precioUnitario * cantidad)

    val igv: Double
        get() = mtoIgv ?: 0.0

    val total: Double
        get() = totalFactura ?: (subtotal + igv)

    val afectacionIgv: String
        get() = tipAfeIgv
}

fun SaleDetail.textoEnComprobante(): String {
    nombre?.takeIf { it.isNotBlank() }?.let { return it }
    descripcion?.takeIf { it.isNotBlank() }?.let { return it }
    return ""
}
