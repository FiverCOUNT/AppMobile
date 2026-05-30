package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Leyenda SUNAT (Greenter `Legend`, catálogo 52). Ej. código 1000 = monto en letras. */
data class Legend(
    @SerializedName(value = "code", alternate = ["codigo"])
    val code: String,
    @SerializedName(value = "value", alternate = ["valor"])
    val value: String,
)
