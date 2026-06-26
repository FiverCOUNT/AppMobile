package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.network.dto.cantidadMaximaEnEmision
import com.factapp.jhonny.network.dto.coerceCantidadParaEmision
import com.factapp.jhonny.network.dto.debeValidarStockEnEmision
import com.factapp.jhonny.network.dto.hayStockPara
import com.factapp.jhonny.network.dto.hayStockParaEmision
import com.factapp.jhonny.network.dto.manejaInventario
import com.factapp.jhonny.network.dto.unidadPermiteSerie
import com.factapp.jhonny.network.dto.usaSeriesInventario
import com.factapp.jhonny.network.dto.request.EmitirLineaRequest
import com.factapp.jhonny.network.dto.request.RegistrarMovimientoLineaRequest
import com.google.gson.annotations.SerializedName
import java.util.UUID
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Renglón de operación sobre catálogo (emisión, ingreso, salida, kardex).
 *
 * Guarda un **snapshot** de los datos del [CatalogItem] que pueden cambiar después
 * (nombre, precio, unidad, flags de stock/serie). La referencia viva al catálogo
 * es opcional ([catalogItem]) solo para UI en pantalla.
 */
data class LineaCatalogoItem(
    @SerializedName("linea_id")
    val lineaId: String = UUID.randomUUID().toString(),
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("catalog_item_id")
    val catalogItemId: String = "",
    /** ID de la línea original en sale_details (solo NC sobre documento afectado). */
    @SerializedName("sale_detail_id")
    val saleDetailId: String? = null,
    /** Snapshot — nombre al momento de la operación. */
    val nombre: String? = null,
    val codigo: String? = null,
    val descripcion: String? = null,
    val unidad: String? = null,
    @SerializedName("precio_unitario")
    val precioUnitario: Double? = null,
    /** Precio unitario de la factura original (solo NC en pantalla). */
    @SerializedName("precio_original_referencia")
    val precioOriginalReferencia: Double? = null,
    /** Cantidad máxima acreditable según la factura original (solo NC en pantalla). */
    @SerializedName("cantidad_original_referencia")
    val cantidadOriginalReferencia: Double? = null,
    @SerializedName("afectacion_igv")
    val afectacionIgv: String? = null,
    val kind: String? = null,
    @SerializedName("maneja_stock")
    val manejaStock: Boolean? = null,
    @SerializedName("maneja_serie")
    val manejaSerie: Boolean? = null,
    val cantidad: Double = 0.0,
    @SerializedName("almacen_id")
    val almacenId: String? = null,
    @SerializedName(value = "producto_serie", alternate = ["serie"])
    val productoSerie: ProductoSerie? = null,
    /** Referencia viva al catálogo (solo pantalla; no viene del API). */
    @SerializedName("catalog_item_ui")
    val catalogItem: CatalogItem? = null,
) {
    val nombreEfectivo: String
        get() = nombre?.takeIf { it.isNotBlank() }
            ?: descripcion?.takeIf { it.isNotBlank() }
            ?: catalogItem?.nombre
            ?: catalogItemId.takeIf { it.isNotBlank() }
            ?: "Producto"

    val unidadEfectiva: String
        get() = unidad ?: catalogItem?.unidad ?: "NIU"

    val precioUnitarioEfectivo: Double
        get() = precioUnitario ?: catalogItem?.precioUnitario ?: 0.0

    val afectacionIgvEfectiva: String
        get() = afectacionIgv ?: catalogItem?.afectacionIgv ?: "10"

    val manejaSerieEfectivo: Boolean
        get() = manejaSerie ?: catalogItem?.manejaSerie == true

    val manejaInventarioEfectivo: Boolean
        get() = when {
            manejaStock != null && kind != null ->
                kind.uppercase() == CatalogItemKind.PRODUCT.name && manejaStock
            catalogItem != null -> catalogItem.manejaInventario
            else -> kind?.uppercase() == CatalogItemKind.PRODUCT.name && manejaStock == true
        }

    /** Precio de catálogo incluye IGV (afectación 10). Base imponible por unidad. */
    val valorUnitarioSinIgv: Double
        get() = if (afectacionIgvEfectiva == "10") {
            round4(precioUnitarioEfectivo / (1 + IGV_RATE))
        } else {
            precioUnitarioEfectivo
        }

    /** Base imponible de la línea (sin IGV). */
    val subtotal: Double
        get() = round4(valorUnitarioSinIgv * cantidad)

    val igv: Double
        get() = if (afectacionIgvEfectiva == "10") round4(subtotal * IGV_RATE) else 0.0

    /** Total con IGV — coincide con precioUnitario × cantidad en afectación 10. */
    val total: Double
        get() = round4(subtotal + igv)

    val numeroSerieEfectivo: String?
        get() = productoSerie?.numeroSerie?.takeIf { it.isNotBlank() }

    val almacenIdEfectivo: String?
        get() = almacenId ?: productoSerie?.almacenId

    val tieneSerie: Boolean
        get() = productoSerie != null

    val tieneSeries: Boolean
        get() = manejaSerieEfectivo || productoSerie != null
}

