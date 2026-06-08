package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.lineaCatalogoConSerie
import com.factapp.jhonny.network.dto.request.RegistrarMovimientoLineaRequest
import kotlin.math.roundToInt

/** Stock e inventario sobre [CatalogItem]. */

val CatalogItem.manejaInventario: Boolean
    get() = esProducto && manejaStock

val CatalogItem.stockDisponible: Double
    get() = stockActual ?: 0.0

fun CatalogItem.etiquetaStock(): String? {
    if (!manejaInventario) return null
    return if (manejaSerie) {
        val n = stockDisponible.roundToInt()
        "Stock: $n ${etiquetaUnidad(unidad, plural = n != 1)} (con serie)"
    } else {
        "Stock: ${formatCantidadConUnidad(stockDisponible, unidad)}"
    }
}

fun CatalogItem.hayStockPara(cantidad: Double): Boolean {
    if (!manejaInventario) return true
    return stockDisponible >= cantidad
}

/** Catálogo: ítem serializable (flag API o nombre tipo "Producto Series"). */
fun CatalogItem.requiereSeriesEnCatalogo(): Boolean =
    manejaSerie || nombre.contains("serie", ignoreCase = true)

fun formatCantidadInventario(cantidad: Double): String =
    if (cantidad == cantidad.roundToInt().toDouble()) {
        cantidad.roundToInt().toString()
    } else {
        "%.2f".format(cantidad)
    }

fun etiquetaUnidad(unidad: String, plural: Boolean = false): String = when (unidad.uppercase()) {
    "NIU" -> if (plural) "unidades" else "unidad"
    "MTR" -> "m"
    "KGM" -> "kg"
    "LTR" -> "L"
    else -> unidad.uppercase()
}

fun formatCantidadConUnidad(cantidad: Double, unidad: String): String {
    val plural = cantidad != 1.0
    return "${formatCantidadInventario(cantidad)} ${etiquetaUnidad(unidad, plural)}"
}

fun CatalogItem.disponibleParaIngreso(): Boolean =
    activo && esProducto && manejaStock

fun CatalogItem.disponibleParaSalida(): Boolean =
    disponibleParaIngreso() && stockDisponible > 0

fun ProductoSerie.aLineaCatalogoInventario(
    cantidad: Double = 1.0,
    catalogItem: CatalogItem? = null,
): LineaCatalogoItem = lineaCatalogoConSerie(this, cantidad, catalogItem)

fun ProductoSerie.aRegistrarMovimientoLineaRequest(
    cantidad: Double = 1.0,
): RegistrarMovimientoLineaRequest = RegistrarMovimientoLineaRequest(
    catalogItemId = catalogItemId,
    cantidad = cantidad,
    serieIds = listOf(id),
)
