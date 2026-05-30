package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/**
 * Unidad física con número de serie (ej. una PC).
 * Pertenece a un [CatalogItem]; no duplica el producto en catálogo.
 */
data class ProductoSerie(
    val id: String,
    @SerializedName("company_ruc")
    val companyRuc: String,
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    @SerializedName("numero_serie")
    val numeroSerie: String,
    @SerializedName("almacen_id")
    val almacenId: String? = null,
    val estado: ProductoSerieEstado,
    @SerializedName("comprobante_id")
    val comprobanteId: String? = null,
    @SerializedName("entrega_id")
    val entregaId: String? = null,
)

enum class ProductoSerieEstado {
    @SerializedName("DISPONIBLE")
    DISPONIBLE,

    @SerializedName("RESERVADO")
    RESERVADO,

    @SerializedName("VENDIDO")
    VENDIDO,

    @SerializedName("ENTREGADO")
    ENTREGADO,

    @SerializedName("BAJA")
    BAJA,
}
