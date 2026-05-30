package com.factapp.jhonny.network.dto.request

import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.TIPO_DOC_DNI
import com.google.gson.annotations.SerializedName

/** Body POST: registro manual solo para personas naturales (DNI). */
data class CrearClienteRequest(
    @SerializedName("tipo_doc")
    val tipoDoc: String = TIPO_DOC_DNI,
    @SerializedName("numero_doc")
    val numeroDoc: String,
    @SerializedName("razon_social")
    val razonSocial: String,
    @SerializedName(value = "address", alternate = ["direccion"])
    val address: Address? = null,
    val telefono: String? = null,
)
