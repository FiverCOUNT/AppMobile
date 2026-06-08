package com.factapp.jhonny.network.dto

import com.factapp.jhonny.modelos.Usuario

/** RUC de la empresa del usuario logueado (requerido por `/api/empresas/{ruc}/catalogo`). */
fun Usuario?.companyRucParaCatalogo(): String? =
    this?.company?.ruc?.takeIf { it.isNotBlank() }

/** Almacén asignado al usuario (query `?almacen_id=` en catálogo, ingresos, salidas). */
fun Usuario?.almacenIdParaOperaciones(): String? =
    this?.almacenId?.takeIf { it.isNotBlank() }

