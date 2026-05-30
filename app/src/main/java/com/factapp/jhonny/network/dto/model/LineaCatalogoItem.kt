package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.network.dto.hayStockPara
import com.factapp.jhonny.network.dto.manejaInventario
import com.factapp.jhonny.network.dto.request.EmitirLineaRequest
import com.factapp.jhonny.network.dto.request.RegistrarMovimientoLineaRequest
import com.google.gson.annotations.SerializedName
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Renglón de operación sobre catálogo (emisión, ingreso, salida, kardex).
 *
 * Guarda un **snapshot** de los datos del [CatalogItem] que pueden cambiar después
 * (nombre, precio, unidad, flags de stock/serie). La referencia viva al catálogo
 * es opcional ([catalogItem]) solo para UI en pantalla.
 */
data class LineaCatalogoItem(
    val lineaId: String = UUID.randomUUID().toString(),
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    /** Snapshot — nombre al momento de la operación. */
    val nombre: String? = null,
    val codigo: String? = null,
    val descripcion: String? = null,
    val unidad: String? = null,
    @SerializedName("precio_unitario")
    val precioUnitario: Double? = null,
    @SerializedName("afectacion_igv")
    val afectacionIgv: String? = null,
    val kind: String? = null,
    @SerializedName("maneja_stock")
    val manejaStock: Boolean? = null,
    @SerializedName("maneja_serie")
    val manejaSerie: Boolean? = null,
    val cantidad: Double,
    @SerializedName("almacen_id")
    val almacenId: String? = null,
    @SerializedName(value = "producto_serie", alternate = ["serie"])
    val productoSerie: ProductoSerie? = null,
    @SerializedName("series")
    internal val numerosSerieApi: List<String>? = null,
    @SerializedName("serie_ids")
    val serieIds: List<String>? = null,
    val series: List<ProductoSerie> = emptyList(),
    val numerosSerie: List<String> = emptyList(),
    /** Referencia viva al catálogo (solo pantalla; no suele venir del API). */
    val catalogItem: CatalogItem? = null,
) {
    val nombreEfectivo: String
        get() = nombre?.takeIf { it.isNotBlank() }
            ?: descripcion?.takeIf { it.isNotBlank() }
            ?: catalogItem?.nombre
            ?: catalogItemId

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

    val subtotal: Double
        get() = precioUnitarioEfectivo * cantidad

    val igv: Double
        get() = if (afectacionIgvEfectiva == "10") subtotal * IGV_RATE else 0.0

    val total: Double
        get() = subtotal + igv

    val numerosSerieUi: List<String>
        get() = when {
            numerosSerie.isNotEmpty() -> numerosSerie
            !numerosSerieApi.isNullOrEmpty() -> numerosSerieApi
            series.isNotEmpty() -> series.map { it.numeroSerie }
            productoSerie != null -> listOf(productoSerie.numeroSerie)
            else -> emptyList()
        }

    val seriesEfectivas: List<ProductoSerie>
        get() = when {
            series.isNotEmpty() -> series
            productoSerie != null -> listOf(productoSerie)
            else -> emptyList()
        }

    val almacenIdEfectivo: String?
        get() = almacenId ?: seriesEfectivas.firstOrNull()?.almacenId

    val tieneSeries: Boolean
        get() = productoSerie != null ||
            series.isNotEmpty() ||
            numerosSerie.isNotEmpty() ||
            !numerosSerieApi.isNullOrEmpty() ||
            !serieIds.isNullOrEmpty()
}

fun CatalogItem.aLineaCatalogoItem(
    cantidad: Double = 1.0,
    almacenId: String? = null,
    series: List<ProductoSerie> = emptyList(),
    numerosSerie: List<String> = emptyList(),
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
    series = series,
    numerosSerie = numerosSerie,
    catalogItem = this,
)

/** @see aLineaCatalogoItem */
@Deprecated("Usa aLineaCatalogoItem", ReplaceWith("aLineaCatalogoItem(cantidad, almacenId, series, numerosSerie)"))
fun CatalogItem.aLineaCatalogo(
    cantidad: Double = 1.0,
    series: List<ProductoSerie> = emptyList(),
    numerosSerie: List<String> = emptyList(),
): LineaCatalogoItem = aLineaCatalogoItem(cantidad = cantidad, series = series, numerosSerie = numerosSerie)

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
    cantidad = cantidad,
    productoSerie = serie,
    series = listOf(serie),
    catalogItemId = serie.catalogItemId,
    catalogItem = catalogItem?.takeIf { it.id == serie.catalogItemId }
        ?: catalogItem,
    nombre = catalogItem?.nombre,
    codigo = catalogItem?.codigo,
    descripcion = catalogItem?.descripcion,
    unidad = catalogItem?.unidad,
    precioUnitario = catalogItem?.precioUnitario,
    afectacionIgv = catalogItem?.afectacionIgv,
    kind = catalogItem?.kind,
    manejaStock = catalogItem?.manejaStock,
    manejaSerie = catalogItem?.manejaSerie ?: true,
)

