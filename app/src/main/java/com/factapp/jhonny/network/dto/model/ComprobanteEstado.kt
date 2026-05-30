package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

enum class ComprobanteEstado {
    @SerializedName("BORRADOR")
    BORRADOR,

    @SerializedName("ENVIADO")
    ENVIADO,

    @SerializedName("ACEPTADO")
    ACEPTADO,

    @SerializedName("RECHAZADO")
    RECHAZADO,

    @SerializedName("ANULADO")
    ANULADO,
}
