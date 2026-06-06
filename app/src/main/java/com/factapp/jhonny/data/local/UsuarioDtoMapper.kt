package com.factapp.jhonny.data.local

import com.factapp.jhonny.modelos.Company as CompanyEntity
import com.factapp.jhonny.network.dto.model.BusinessTemplate
import com.factapp.jhonny.modelos.Usuario as UsuarioEntity
import com.factapp.jhonny.network.dto.model.Company as CompanyDto
import com.factapp.jhonny.network.dto.model.UsuarioSesionApi
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
    lastUpdated = Timestamp(lastUpdated.takeIf { it > 0 } ?: System.currentTimeMillis()),
    estado = estado,
    rol = rol,
    almacenId = almacenId,
    almacenNombre = almacenNombre,
    company = company?.toEntity() ?: companyDesdeCamposPlanos(),
)

/** Login/refresh: fusiona tokens del `data` con el [UsuarioDto] anidado en `user`. */
fun UsuarioSesionApi.toUsuarioEntity(): UsuarioEntity = user.copy(
    token = accessToken,
    refreshToken = refreshToken,
).toEntity()

private fun UsuarioDto.companyDesdeCamposPlanos(): CompanyEntity? =
    companyRuc?.takeIf { it.isNotBlank() }?.let { ruc ->
        CompanyEntity(
            ruc = ruc,
            nombre = companyNombre?.takeIf { it.isNotBlank() } ?: "Empresa",
            rutaFirma = null,
            rutaLogo = null,
            name_logo = null,
            direccion = null,
            telefono = null,
            plantilla = BusinessTemplate.GENERAL,
        )
    }
