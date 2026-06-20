package com.factapp.jhonny.network.dto

import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Company as NetworkCompany

/** RUC de la empresa del usuario logueado (requerido por `/api/empresas/{ruc}/catalogo`). */
fun Usuario?.companyRucParaCatalogo(): String? =
    this?.company?.ruc?.takeIf { it.isNotBlank() }

/** Almacén asignado al usuario (query `?almacen_id=` en catálogo, ingresos, salidas). */
fun Usuario?.almacenIdParaOperaciones(): String? =
    this?.almacenId?.takeIf { it.isNotBlank() }

/** Datos del emisor para PDF local (nombre, RUC, dirección desde sesión). */
fun Usuario?.emisorParaPdf(): NetworkCompany? =
    this?.company?.let { c ->
        NetworkCompany(
            ruc = c.ruc,
            nombre = c.nombre,
            rutaLogo = c.rutaLogo,
            telefono = c.telefono,
            address = c.direccion?.takeIf { it.isNotBlank() }?.let { Address.linea(it) },
        )
    }

