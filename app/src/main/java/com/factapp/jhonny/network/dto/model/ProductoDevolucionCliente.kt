package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Producto aún pendiente de devolución por un cliente (entregado y no reingresado). */
data class ProductoDevolucionCliente(
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    @SerializedName("company_ruc")
    val companyRuc: String,
    val kind: String,
    val codigo: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val unidad: String,
    @SerializedName("precio_unitario")
    val precioUnitario: Double = 0.0,
    @SerializedName("afectacion_igv")
    val afectacionIgv: String = "10",
    val activo: Boolean = true,
    @SerializedName("maneja_stock")
    val manejaStock: Boolean = true,
    @SerializedName("maneja_serie")
    val manejaSerie: Boolean = false,
    @SerializedName("cantidad_pendiente")
    val cantidadPendiente: Double,
    val series: List<ProductoSerie> = emptyList(),
) {
    fun aCatalogItem(): CatalogItem = CatalogItem(
        id = catalogItemId,
        companyRuc = companyRuc,
        kind = kind,
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        unidad = unidad,
        precioUnitario = precioUnitario,
        afectacionIgv = afectacionIgv,
        activo = activo,
        manejaStock = manejaStock,
        manejaSerie = manejaSerie,
        stockActual = cantidadPendiente,
    )
}
