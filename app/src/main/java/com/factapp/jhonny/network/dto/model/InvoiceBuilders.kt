package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.network.dto.request.CrearFacturaLineaRequest
import com.factapp.jhonny.network.dto.request.CrearFacturaRequest
import kotlin.math.round

/**
 * Construye payloads de factura a partir del catálogo / pantalla de emisión.
 */

fun CrearFacturaRequest.desdeGreenter(
    cliente: Company,
    serie: String,
    fechaEmisionIso: String,
    items: List<CrearFacturaLineaRequest>,
    leyendas: List<Legend>? = null,
    formaPago: String = Invoice.FORMA_PAGO_CONTADO,
): CrearFacturaRequest = CrearFacturaRequest(
    serie = serie,
    fechaEmision = fechaEmisionIso.take(10),
    formaPago = formaPago,
    cliente = cliente,
    items = items,
    leyendas = leyendas,
)

/** Calcula totales gravados 18% desde líneas con precio unitario con IGV incluido. */
fun List<CrearFacturaLineaRequest>.totalesGravados18(moneda: String = "PEN"): ComprobanteTotales {
    var valorVenta = 0.0
    var igv = 0.0
    for (linea in this) {
        val base = linea.precioUnitario * linea.cantidad / 1.18
        val lineIgv = base * 0.18
        valorVenta += base
        igv += lineIgv
    }
    valorVenta = valorVenta.redondear2()
    igv = igv.redondear2()
    val total = (valorVenta + igv).redondear2()
    return ComprobanteTotales(
        subtotal = valorVenta,
        igv = igv,
        total = total,
        moneda = moneda,
        mtoOperGravadas = valorVenta,
        totalImpuestos = igv,
    )
}

fun List<CrearFacturaLineaRequest>.aSaleDetails(): List<SaleDetail> = map { linea ->
    val pct = linea.porcentajeIgv ?: 18.0
    val factor = 1.0 + pct / 100.0
    val valorVenta = (linea.precioUnitario * linea.cantidad / factor).redondear2()
    val igvLinea = (valorVenta * pct / 100.0).redondear2()
    val totalLinea = (valorVenta + igvLinea).redondear2()
    SaleDetail(
        catalogItemId = linea.catalogItemId ?: linea.codigo,
        descripcion = linea.descripcion,
        cantidad = linea.cantidad,
        unidad = linea.unidad,
        mtoPrecioUnitario = linea.precioUnitario,
        tipAfeIgv = linea.tipAfeIgv,
        mtoValorVenta = valorVenta,
        mtoIgv = igvLinea,
        totalFactura = totalLinea,
        mtoValorUnitario = (linea.precioUnitario / factor).redondear2(),
        mtoBaseIgv = valorVenta,
        porcentajeIgv = pct,
    )
}

private fun Double.redondear2(): Double = round(this * 100.0) / 100.0
