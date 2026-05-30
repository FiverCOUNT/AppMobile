package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/**
 * Ítem del catálogo tal como lo devuelve el API (producto o servicio).
 */
data class CatalogItem(
    val id: String,
    @SerializedName("company_ruc")
    val companyRuc: String,
    val kind: String,
    val codigo: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val unidad: String,
    @SerializedName("precio_unitario")
    val precioUnitario: Double,
    @SerializedName("afectacion_igv")
    val afectacionIgv: String = "10",
    val activo: Boolean = true,
    @SerializedName("maneja_stock")
    val manejaStock: Boolean = false,
    /** Si true, cada unidad tiene número de serie (stock = cantidad de series disponibles). */
    @SerializedName("maneja_serie")
    val manejaSerie: Boolean = false,
    @SerializedName("stock_actual")
    val stockActual: Double? = null,
    @SerializedName("duracion_minutos")
    val duracionMinutos: Int? = null,
) {
    val tipo: CatalogItemKind
        get() = CatalogItemKind.valueOf(kind.uppercase())

    val esProducto: Boolean
        get() = tipo == CatalogItemKind.PRODUCT

    val esServicio: Boolean
        get() = tipo == CatalogItemKind.SERVICE
}

fun CatalogItem.descripcionEnComprobante(): String =
    descripcion?.takeIf { it.isNotBlank() } ?: nombre
