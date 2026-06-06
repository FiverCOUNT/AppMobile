package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.InventarioSaldo
import com.factapp.jhonny.network.dto.model.TipoInventarioSaldo
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventarioAlmacenJsonTest {

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(com.factapp.jhonny.network.dto.model.Address::class.java, AddressTypeAdapter())
        .create()

    @Test
    fun parseAlmacenesFromApi() {
        val json = """
            [
              {
                "id": "b1000001-0001-4000-8000-000000000001",
                "company_ruc": "20100000001",
                "codigo": "ALM01",
                "nombre": "Almacén Principal",
                "activo": true,
                "address": {
                  "ubigeo": "150103",
                  "departamento": "Lima",
                  "provincia": "Lima",
                  "distrito": "Ate",
                  "direccion": "Jr. Almacén 789",
                  "cod_local": "0000"
                }
              },
              {
                "id": "b1000001-0001-4000-8000-000000000002",
                "company_ruc": "20100000001",
                "codigo": "ALM02",
                "nombre": "Almacén Secundario",
                "activo": true,
                "address": null
              }
            ]
        """.trimIndent()

        val type = object : TypeToken<List<Almacen>>() {}.type
        val lista: List<Almacen> = gson.fromJson(json, type)
        assertEquals(2, lista.size)
        assertEquals("ALM01", lista[0].codigo)
    }

    @Test
    fun parseInventarioFromApi() {
        val json = """
            [
              {
                "id": "i1000001-0001-4000-8000-000000000003",
                "company_ruc": "20100000001",
                "catalog_item_id": "d1000001-0001-4000-8000-000000000001",
                "almacen_id": "b1000001-0001-4000-8000-000000000001",
                "producto_serie_id": "e1000001-0001-4000-8000-000000000001",
                "saldo_key": null,
                "cantidad": 1,
                "tipo": "SERIE",
                "catalog_item_nombre": "Laptop Demo 15\"",
                "almacen_nombre": "Almacén Principal",
                "almacen_codigo": "ALM01",
                "numero_serie": "SN-DEMO-LAP-001"
              }
            ]
        """.trimIndent()

        val type = object : TypeToken<List<InventarioSaldo>>() {}.type
        val lista: List<InventarioSaldo> = gson.fromJson(json, type)
        assertEquals(1, lista.size)
        assertEquals(TipoInventarioSaldo.SERIE, lista[0].tipo)
        assertEquals("Laptop Demo 15\"", lista[0].nombreProducto)
    }
}