fun CatalogItem.aLineaCatalogoItem(
    cantidad: Double = 1.0,
    almacenId: String? = null,
    productoSerie: ProductoSerie? = null,
): LineaCatalogoItem = LineaCatalogoItem(
    catalogItemId = id,
    nombre = nombre,
    codigo = codigo,
    descripcion = descripcion,
    unidad = unidad,
    precioUnitario = precioUnitario,
    afectacionIgv = afectacionIgv,
    kind = kind,
    manejaStock = manejaStock,
    manejaSerie = manejaSerie,
    cantidad = cantidad,
    almacenId = almacenId,
    productoSerie = productoSerie,
    catalogItem = this,
)

/** @see aLineaCatalogoItem */
@Deprecated("Usa aLineaCatalogoItem", ReplaceWith("aLineaCatalogoItem(cantidad, almacenId, productoSerie)"))
fun CatalogItem.aLineaCatalogo(
    cantidad: Double = 1.0,
    productoSerie: ProductoSerie? = null,
): LineaCatalogoItem = aLineaCatalogoItem(cantidad = cantidad, productoSerie = productoSerie)

fun lineaCatalogoSinSerie(
    catalogItemId: String,
    cantidad: Double,
    catalogItem: CatalogItem? = null,
): LineaCatalogoItem = catalogItem?.aLineaCatalogoItem(cantidad = cantidad)
    ?: LineaCatalogoItem(catalogItemId = catalogItemId, cantidad = cantidad)

fun lineaCatalogoConSerie(
    serie: ProductoSerie,
    cantidad: Double = 1.0,
    catalogItem: CatalogItem? = null,
): LineaCatalogoItem = LineaCatalogoItem(
    cantidad = cantidad.coerceAtMost(1.0),
    productoSerie = serie,
    catalogItemId = serie.catalogItemId,
    catalogItem = catalogItem?.takeIf { it.id == serie.catalogItemId } ?: catalogItem,
    nombre = catalogItem?.nombre,
    descripcion = catalogItem?.descripcion,
    unidad = catalogItem?.unidad,
    precioUnitario = catalogItem?.precioUnitario,
    afectacionIgv = catalogItem?.afectacionIgv,
    kind = catalogItem?.kind,
    manejaStock = catalogItem?.manejaStock,
    manejaSerie = catalogItem?.manejaSerie ?: true,
    almacenId = serie.almacenId,
)

fun LineaCatalogoItem.resumenSerie(): String? = productoSerie?.numeroSerie

/** @see resumenSerie */
fun LineaCatalogoItem.resumenSeries(): String? = resumenSerie()

fun LineaCatalogoItem.serieEnMovimiento(): String? = productoSerie?.numeroSerie?.takeIf { it.isNotBlank() }

/** @see serieEnMovimiento */
fun LineaCatalogoItem.seriesEnMovimiento(): List<String>? =
    serieEnMovimiento()?.let { listOf(it) }

@Deprecated("Usa LineaCatalogoItem", ReplaceWith("LineaCatalogoItem"))
typealias LineaCatalogo = LineaCatalogoItem

private const val IGV_RATE = 0.18

private fun round4(value: Double): Double = round(value * 10000.0) / 10000.0

fun LineaCatalogoItem.requireItem(): CatalogItem =
    requireNotNull(catalogItem) { "catalogItem requerido en esta operación" }

fun LineaCatalogoItem.itemParaEmision(catalogo: List<CatalogItem>): CatalogItem =
    catalogo.find { it.id == catalogItemId } ?: requireItem()

