package com.factapp.jhonny.network

import com.factapp.jhonny.network.EpochMillisTypeAdapter
import com.factapp.jhonny.network.NullableEpochMillisTypeAdapter
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.MovimientoTipo
import com.factapp.jhonny.network.dto.model.fechaLegible
import com.factapp.jhonny.network.dto.model.sanitizarDesdeApi
import com.factapp.jhonny.network.dto.model.serieEnMovimiento
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovimientoEntradaJsonTest {

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Long::class.javaPrimitiveType, EpochMillisTypeAdapter())
        .registerTypeAdapter(Long::class.javaObjectType, NullableEpochMillisTypeAdapter())
        .create()

    @Test
    fun parseEntradaConTimestampDelBackend() {
        val json = """
            {
              "id": "5f49e69a-ec28-4d99-b61f-b9123b49c615",
              "company_ruc": "22222222222",
              "almacen_id": "3d14a79d-605e-43a8-90fd-85493603206d",
              "tipo": "ENTRADA",
              "fecha": 1780979169524,
              "observaciones": "Prueba entrada API",
              "referencia_tipo": "INGRESO_MANUAL",
              "numero": "MOV-0001",
              "estado": "DESPACHADA",
              "lineas": [
                {
                  "linea_id": "5ec5bd97-69a1-44b5-9c6d-67f20cad9cc3",
                  "catalog_item_id": "9e2d5841-815a-4616-81af-7106901add50",
                  "nombre": "Pc",
                  "unidad": "NIU",
                  "cantidad": 1,
                  "producto_serie": {
                    "id": "f564792c-365f-4022-b2aa-8eb25174e463",
                    "numero_serie": "SN-TEST-001"
                  }
                },
                {
                  "linea_id": "6fc6ce08-7ab2-55c6-9d7e-7831dbe0dd4",
                  "catalog_item_id": "9e2d5841-815a-4616-81af-7106901add50",
                  "nombre": "Pc",
                  "unidad": "NIU",
                  "cantidad": 1,
                  "producto_serie": {
                    "id": "a675803d-4760-5133-c3bb-9fc36285f574",
                    "numero_serie": "SN-TEST-002"
                  }
                }
              ]
            }
        """.trimIndent()

        val mov = gson.fromJson(json, Movimiento::class.java)
        assertEquals(MovimientoTipo.ENTRADA, mov.tipo)
        assertEquals("MOV-0001", mov.numero)
        assertEquals(1780979169524L, mov.fecha)
        assertEquals(2, mov.lineas.size)
        assertEquals(
            listOf("SN-TEST-001", "SN-TEST-002"),
            mov.lineas.mapNotNull { it.serieEnMovimiento() },
        )
    }

    @Test
    fun parseEntradaLegacyIsoString() {
        val json = """
            {
              "id": "legacy",
              "company_ruc": "22222222222",
              "almacen_id": "alm",
              "tipo": "ENTRADA",
              "fecha": "2026-06-08T04:52:33",
              "lineas": []
            }
        """.trimIndent()

        val mov = gson.fromJson(json, Movimiento::class.java)
        assertTrue(mov.fecha > 0L)
    }

    @Test
    fun lineasNullNoRompenListaIngresos() {
        val json = """
            [{
              "id": "test-id",
              "company_ruc": "22222222222",
              "almacen_id": "alm-1",
              "tipo": "ENTRADA",
              "fecha": 1780982625297,
              "numero": "MOV-TEST",
              "lineas": null
            }]
        """.trimIndent()

        val type = object : TypeToken<List<Movimiento>>() {}.type
        val lista = gson.fromJson<List<Movimiento>>(json, type).map { it.sanitizarDesdeApi() }
        assertEquals(1, lista.size)
        assertTrue(lista.first().lineasSeguras.isEmpty())
        lista.first().fechaLegible()
    }
}
