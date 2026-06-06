package com.factapp.jhonny.network.dto.request

import com.google.gson.annotations.SerializedName

data class CrearCatalogItemRequest(
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
    val manejaStock: Boolean = false,
    @SerializedName("maneja_serie")
    val manejaSerie: Boolean = false,
    @SerializedName("stock_actual")
    val stockActual: Double? = null,
    @SerializedName("duracion_minutos")
    val duracionMinutos: Int? = null,
)

data class ActualizarCatalogItemRequest(
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
    val manejaStock: Boolean = false,
    @SerializedName("maneja_serie")
    val manejaSerie: Boolean = false,
    @SerializedName("stock_actual")
    val stockActual: Double? = null,
    @SerializedName("duracion_minutos")
    val duracionMinutos: Int? = null,
)

data class PatchCatalogItemRequest(
    val activo: Boolean,
)
