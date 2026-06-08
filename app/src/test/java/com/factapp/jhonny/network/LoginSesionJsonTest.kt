package com.factapp.jhonny.network

import com.factapp.jhonny.data.local.toUsuarioEntity
import com.factapp.jhonny.network.dto.ApiEnvelope
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.UsuarioSesionApi
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LoginSesionJsonTest {

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(com.factapp.jhonny.network.dto.model.Address::class.java, AddressTypeAdapter())
        .registerTypeAdapter(Company::class.java, CompanyTypeAdapter())
        .create()

    @Test
    fun parseLoginResponseAndMapToRoomEntity() {
        val json = """
            {
              "success": true,
              "data": {
                "accessToken": "jwt-access",
                "refreshToken": "refresh-abc",
                "tokenType": "Bearer",
                "almacenId": "b1000001-0001-4000-8000-000000000001",
                "almacenNombre": "Almacén Principal",
                "almacenCodigo": "ALM01",
                "user": {
                  "id": 1,
                  "email": "demo@empresademo.pe",
                  "estado": "ACTIVO",
                  "rol": "USUARIO",
                  "companyRuc": "20100000001",
                  "companyNombre": "Empresa Demo",
                  "company": {
                    "ruc": "20100000001",
                    "nombre": "Empresa Demo"
                  },
                  "almacenId": "b1000001-0001-4000-8000-000000000001",
                  "almacenNombre": "Almacén Principal",
                  "lastUpdated": "1710000000000"
                }
              }
            }
        """.trimIndent()

        val type = object : TypeToken<ApiEnvelope<UsuarioSesionApi>>() {}.type
        val envelope = gson.fromJson<ApiEnvelope<UsuarioSesionApi>>(json, type)

        assertEquals(true, envelope.success)
        val session = requireNotNull(envelope.data)
        val usuario = session.toUsuarioEntity()

        assertEquals("jwt-access", usuario.token)
        assertEquals("refresh-abc", usuario.refreshToken)
        assertEquals("demo@empresademo.pe", usuario.email)
        assertEquals("b1000001-0001-4000-8000-000000000001", usuario.almacenId)
        assertEquals("Almacén Principal", usuario.almacenNombre)
        assertEquals("20100000001", usuario.company?.ruc)
        assertEquals("Empresa Demo", usuario.company?.nombre)
    }
}
