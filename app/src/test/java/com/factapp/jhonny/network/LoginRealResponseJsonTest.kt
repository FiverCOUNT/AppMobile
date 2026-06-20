package com.factapp.jhonny.network

import com.factapp.jhonny.data.local.toUsuarioEntity
import com.factapp.jhonny.network.dto.ApiEnvelope
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.UsuarioSesionApi
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginRealResponseJsonTest {

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(com.factapp.jhonny.network.dto.model.Address::class.java, AddressTypeAdapter())
        .registerTypeAdapter(Company::class.java, CompanyTypeAdapter())
        .create()

    @Test
    fun parseRealBackendLoginFor2Gmail() {
        val json = """
            {
              "success": true,
              "data": {
                "accessToken": "eyJ-test",
                "refreshToken": "refresh-test",
                "tokenType": "Bearer",
                "almacenId": "3d14a79d-605e-43a8-90fd-85493603206d",
                "almacenNombre": "ALMACEN DOS",
                "almacenCodigo": "ALMCE01",
                "user": {
                  "id": 2,
                  "email": "2@gmail.com",
                  "estado": "ACTIVO",
                  "rol": "ADMIN",
                  "companyId": "1",
                  "almacenId": "3d14a79d-605e-43a8-90fd-85493603206d",
                  "almacenNombre": "ALMACEN DOS",
                  "almacenCodigo": "ALMCE01",
                  "lastUpdated": "1780892921369",
                  "company": {
                    "ruc": "22222222222",
                    "nombre": "Papeleria Sonia"
                  },
                  "companyRuc": "22222222222",
                  "companyNombre": "Papeleria Sonia"
                }
              }
            }
        """.trimIndent()

        val type = object : TypeToken<ApiEnvelope<UsuarioSesionApi>>() {}.type
        val envelope = gson.fromJson<ApiEnvelope<UsuarioSesionApi>>(json, type)
        val usuario = requireNotNull(envelope.data).toUsuarioEntity()

        assertEquals("eyJ-test", usuario.token)
        assertEquals("2@gmail.com", usuario.email)
        assertEquals("22222222222", usuario.company?.ruc)
        assertEquals("ALMACEN DOS", usuario.almacenNombre)
    }
}
