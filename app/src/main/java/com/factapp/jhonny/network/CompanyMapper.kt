package com.factapp.jhonny.network

import com.factapp.jhonny.modelos.Company as CompanyEntity
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Company as CompanyDto

fun CompanyEntity.toDto(): CompanyDto = CompanyDto(
    ruc = ruc,
    nombre = nombre,
    rutaFirma = rutaFirma,
    rutaLogo = rutaLogo,
    nameLogo = name_logo,
    address = direccion?.let { Address.linea(it) },
    telefono = telefono,
)
