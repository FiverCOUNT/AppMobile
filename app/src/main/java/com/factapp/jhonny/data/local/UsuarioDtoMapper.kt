package com.factapp.jhonny.data.local

import com.factapp.jhonny.modelos.Company as CompanyEntity
import com.factapp.jhonny.network.dto.model.BusinessTemplate
import com.factapp.jhonny.modelos.Usuario as UsuarioEntity
import com.factapp.jhonny.network.dto.model.Company as CompanyDto
import com.factapp.jhonny.network.dto.model.lineaPrincipal
import com.factapp.jhonny.network.dto.model.Usuario as UsuarioDto
import java.sql.Timestamp

fun CompanyDto.toEntity(): CompanyEntity = CompanyEntity(
    ruc = ruc,
    nombre = nombre,
    rutaFirma = rutaFirma,
    rutaLogo = rutaLogo,
    name_logo = nameLogo,
    direccion = address.lineaPrincipal,
    telefono = telefonoPrincipal,
    plantilla = BusinessTemplate.GENERAL,
)

fun UsuarioDto.toEntity(): UsuarioEntity = UsuarioEntity(
    email = email,
    contrasena = contrasena,
    token = token,
    refreshToken = refreshToken,
    lastUpdated = Timestamp(lastUpdated),
    estado = estado,
    company = company?.toEntity(),
)