fun lineaCatalogoConNumerosSerie(
    catalogItemId: String,
    numerosSerie: List<String>,
    catalogItem: CatalogItem? = null,
): LineaCatalogoItem = catalogItem?.aLineaCatalogoItem(
    cantidad = numerosSerie.size.toDouble(),
    numerosSerie = numerosSerie,
) ?: LineaCatalogoItem(
    catalogItemId = catalogItemId,
    cantidad = numerosSerie.size.toDouble(),
    numerosSerie = numerosSerie,
    manejaSerie = true,
)

fun lineaCatalogoConSerieIds(
    catalogItemId: String,
    serieIds: List<String>,
    catalogItem: CatalogItem? = null,
): LineaCatalogoItem = catalogItem?.aLineaCatalogoItem(
    cantidad = serieIds.size.toDouble(),
) ?: LineaCatalogoItem(
    catalogItemId = catalogItemId,
    cantidad = serieIds.size.toDouble(),
    serieIds = serieIds,
)

fun LineaCatalogoItem.resumenSeries(): String? = when {
    productoSerie != null -> productoSerie.numeroSerie
    numerosSerieUi.isNotEmpty() -> numerosSerieUi.joinToString(", ")
    !serieIds.isNullOrEmpty() -> "${serieIds.size} serie(s)"
    else -> null
}

fun LineaCatalogoItem.seriesEnMovimiento(): List<String>? {
    numerosSerieUi.takeIf { it.isNotEmpty() }?.let { return it }
    resumenSeries()?.let { resumen ->
        return if (resumen.contains(",")) {
            resumen.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf(resumen.trim())
        }
    }
    return null
}

@Deprecated("Usa LineaCatalogoItem", ReplaceWith("LineaCatalogoItem"))
typealias LineaCatalogo = LineaCatalogoItem

private const val IGV_RATE = 0.18

fun LineaCatalogoItem.requireItem(): CatalogItem =
    requireNotNull(catalogItem) { "catalogItem requerido en esta operación" }

val LineaCatalogoItem.requiereSeries: Boolean
    get() = manejaSerieEfectivo

fun LineaCatalogoItem.seriesValidas(): Boolean {
    if (!requiereSeries) return true
    if (series.isNotEmpty()) {
        return series.size == cantidad.roundToInt() &&
            cantidad == cantidad.roundToInt().toDouble()
    }
    val lista = numerosSerie.map { it.trim() }.filter { it.isNotEmpty() }
    return lista.size == cantidad.roundToInt() &&
        cantidad == cantidad.roundToInt().toDouble()
}

fun LineaCatalogoItem.seriesEnAlmacen(): Boolean {
    if (series.isEmpty()) return true
    val alm = almacenIdEfectivo ?: return true
    return series.all { it.almacenId == null || it.almacenId == alm }
}

fun LineaCatalogoItem.numerosSerieLimpios(): List<String>? {
    if (!requiereSeries) return null
    return numerosSerieUi.map { it.trim() }.filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }
}

fun LineaCatalogoItem.listaParaEmitir(): Boolean {
    val item = catalogItem
    if (item != null) {
        return cantidad > 0 &&
            (!item.manejaInventario || item.hayStockPara(cantidad)) &&
            seriesValidas()
    }
    return cantidad > 0 && seriesValidas()
}

fun List<LineaCatalogoItem>.agregarDesdeCatalogo(
    item: CatalogItem,
    almacenId: String? = null,
): List<LineaCatalogoItem> {
    if (item.manejaSerie) {
        return this + item.aLineaCatalogoItem(
            cantidad = 1.0,
            almacenId = almacenId,
            numerosSerie = listOf(""),
        )
    }
    val existente = find { it.catalogItemId == item.id && it.almacenId == almacenId }
    return if (existente != null) {
        map { linea ->
            if (linea.lineaId == existente.lineaId) {
                linea.copy(cantidad = linea.cantidad + 1.0)
            } else {
                linea
            }
        }
    } else {
        this + item.aLineaCatalogoItem(cantidad = 1.0, almacenId = almacenId)
    }
}

