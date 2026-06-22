package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Series entregadas al cliente en una venta (comprobante afectado por NC de devolución). */
data class SeriesComprobanteItem(
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    val series: List<ProductoSerie> = emptyList(),
)
