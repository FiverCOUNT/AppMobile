package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/** Cliente / receptor persistido (API `GET/POST …/clientes`). En boletas → [Invoice.cliente]; UBL → [Company] vía [aCompany]. */
data class Cliente(
    val id: String,
    @SerializedName("company_ruc")
    val companyRuc: String,
    @SerializedName("tipo_doc")
    val tipoDoc: String,
    @SerializedName("numero_doc")
    val numeroDoc: String,
    @SerializedName("razon_social")
    val razonSocial: String,
    @SerializedName(value = "address", alternate = ["direccion"])
    val address: Address? = null,
    val telefono: String? = null,
    val activo: Boolean = true,
) {
    val esPersonaNatural: Boolean
        get() = tipoDoc == TIPO_DOC_DNI

    val etiquetaDocumento: String
        get() = when (tipoDoc) {
            TIPO_DOC_DNI -> "DNI $numeroDoc"
            TIPO_DOC_RUC -> "RUC $numeroDoc"
            else -> "Doc. $numeroDoc"
        }
}

val Cliente.direccion: String?
    get() = address.lineaPrincipal

const val TIPO_DOC_DNI = "1"
const val TIPO_DOC_RUC = "6"

fun Cliente.aMovimientoCliente(): MovimientoCliente = MovimientoCliente(
    tipoDoc = tipoDoc,
    numeroDoc = numeroDoc,
    razonSocial = razonSocial,
)

fun dniValido(numero: String): Boolean =
    numero.length == 8 && numero.all { it.isDigit() }
