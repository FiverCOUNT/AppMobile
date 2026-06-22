package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.CatalogItemKind
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.lineaCatalogoConSerie
import com.factapp.jhonny.network.dto.request.RegistrarMovimientoLineaRequest
import kotlin.math.roundToInt

/** Stock e inventario sobre [CatalogItem]. */

/** Solo productos contados por unidad (NIU) admiten número de serie. */
fun unidadPermiteSerie(unidad: String): Boolean = unidad.uppercase() == "NIU"

val CatalogItem.usaSeriesInventario: Boolean
    get() = manejaSerie && unidadPermiteSerie(unidad)

val CatalogItem.manejaInventario: Boolean
    get() = esProducto && manejaStock

val CatalogItem.stockDisponible: Double
    get() = stockActual ?: 0.0

fun CatalogItem.etiquetaStock(): String? {
    if (!esProducto) return null
    if (!manejaStock && !usaSeriesInventario) return null
    return if (usaSeriesInventario) {
        val n = stockDisponible.roundToInt()
        "Stock: $n ${etiquetaUnidad(unidad, plural = n != 1)} (con serie)"
    } else {
        "Stock: ${formatCantidadConUnidad(stockDisponible, unidad)}"
    }
}

/** Producto con stock reportado por el API (emisión de comprobantes). */
fun CatalogItem.debeValidarStockEnEmision(): Boolean =
    esProducto && (manejaStock || manejaSerie || stockActual != null)

fun CatalogItem.hayStockPara(cantidad: Double): Boolean {
    if (!manejaInventario) return true
    return stockDisponible >= cantidad
}

fun CatalogItem.hayStockParaEmision(cantidad: Double): Boolean {
    if (!debeValidarStockEnEmision()) return true
    return stockDisponible >= cantidad
}

/** Cantidad máxima permitida en una línea (respeta stock del almacén). */
fun CatalogItem.cantidadMaximaEnLinea(): Double =
    if (manejaInventario) stockDisponible.coerceAtLeast(0.0) else Double.MAX_VALUE

fun CatalogItem.cantidadMaximaEnEmision(): Double =
    if (debeValidarStockEnEmision()) stockDisponible.coerceAtLeast(0.0) else Double.MAX_VALUE

fun CatalogItem.coerceCantidadConStock(cantidad: Double): Double {
    if (!manejaInventario) return cantidad.coerceAtLeast(0.0)
    return cantidad.coerceIn(0.0, cantidadMaximaEnLinea())
}

fun CatalogItem.coerceCantidadParaEmision(cantidad: Double): Double {
    if (!debeValidarStockEnEmision()) return cantidad.coerceAtLeast(0.0)
    return cantidad.coerceIn(0.0, cantidadMaximaEnEmision())
}

/** Catálogo: ítem serializable (flag API o nombre tipo "Producto Series"). */
fun CatalogItem.requiereSeriesEnCatalogo(): Boolean =
    usaSeriesInventario || (nombre.contains("serie", ignoreCase = true) && unidadPermiteSerie(unidad))

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

/** Productos que pueden devolverse (activos con stock, sin exigir saldo actual). */
fun CatalogItem.disponibleParaDevolucionCliente(): Boolean =
    activo && esProducto && manejaStock

/** NC devolución de mercadería: solo productos con stock por cantidad o por serie. */
fun CatalogItem.aplicaIngresoDevolucionNotaCredito(): Boolean =
    esProducto && (manejaInventario || usaSeriesInventario)

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

fun LineaCatalogoItem.aplicaIngresoDevolucionNotaCredito(): Boolean {
    catalogItem?.let { return it.aplicaIngresoDevolucionNotaCredito() }
    if (kind?.uppercase() == CatalogItemKind.SERVICE.name) return false
    return manejaStock == true || manejaSerieEfectivo
}
