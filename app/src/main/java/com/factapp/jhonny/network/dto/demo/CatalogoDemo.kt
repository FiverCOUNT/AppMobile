package com.factapp.jhonny.network.dto.demo

import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.CatalogItemKind

/**
 * Catálogo estático de desarrollo (20 ítems).
 * Solo se usa si falla [com.factapp.jhonny.network.CatalogRepository] al llamar al API;
 * no reemplaza la lógica del backend.
 */
object CatalogoDemo {

    fun items(companyRuc: String): List<CatalogItem> = listOf(
        producto(companyRuc, "demo-01", "ARR-001", "Arroz premium 1 kg", 4.50, stock = 120.0),
        producto(companyRuc, "demo-02", "ACE-001", "Aceite vegetal 1 L", 12.90, stock = 45.0),
        producto(companyRuc, "demo-03", "BEB-001", "Gaseosa 500 ml", 2.50, stock = 200.0),
        producto(companyRuc, "demo-04", "LEC-001", "Leche evaporada 400 ml", 3.80, stock = 80.0),
        producto(companyRuc, "demo-05", "PAN-001", "Pan francés unidad", 0.80, stock = 60.0),
        producto(companyRuc, "demo-06", "DET-001", "Detergente en polvo 1 kg", 9.50, stock = 35.0),
        producto(companyRuc, "demo-07", "PAP-001", "Papel higiénico 4 rollos", 6.20, stock = 50.0),
        producto(companyRuc, "demo-08", "ATU-001", "Atún en lata 170 g", 5.40, stock = 90.0),
        producto(companyRuc, "demo-09", "AGU-001", "Agua mineral 625 ml", 1.50, stock = 300.0),
        producto(companyRuc, "demo-10", "HAR-001", "Harina de maíz 1 kg", 5.90, stock = 70.0),
        producto(companyRuc, "demo-11", "POL-001", "Pollo entero (kg)", 11.50, stock = 25.0, unidad = "KGM"),
        productoConSerie(companyRuc, "demo-pc", "PC-001", "Laptop Dell 15\"", 2499.00, stock = 2.0),
        producto(companyRuc, "demo-12", "CER-001", "Cerveza lata 330 ml", 4.00, stock = 144.0),
        producto(companyRuc, "demo-13", "CAF-001", "Café molido 250 g", 18.00, stock = 40.0),
        producto(companyRuc, "demo-14", "AZU-001", "Azúcar rubia 1 kg", 4.20, stock = 100.0),
        producto(companyRuc, "demo-15", "SAL-001", "Sal de mesa 1 kg", 1.90, stock = 85.0),
        servicio(companyRuc, "demo-16", "Asesoría contable mensual", 150.0, minutos = 60),
        servicio(companyRuc, "demo-17", "Diseño de logo corporativo", 350.0, minutos = 480),
        servicio(companyRuc, "demo-18", "Mantenimiento de PC", 80.0, minutos = 90),
        servicio(companyRuc, "demo-19", "Flete urbano (hasta 10 km)", 25.0, minutos = 45),
        servicio(companyRuc, "demo-20", "Capacitación facturación SUNAT", 200.0, minutos = 120),
    )

    /** Stock distinto por bodega en demo (simula `GET catalogo?almacen_id=`). */
    fun itemsPorAlmacen(companyRuc: String, almacenId: String): List<CatalogItem> {
        val centralId = InventarioDemo.almacenPrincipal(companyRuc).id
        if (almacenId == centralId) return items(companyRuc)
        return items(companyRuc).mapNotNull { item ->
            if (!item.esProducto || !item.manejaStock) return@mapNotNull null
            when (item.id) {
                "demo-03" -> item.copy(stockActual = 48.0)
                "demo-05" -> item.copy(stockActual = 15.0)
                "demo-09" -> item.copy(stockActual = 60.0)
                "demo-14" -> item.copy(stockActual = 20.0)
                else -> null
            }
        }
    }

    private fun producto(
        companyRuc: String,
        id: String,
        codigo: String,
        nombre: String,
        precio: Double,
        stock: Double,
        unidad: String = "NIU",
        descripcion: String? = null,
    ) = CatalogItem(
        id = id,
        companyRuc = companyRuc,
        kind = "PRODUCT",
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        unidad = unidad,
        precioUnitario = precio,
        afectacionIgv = "10",
        activo = true,
        manejaStock = true,
        manejaSerie = false,
        stockActual = stock,
    )

    private fun productoConSerie(
        companyRuc: String,
        id: String,
        codigo: String,
        nombre: String,
        precio: Double,
        stock: Double,
        unidad: String = "NIU",
        descripcion: String? = null,
    ) = producto(
        companyRuc = companyRuc,
        id = id,
        codigo = codigo,
        nombre = nombre,
        precio = precio,
        stock = stock,
        unidad = unidad,
        descripcion = descripcion,
    ).copy(manejaSerie = true)

    private fun servicio(
        companyRuc: String,
        id: String,
        nombre: String,
        precio: Double,
        minutos: Int,
        codigo: String? = null,
    ) = CatalogItem(
        id = id,
        companyRuc = companyRuc,
        kind = "SERVICE",
        codigo = codigo,
        nombre = nombre,
        unidad = "ZZ",
        precioUnitario = precio,
        afectacionIgv = "10",
        activo = true,
        duracionMinutos = minutos,
    )
}