fun List<LineaCatalogoItem>.actualizarCantidad(lineaId: String, cantidad: Double): List<LineaCatalogoItem> {
    if (cantidad <= 0) return filter { it.lineaId != lineaId }
    return map { linea ->
        if (linea.lineaId != lineaId) return@map linea
        if (!linea.requiereSeries) return@map linea.copy(cantidad = cantidad)
        val n = cantidad.roundToInt()
        if (n <= 0) return@map linea
        val actuales = linea.numerosSerieUi
        val nuevas = when {
            actuales.size < n -> actuales + List(n - actuales.size) { "" }
            actuales.size > n -> actuales.take(n)
            else -> actuales
        }
        linea.copy(cantidad = n.toDouble(), numerosSerie = nuevas, series = emptyList())
    }
}

fun List<LineaCatalogoItem>.actualizarSeries(lineaId: String, series: List<String>): List<LineaCatalogoItem> =
    map { linea ->
        if (linea.lineaId == lineaId) {
            val limpias = series.map { it.trim() }
            linea.copy(
                cantidad = limpias.size.coerceAtLeast(1).toDouble(),
                numerosSerie = limpias,
                series = emptyList(),
            )
        } else {
            linea
        }
    }

fun List<LineaCatalogoItem>.actualizarSerieEnIndice(
    lineaId: String,
    indice: Int,
    numeroSerie: String,
): List<LineaCatalogoItem> = map { linea ->
    if (linea.lineaId != lineaId) return@map linea
    val base = linea.numerosSerieUi.toMutableList()
    while (base.size <= indice) base.add("")
    base[indice] = numeroSerie
    linea.copy(numerosSerie = base, series = emptyList(), cantidad = base.size.toDouble())
}

fun List<LineaCatalogoItem>.actualizarSeriesInventario(
    catalogItemId: String,
    almacenId: String?,
    seleccionadas: List<ProductoSerie>,
): List<LineaCatalogoItem> = map { linea ->
    if (linea.catalogItemId == catalogItemId) {
        linea.copy(
            series = seleccionadas,
            numerosSerie = emptyList(),
            almacenId = almacenId ?: seleccionadas.firstOrNull()?.almacenId ?: linea.almacenId,
            cantidad = seleccionadas.size.toDouble(),
        )
    } else {
        linea
    }
}

fun List<LineaCatalogoItem>.eliminarLinea(lineaId: String): List<LineaCatalogoItem> =
    filter { it.lineaId != lineaId }

fun List<LineaCatalogoItem>.eliminarPorCatalogo(catalogItemId: String): List<LineaCatalogoItem> =
    filter { it.catalogItemId != catalogItemId }

fun List<LineaCatalogoItem>.actualizarAlmacen(almacenId: String?): List<LineaCatalogoItem> =
    map { it.copy(almacenId = almacenId, series = emptyList(), cantidad = if (it.requiereSeries) 0.0 else it.cantidad) }

data class TotalesComprobante(
    val subtotal: Double,
    val igv: Double,
    val total: Double,
)

fun List<LineaCatalogoItem>.calcularTotales(): TotalesComprobante {
    val subtotal = sumOf { it.subtotal }
    val igv = sumOf { it.igv }
    return TotalesComprobante(subtotal = subtotal, igv = igv, total = subtotal + igv)
}

fun List<LineaCatalogoItem>.lineasListasParaEmitir(): Boolean =
    isNotEmpty() && all { it.listaParaEmitir() }

fun List<LineaCatalogoItem>.lineasListasParaSalida(): Boolean =
    isNotEmpty() && all { linea ->
        val item = linea.catalogItem ?: return@all linea.cantidad > 0 && !linea.requiereSeries
        val almacenOk = !item.manejaInventario || !linea.almacenIdEfectivo.isNullOrBlank()
        if (!almacenOk) return@all false
        if (item.manejaSerie) {
            linea.series.isNotEmpty() &&
                linea.cantidad == linea.series.size.toDouble() &&
                linea.seriesEnAlmacen()
        } else {
            linea.cantidad > 0 && item.hayStockPara(linea.cantidad)
        }
    }

fun List<LineaCatalogoItem>.lineasListasParaIngreso(): Boolean =
    isNotEmpty() && all { linea ->
        val item = linea.catalogItem ?: return@all linea.cantidad > 0
        val almacenOk = !item.manejaInventario || !linea.almacenId.isNullOrBlank()
        if (!almacenOk) return@all false
        if (item.manejaSerie) {
            val unidades = linea.series.ifEmpty {
                linea.numerosSerieLimpios().orEmpty().map { num ->
                    ProductoSerie(
                        id = "temp",
                        companyRuc = item.companyRuc,
                        catalogItemId = linea.catalogItemId,
                        numeroSerie = num,
                        almacenId = linea.almacenId,
                        estado = ProductoSerieEstado.DISPONIBLE,
                    )
                }
            }
            unidades.isNotEmpty() && linea.cantidad == unidades.size.toDouble()
        } else {
            linea.cantidad > 0
        }
    }

