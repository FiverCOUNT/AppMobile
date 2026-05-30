package com.factapp.jhonny.network.dto.request

import com.factapp.jhonny.network.dto.model.Address
import com.google.gson.annotations.SerializedName

/** Body POST para registrar un almacén en la empresa. */
data class CrearAlmacenRequest(
    val codigo: String,
    val nombre: String,
    @SerializedName(value = "address", alternate = ["direccion"])
    val address: Address? = null,
)
