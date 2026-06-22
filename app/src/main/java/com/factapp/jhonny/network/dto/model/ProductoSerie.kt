package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.network.gson.ProductoSerieEstadoTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

/**
 * Unidad física con número de serie (ej. una PC).
 * Pertenece a un [CatalogItem]; no duplica el producto en catálogo.
 */
data class ProductoSerie(
    @SerializedName(value = "id", alternate = ["producto_serie_id"])
    val id: String = "",
    @SerializedName("company_ruc")
    val companyRuc: String = "",
    @SerializedName("catalog_item_id")
    val catalogItemId: String = "",
    @SerializedName("numero_serie")
    val numeroSerie: String = "",
    @SerializedName("almacen_id")
    val almacenId: String? = null,
    @JsonAdapter(ProductoSerieEstadoTypeAdapter::class)
    val estado: ProductoSerieEstado = ProductoSerieEstado.DISPONIBLE,
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

/** Serie libre para vender (estado DISPONIBLE, sin documento previo). */
fun ProductoSerie.estaDisponibleParaEmision(almacenId: String? = null): Boolean {
    if (estado != ProductoSerieEstado.DISPONIBLE) return false
    if (!comprobanteId.isNullOrBlank() || !entregaId.isNullOrBlank()) return false
    return true
}

/** Visible en el selector del almacén de salida elegido (excluye otras bodegas). */
fun ProductoSerie.perteneceAlAlmacen(almacenId: String?): Boolean {
    val alm = almacenId?.takeIf { it.isNotBlank() } ?: return true
    return this.almacenId.isNullOrBlank() || this.almacenId == alm
}

fun List<ProductoSerie>.soloDisponiblesParaEmision(almacenId: String? = null): List<ProductoSerie> =
    filter { it.estaDisponibleParaEmision(almacenId) }

fun List<ProductoSerie>.delAlmacen(almacenId: String?): List<ProductoSerie> =
    filter { it.perteneceAlAlmacen(almacenId) }

/** Busca la misma unidad en una lista fresca del API (por id o número de serie). */
fun List<ProductoSerie>.encontrarEquivalente(ref: ProductoSerie): ProductoSerie? {
    if (ref.id.isNotBlank()) {
        find { it.id == ref.id }?.let { return it }
    }
    if (ref.numeroSerie.isNotBlank()) {
        return find { it.numeroSerie.equals(ref.numeroSerie, ignoreCase = true) }
    }
    return null
}