val LineaCatalogoItem.requiereSeries: Boolean
    get() = catalogItem?.usaSeriesInventario
        ?: (manejaSerieEfectivo && unidadPermiteSerie(unidadEfectiva))

fun LineaCatalogoItem.seriesValidas(): Boolean {
    if (!requiereSeries) return true
    return productoSerie != null && cantidad == 1.0
}

fun LineaCatalogoItem.seriesEnAlmacen(): Boolean {
    if (productoSerie == null) return true
    val alm = almacenIdEfectivo ?: return true
    return productoSerie.almacenId == null || productoSerie.almacenId == alm
}

fun LineaCatalogoItem.seriesDisponiblesParaEmision(): Boolean {
    if (!requiereSeries) return true
    return productoSerie != null && seriesValidas()
}

/** Marca el almacén de salida en cada línea antes de emitir (no altera el almacén real de cada serie). */
fun List<LineaCatalogoItem>.prepararLineasParaEmitir(almacenSalidaId: String): List<LineaCatalogoItem> {
    if (almacenSalidaId.isBlank()) return this
    return map { linea -> linea.copy(almacenId = almacenSalidaId) }
}

fun LineaCatalogoItem.aEmitirLinea(incluirPrecio: Boolean = false): EmitirLineaRequest {
    val serie = productoSerie?.takeIf {
        it.id.isNotBlank() && it.id != "temp" && !it.id.startsWith("scan-")
    }
    return EmitirLineaRequest(
        catalogItemId = catalogItemId,
        cantidad = if (serie != null) 1.0 else cantidad,
        precioUnitario = if (incluirPrecio) precioUnitarioEfectivo.takeIf { it > 0 } else null,
        saleDetailId = saleDetailId?.takeIf { it.isNotBlank() },
        productoSerieId = serie?.id,
        numeroSerie = serie?.numeroSerie?.takeIf { it.isNotBlank() },
    )
}

fun LineaCatalogoItem.numeroSerieLimpio(): String? {
    if (!requiereSeries) return null
    return productoSerie?.numeroSerie?.trim()?.takeIf { it.isNotEmpty() }
}

fun LineaCatalogoItem.listaParaEmitir(): Boolean {
    val item = catalogItem
    val seriesOk = if (requiereSeries) seriesDisponiblesParaEmision() else seriesValidas()
    if (item != null) {
        return cantidad > 0 &&
            (!item.debeValidarStockEnEmision() || item.hayStockParaEmision(cantidad)) &&
            seriesOk
    }
    return cantidad > 0 && seriesOk
}

/** Notas de crédito/débito: no exigen stock de almacén ni series de inventario. */
fun LineaCatalogoItem.listaParaEmitirNotaFiscal(): Boolean = cantidad > 0

fun List<LineaCatalogoItem>.agregarDesdeCatalogo(
    item: CatalogItem,
    almacenId: String? = null,
): List<LineaCatalogoItem> {
    if (item.usaSeriesInventario) {
        if (item.debeValidarStockEnEmision() && !item.hayStockParaEmision(1.0)) return this
        return this + item.aLineaCatalogoItem(cantidad = 1.0, almacenId = almacenId)
    }
    val existente = find {
        it.catalogItemId == item.id && it.almacenId == almacenId && !it.requiereSeries
    }
    return if (existente != null) {
        val nuevaCantidad = existente.cantidad + 1.0
        if (item.debeValidarStockEnEmision() && !item.hayStockParaEmision(nuevaCantidad)) return this
        map { linea ->
            if (linea.lineaId == existente.lineaId) {
                linea.copy(cantidad = nuevaCantidad)
            } else {
                linea
            }
        }
    } else {
        if (item.debeValidarStockEnEmision() && !item.hayStockParaEmision(1.0)) return this
        this + item.aLineaCatalogoItem(cantidad = 1.0, almacenId = almacenId)
    }
}

fun List<LineaCatalogoItem>.agregarDesdeCatalogoSiHayStock(
    item: CatalogItem,
    almacenId: String? = null,
): Pair<List<LineaCatalogoItem>, Boolean> {
    val cantidadAntes = find { it.catalogItemId == item.id && it.almacenId == almacenId }?.cantidad ?: 0.0
    val despues = agregarDesdeCatalogo(item, almacenId)
    val cantidadDespues = despues.find { it.catalogItemId == item.id && it.almacenId == almacenId }?.cantidad ?: 0.0
    return despues to (cantidadDespues > cantidadAntes)
}

