package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Almacén / bodega de la empresa. */
data class Almacen(
    val id: String,
    @SerializedName("company_ruc")
    val companyRuc: String,
    val codigo: String,
    val nombre: String,
    @SerializedName(value = "address", alternate = ["direccion"])
    val address: Address? = null,
    val activo: Boolean = true,
)

val Almacen.direccion: String?
    get() = address.lineaPrincipal
