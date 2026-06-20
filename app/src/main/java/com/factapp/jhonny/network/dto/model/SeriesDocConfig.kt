package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Serie y correlativo por tipo de comprobante SUNAT (01, 03, 07, 08, 09). */
data class SeriesDocConfig(
    val serie: String,
    @SerializedName("correlativo_inicio")
    val correlativoInicio: Int? = null,
    @SerializedName("correlativo_digitos")
    val correlativoDigitos: Int? = null,
)
