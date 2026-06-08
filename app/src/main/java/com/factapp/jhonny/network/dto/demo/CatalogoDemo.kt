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
        producto(companyRuc, "demo-01", "Arroz premium 1 kg", 4.50, stock = 120.0),
        producto(companyRuc, "demo-02", "Aceite vegetal 1 L", 12.90, stock = 45.0),
        producto(companyRuc, "demo-03", "Gaseosa 500 ml", 2.50, stock = 200.0),
        producto(companyRuc, "demo-04", "Leche evaporada 400 ml", 3.80, stock = 80.0),
        producto(companyRuc, "demo-05", "Pan francés unidad", 0.80, stock = 60.0),
        producto(companyRuc, "demo-06", "Detergente en polvo 1 kg", 9.50, stock = 35.0),
        producto(companyRuc, "demo-07", "Papel higiénico 4 rollos", 6.20, stock = 50.0),
        producto(companyRuc, "demo-08", "Atún en lata 170 g", 5.40, stock = 90.0),
        producto(companyRuc, "demo-09", "Agua mineral 625 ml", 1.50, stock = 300.0),
        producto(companyRuc, "demo-10", "Harina de maíz 1 kg", 5.90, stock = 70.0),
        producto(companyRuc, "demo-11", "Pollo entero (kg)", 11.50, stock = 25.0, unidad = "KGM"),
        productoConSerie(companyRuc, InventarioDemo.ID_PRODUCTO_SERIES, "Producto Series", 199.00, stock = 20.0),
        productoConSerie(companyRuc, "demo-pc", "Laptop Dell 15\"", 2499.00, stock = 2.0),
        productoConSerie(companyRuc, "demo-monitor", "Monitor LG 27\"", 899.00, stock = 3.0),
        productoConSerie(companyRuc, "demo-cel", "Celular Samsung A54", 1299.00, stock = 4.0),
        producto(companyRuc, "demo-12", "Cerveza lata 330 ml", 4.00, stock = 144.0),
        producto(companyRuc, "demo-13", "Café molido 250 g", 18.00, stock = 40.0),
        producto(companyRuc, "demo-14", "Azúcar rubia 1 kg", 4.20, stock = 100.0),
        producto(companyRuc, "demo-15", "Sal de mesa 1 kg", 1.90, stock = 85.0),
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
                "demo-pc" -> item.copy(stockActual = 1.0)
                "demo-monitor" -> item.copy(stockActual = 2.0)
                else -> null
            }
        }
    }

    private fun producto(
        companyRuc: String,
        id: String,
        nombre: String,
        precio: Double,
        stock: Double,
        unidad: String = "NIU",
        descripcion: String? = null,
    ) = CatalogItem(
        id = id,
        companyRuc = companyRuc,
        kind = "PRODUCT",
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
        nombre: String,
        precio: Double,
        stock: Double,
        unidad: String = "NIU",
        descripcion: String? = null,
    ) = producto(
        companyRuc = companyRuc,
        id = id,
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
    ) = CatalogItem(
        id = id,
        companyRuc = companyRuc,
        kind = "SERVICE",
        nombre = nombre,
        unidad = "ZZ",
        precioUnitario = precio,
        afectacionIgv = "10",
        activo = true,
        duracionMinutos = minutos,
    )
}