fun List<LineaCatalogoItem>.actualizarPrecioAcreditar(
    lineaId: String,
    precioConIgv: Double,
    limitarAlOriginal: Boolean = true,
): List<LineaCatalogoItem> =
    map { linea ->
        if (linea.lineaId != lineaId) return@map linea
        val ajustado = if (limitarAlOriginal) {
            val max = linea.precioOriginalReferencia ?: linea.precioUnitarioEfectivo
            precioConIgv.coerceIn(0.0, max)
        } else {
            precioConIgv.coerceAtLeast(0.0)
        }
        linea.copy(precioUnitario = ajustado)
    }

fun List<LineaCatalogoItem>.actualizarCantidad(lineaId: String, cantidad: Double): List<LineaCatalogoItem> {
    if (cantidad <= 0) return filter { it.lineaId != lineaId }
    return map { linea ->
        if (linea.lineaId != lineaId) return@map linea
        val item = linea.catalogItem
        val cantidadAjustada = item?.coerceCantidadParaEmision(cantidad) ?: cantidad
        if (!linea.requiereSeries) return@map linea.copy(cantidad = cantidadAjustada)
        return@map linea.copy(cantidad = 1.0)
    }
}

fun List<LineaCatalogoItem>.actualizarProductoSerie(
    lineaId: String,
    serie: ProductoSerie?,
): List<LineaCatalogoItem> = map { linea ->
    if (linea.lineaId != lineaId) linea
    else linea.copy(productoSerie = serie, cantidad = if (serie != null) 1.0 else linea.cantidad)
}

fun List<LineaCatalogoItem>.actualizarSeriesInventario(
    catalogItemId: String,
    almacenId: String?,
    seleccionadas: List<ProductoSerie>,
): List<LineaCatalogoItem> {
    val sinEste = filter { it.catalogItemId != catalogItemId }
    val item = find { it.catalogItemId == catalogItemId }?.catalogItem
    val nuevas = seleccionadas.map { serie ->
        lineaCatalogoConSerie(serie, cantidad = 1.0, catalogItem = item).copy(
            almacenId = almacenId ?: serie.almacenId,
            lineaId = UUID.randomUUID().toString(),
        )
    }
    return sinEste + nuevas
}

fun List<LineaCatalogoItem>.eliminarLinea(lineaId: String): List<LineaCatalogoItem> =
    filter { it.lineaId != lineaId }

fun List<LineaCatalogoItem>.eliminarPorCatalogo(catalogItemId: String): List<LineaCatalogoItem> =
    filter { it.catalogItemId != catalogItemId }

fun List<LineaCatalogoItem>.actualizarAlmacen(almacenId: String?): List<LineaCatalogoItem> =
    map { it.copy(almacenId = almacenId, productoSerie = null, cantidad = if (it.requiereSeries) 0.0 else it.cantidad) }

data class TotalesComprobante(
    val subtotal: Double,
    val igv: Double,
    val total: Double,
)

fun List<LineaCatalogoItem>.calcularTotales(): TotalesComprobante {
    val subtotal = round4(sumOf { it.subtotal })
    val igv = round4(sumOf { it.igv })
    val total = round4(sumOf { it.total })
    return TotalesComprobante(subtotal = subtotal, igv = igv, total = total)
}

fun List<LineaCatalogoItem>.lineasListasParaEmitir(): Boolean =
    isNotEmpty() && all { it.listaParaEmitir() }

fun List<LineaCatalogoItem>.lineasListasParaEmitirNotaFiscal(): Boolean =
    isNotEmpty() && all { it.listaParaEmitirNotaFiscal() }

fun List<LineaCatalogoItem>.lineasListasParaSalida(): Boolean =
    isNotEmpty() && all { linea ->
        val item = linea.catalogItem ?: return@all linea.cantidad > 0 && !linea.requiereSeries
        val almacenOk = !item.manejaInventario || !linea.almacenIdEfectivo.isNullOrBlank()
        if (!almacenOk) return@all false
        if (item.usaSeriesInventario) {
            linea.productoSerie != null && linea.cantidad == 1.0 && linea.seriesEnAlmacen()
        } else {
            linea.cantidad > 0 && item.hayStockParaEmision(linea.cantidad)
        }
    }

