package com.factapp.jhonny.data.local

import com.factapp.jhonny.modelos.Company as CompanyEntity
import com.factapp.jhonny.network.dto.model.BusinessTemplate
import com.factapp.jhonny.modelos.Usuario as UsuarioEntity
import com.factapp.jhonny.network.dto.model.Company as CompanyDto
import com.factapp.jhonny.network.dto.model.UsuarioSesionApi
import com.factapp.jhonny.network.dto.model.direccion
import com.factapp.jhonny.network.dto.model.Usuario as UsuarioDto
import java.sql.Timestamp

fun CompanyDto.toEntityOrNull(): CompanyEntity? {
    val rucFinal = ruc.takeIf { it.isNotBlank() }
        ?: documentoNumero.takeIf { it.isNotBlank() }
        ?: return null
    return CompanyEntity(
        ruc = rucFinal,
        nombre = nombre.takeIf { it.isNotBlank() } ?: "Empresa",
        rutaFirma = rutaFirma,
        rutaLogo = rutaLogo,
        name_logo = nameLogo,
        direccion = direccion,
        telefono = telefonoPrincipal,
        plantilla = BusinessTemplate.GENERAL,
    )
}

fun UsuarioDto.toEntity(): UsuarioEntity {
    val emailNormalizado = email.trim().lowercase()
    if (emailNormalizado.isBlank()) {
        throw IllegalStateException("El servidor no devolvió email de usuario")
    }
    return UsuarioEntity(
        email = emailNormalizado,
        contrasena = contrasena,
        token = token,
        refreshToken = refreshToken,
        lastUpdated = Timestamp(lastUpdated.takeIf { it > 0 } ?: System.currentTimeMillis()),
        estado = estado,
        rol = rol,
        almacenId = almacenId,
        almacenNombre = almacenNombre,
        company = company?.toEntityOrNull() ?: companyDesdeCamposPlanos(),
    )
}

/** Login/refresh/me: fusiona tokens, almacén y configuración de empresa con el perfil anidado. */
fun UsuarioSesionApi.toUsuarioEntity(): UsuarioEntity {
    val empresa = configuracion?.empresa ?: user.company
    return user.copy(
        token = accessToken,
        refreshToken = refreshToken,
        almacenId = user.almacenId?.takeIf { it.isNotBlank() } ?: almacenId,
        almacenNombre = user.almacenNombre?.takeIf { it.isNotBlank() } ?: almacenNombre,
        almacenCodigo = user.almacenCodigo?.takeIf { it.isNotBlank() } ?: almacenCodigo,
        company = empresa,
        companyRuc = empresa?.ruc ?: user.companyRuc,
        companyNombre = empresa?.nombre ?: user.companyNombre,
    ).toEntity()
}

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
