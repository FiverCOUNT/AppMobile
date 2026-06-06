package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Tipo de registro devuelto por `inventarioApiController` / `inventarioModel.toApi`. */
enum class TipoInventarioSaldo {
    @SerializedName("SALDO")
    SALDO,

    @SerializedName("SERIE")
    SERIE,
}

/**
 * Saldo de inventario por producto y almacén (`GET /api/empresas/{ruc}/inventario`).
 * Alineado con [inventarioModel.toApi] en BackEndEasy.
 */
data class InventarioSaldo(
    val id: String,
    @SerializedName("company_ruc")
    val companyRuc: String,
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    @SerializedName("almacen_id")
    val almacenId: String,
    @SerializedName("producto_serie_id")
    val productoSerieId: String? = null,
    @SerializedName("saldo_key")
    val saldoKey: String? = null,
    val cantidad: Double = 0.0,
    val tipo: TipoInventarioSaldo = TipoInventarioSaldo.SALDO,
    @SerializedName("catalog_item_nombre")
    val catalogItemNombre: String? = null,
    @SerializedName("almacen_nombre")
    val almacenNombre: String? = null,
    @SerializedName("almacen_codigo")
    val almacenCodigo: String? = null,
    @SerializedName("numero_serie")
    val numeroSerie: String? = null,
) {
    val nombreProducto: String
        get() = catalogItemNombre?.takeIf { it.isNotBlank() } ?: catalogItemId

    val nombreAlmacen: String
        get() = almacenNombre?.takeIf { it.isNotBlank() } ?: almacenCodigo ?: almacenId

    val esPorSerie: Boolean
        get() = tipo == TipoInventarioSaldo.SERIE
}

fun List<InventarioSaldo>.filtrarPorBusqueda(query: String): List<InventarioSaldo> {
    if (query.isBlank()) return this
    val q = query.trim().lowercase()
    return filter { row ->
        row.nombreProducto.lowercase().contains(q) ||
            row.nombreAlmacen.lowercase().contains(q) ||
            row.almacenCodigo?.lowercase()?.contains(q) == true ||
            row.numeroSerie?.lowercase()?.contains(q) == true ||
            row.tipo.name.lowercase().contains(q)
    }
}

fun InventarioSaldo.etiquetaCantidad(): String {
    val qty = if (cantidad % 1.0 == 0.0) cantidad.toLong().toString() else cantidad.toString()
    return if (esPorSerie && !numeroSerie.isNullOrBlank()) {
        "Serie $numeroSerie · $qty u."
    } else {
        "Stock: $qty u."
    }
}