fun List<LineaCatalogoItem>.lineasListasParaIngreso(): Boolean =
    isNotEmpty() && all { linea ->
        val item = linea.catalogItem ?: return@all linea.cantidad > 0
        val almacenOk = !item.manejaInventario || !linea.almacenId.isNullOrBlank()
        if (!almacenOk) return@all false
        if (item.usaSeriesInventario) {
            linea.productoSerie != null && linea.cantidad == 1.0
        } else {
            linea.cantidad > 0
        }
    }

fun List<LineaCatalogoItem>.aEmitirLineas(incluirPrecio: Boolean = false): List<EmitirLineaRequest> =
    map { it.aEmitirLinea(incluirPrecio = incluirPrecio) }

fun LineaCatalogoItem.aRegistrarSalidaLinea(): RegistrarMovimientoLineaRequest =
    aRegistrarMovimientoLinea()

fun List<LineaCatalogoItem>.aRegistrarSalidaLineas(): List<RegistrarMovimientoLineaRequest> =
    map { it.aRegistrarSalidaLinea() }

fun LineaCatalogoItem.aRegistrarMovimientoLinea(): RegistrarMovimientoLineaRequest {
    if (!requiereSeries) {
        return RegistrarMovimientoLineaRequest(
            catalogItemId = catalogItemId,
            cantidad = cantidad,
        )
    }
    val serie = productoSerie
    return RegistrarMovimientoLineaRequest(
        catalogItemId = catalogItemId,
        cantidad = 1.0,
        productoSerieId = serie?.id?.takeIf { id ->
            id.isNotBlank() && !id.startsWith("scan-") && id != "temp"
        },
        numeroSerie = serie?.numeroSerie?.trim()?.takeIf { it.isNotEmpty() },
    )
}

fun LineaCatalogoItem.enriquecerConCatalogo(
    catalogItem: CatalogItem,
    almacenId: String? = null,
    @Suppress("UNUSED_PARAMETER") seriesPorId: (String) -> ProductoSerie? = { null },
): LineaCatalogoItem {
    val base = copy(
        catalogItem = catalogItem,
        catalogItemId = catalogItem.id,
        nombre = nombre ?: catalogItem.nombre,
        descripcion = descripcion ?: catalogItem.descripcion,
        unidad = unidad ?: catalogItem.unidad,
        precioUnitario = precioUnitario ?: catalogItem.precioUnitario,
        afectacionIgv = afectacionIgv ?: catalogItem.afectacionIgv,
        kind = kind ?: catalogItem.kind,
        manejaStock = manejaStock ?: catalogItem.manejaStock,
        manejaSerie = manejaSerie ?: catalogItem.manejaSerie,
        almacenId = almacenId ?: this.almacenId,
    )
    return if (productoSerie != null) {
        base.copy(almacenId = almacenId ?: productoSerie.almacenId)
    } else {
        base
    }
}

/** Convierte a línea de [Invoice] al emitir o persistir comprobante. */
fun LineaCatalogoItem.toSaleDetail(invoiceId: String? = null): SaleDetail {
    val pct = if (afectacionIgvEfectiva == "10") 18.0 else 0.0
    val valorVenta = subtotal
    val igvLinea = igv
    val totalLinea = total
    return SaleDetail(
        invoiceId = invoiceId,
        catalogItemId = catalogItemId,
        descripcion = nombreEfectivo,
        nombre = nombre,
        cantidad = cantidad,
        unidad = unidadEfectiva,
        mtoPrecioUnitario = precioUnitarioEfectivo,
        tipAfeIgv = afectacionIgvEfectiva,
        mtoValorVenta = valorVenta,
        mtoIgv = igvLinea,
        totalFactura = totalLinea,
        mtoValorUnitario = valorUnitarioSinIgv,
        mtoBaseIgv = valorVenta,
        porcentajeIgv = pct,
        productoSerie = productoSerie,
    )
}

fun List<LineaCatalogoItem>.toSaleDetails(invoiceId: String? = null): List<SaleDetail> =
    map { it.toSaleDetail(invoiceId) }
