package com.factapp.jhonny.network.dto

import com.factapp.jhonny.modelos.Usuario

/** RUC usado solo para cargar [CatalogoDemo] si el usuario no trae empresa del API. */
const val RUC_DEMO_CATALOGO = "20100000001"

fun Usuario?.companyRucParaCatalogo(): String? {
    if (this == null) return null
    return company?.ruc?.takeIf { it.isNotBlank() } ?: RUC_DEMO_CATALOGO
}

