package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/**
 * Ubicación / domicilio fiscal (UBL `cac:Address`, Greenter `Address`).
 * Reutilizable en [Company], [Cliente], [Almacen], etc.
 */
data class Address(
    val ubigeo: String? = null,
    val departamento: String? = null,
    val provincia: String? = null,
    val distrito: String? = null,
    val urbanizacion: String? = null,
    @SerializedName(value = "direccion", alternate = ["linea"])
    val direccion: String? = null,
    /** Código de establecimiento SUNAT; `0000` = local principal. */
    @SerializedName("cod_local")
    val codLocal: String? = COD_LOCAL_PRINCIPAL,
) {
    val fiscalCompleta: Boolean
        get() = !ubigeo.isNullOrBlank() &&
            !departamento.isNullOrBlank() &&
            !provincia.isNullOrBlank() &&
            !distrito.isNullOrBlank() &&
            !direccion.isNullOrBlank()

    companion object {
        const val COD_LOCAL_PRINCIPAL = "0000"

        fun linea(texto: String): Address = Address(direccion = texto.trim())
    }
}

/** Línea principal para UI y APIs que solo envían texto. */
val Address?.lineaPrincipal: String?
    get() = this?.direccion?.takeIf { it.isNotBlank() } ?: this?.textoUnaLinea()

fun Address.textoUnaLinea(): String? {
    direccion?.takeIf { it.isNotBlank() }?.let { return it }
    val partes = listOfNotNull(
        departamento?.takeIf { it.isNotBlank() },
        provincia?.takeIf { it.isNotBlank() },
        distrito?.takeIf { it.isNotBlank() },
    ).distinct()
    return partes.takeIf { it.isNotEmpty() }?.joinToString(", ")
}

internal fun Address.esSoloLinea(): Boolean =
    !direccion.isNullOrBlank() &&
        ubigeo.isNullOrBlank() &&
        departamento.isNullOrBlank() &&
        provincia.isNullOrBlank() &&
        distrito.isNullOrBlank() &&
        urbanizacion.isNullOrBlank() &&
        (codLocal.isNullOrBlank() || codLocal == Address.COD_LOCAL_PRINCIPAL)

internal val ADDRESS_JSON_KEYS: Set<String> = setOf(
    "address",
    "domicilio_fiscal",
    "ubigeo",
    "departamento",
    "provincia",
    "distrito",
    "urbanizacion",
    "direccion",
    "cod_local",
)