fun LineaCatalogoItem.aEmitirLinea(): EmitirLineaRequest = EmitirLineaRequest(
    catalogItemId = catalogItemId,
    cantidad = cantidad,
    serieIds = when {
        seriesEfectivas.isNotEmpty() -> seriesEfectivas.map { it.id }
        else -> numerosSerieLimpios()
    },
)

fun List<LineaCatalogoItem>.aEmitirLineas(): List<EmitirLineaRequest> = map { it.aEmitirLinea() }

fun LineaCatalogoItem.aRegistrarSalidaLinea(): RegistrarMovimientoLineaRequest =
    aRegistrarMovimientoLinea()

fun List<LineaCatalogoItem>.aRegistrarSalidaLineas(): List<RegistrarMovimientoLineaRequest> =
    map { it.aRegistrarSalidaLinea() }

fun LineaCatalogoItem.aRegistrarMovimientoLinea(): RegistrarMovimientoLineaRequest {
    if (!manejaSerieEfectivo) {
        return RegistrarMovimientoLineaRequest(
            catalogItemId = catalogItemId,
            cantidad = cantidad,
        )
    }
    val idsInventario = seriesEfectivas
        .map { it.id }
        .filter { id -> id.isNotBlank() && !id.startsWith("scan-") && id != "temp" }
    val numeros = numerosSerieLimpios()
        ?: seriesEfectivas.takeIf { it.isNotEmpty() }?.map { it.numeroSerie }
    return RegistrarMovimientoLineaRequest(
        catalogItemId = catalogItemId,
        cantidad = cantidad,
        numerosSerie = numeros.takeIf { idsInventario.isEmpty() },
        serieIds = idsInventario.takeIf { it.isNotEmpty() },
    )
}

fun LineaCatalogoItem.enriquecerConCatalogo(
    catalogItem: CatalogItem,
    almacenId: String? = null,
    seriesPorId: (String) -> ProductoSerie? = { null },
): LineaCatalogoItem {
    val base = copy(
        catalogItem = catalogItem,
        catalogItemId = catalogItem.id,
        nombre = nombre ?: catalogItem.nombre,
        codigo = codigo ?: catalogItem.codigo,
        descripcion = descripcion ?: catalogItem.descripcion,
        unidad = unidad ?: catalogItem.unidad,
        precioUnitario = precioUnitario ?: catalogItem.precioUnitario,
        afectacionIgv = afectacionIgv ?: catalogItem.afectacionIgv,
        kind = kind ?: catalogItem.kind,
        manejaStock = manejaStock ?: catalogItem.manejaStock,
        manejaSerie = manejaSerie ?: catalogItem.manejaSerie,
        almacenId = almacenId ?: this.almacenId,
    )
    return when {
        productoSerie != null -> base.copy(
            almacenId = almacenId ?: productoSerie.almacenId,
            series = listOf(productoSerie),
        )
        !serieIds.isNullOrEmpty() -> {
            val unidades = serieIds.mapNotNull(seriesPorId)
            if (unidades.isNotEmpty()) {
                base.copy(
                    almacenId = almacenId ?: unidades.firstOrNull()?.almacenId,
                    series = unidades,
                )
            } else {
                base
            }
        }
        numerosSerieApi != null || numerosSerie.isNotEmpty() -> base.copy(
            numerosSerie = numerosSerie.ifEmpty { numerosSerieApi.orEmpty() },
        )
        else -> base
    }
}

/** Convierte a línea de [Invoice] al emitir o persistir comprobante. */
fun LineaCatalogoItem.toSaleDetail(invoiceId: String? = null): SaleDetail {
    val pct = if (afectacionIgvEfectiva == "10") 18.0 else 0.0
    val factor = 1.0 + pct / 100.0
    val valorVenta = (precioUnitarioEfectivo * cantidad / factor).redondear2()
    val igvLinea = (valorVenta * pct / 100.0).redondear2()
    val totalLinea = (valorVenta + igvLinea).redondear2()
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
        mtoValorUnitario = (precioUnitarioEfectivo / factor).redondear2(),
        mtoBaseIgv = valorVenta,
        porcentajeIgv = pct,
        productoSerie = productoSerie,
    )
}

fun List<LineaCatalogoItem>.toSaleDetails(invoiceId: String? = null): List<SaleDetail> =
    map { it.toSaleDetail(invoiceId) }

private fun Double.redondear2(): Double = kotlin.math.round(this * 100.0) / 100.0
