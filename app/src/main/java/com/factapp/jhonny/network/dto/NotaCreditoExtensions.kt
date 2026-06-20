package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.MotivosNotaCreditoSunat
import com.factapp.jhonny.network.dto.MotivosNotaDebitoSunat
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.SaleDetail

/** Solo facturas electrónicas pueden ser afectadas por una nota de crédito. */
fun Invoice.esAfectablePorNotaCredito(): Boolean {
    val esFactura = tipoDoc == InvoiceTipoDoc.COD_FACTURA ||
        tipo == InvoiceTipoDoc.FACTURA
    val estadoOk = estado == ComprobanteEstado.ACEPTADO ||
        estado == ComprobanteEstado.ENVIADO
    return esFactura && estadoOk && details.isNotEmpty()
}

fun List<Invoice>.aptosParaNotaCredito(): List<Invoice> =
    filter { it.esAfectablePorNotaCredito() }

/** Facturas y boletas aceptadas pueden ser afectadas por una nota de débito. */
fun Invoice.esAfectablePorNotaDebito(): Boolean {
    val esFacturaOBoleta = tipoDoc == InvoiceTipoDoc.COD_FACTURA ||
        tipo == InvoiceTipoDoc.FACTURA ||
        tipoDoc == InvoiceTipoDoc.COD_BOLETA ||
        tipo == InvoiceTipoDoc.BOLETA
    val estadoOk = estado == ComprobanteEstado.ACEPTADO ||
        estado == ComprobanteEstado.ENVIADO
    return esFacturaOBoleta && estadoOk && details.isNotEmpty()
}

fun List<Invoice>.aptosParaNotaDebito(): List<Invoice> =
    filter { it.esAfectablePorNotaDebito() }

fun List<Invoice>.filtrarComprobantesAfectados(
    query: String,
    docCliente: String = "",
    soloFacturas: Boolean = true,
): List<Invoice> {
    var lista = if (soloFacturas) aptosParaNotaCredito() else aptosParaNotaDebito()
    val doc = docCliente.filter { it.isDigit() }
    if (doc.length in listOf(8, 11)) {
        lista = lista.filter { it.docReceptorNormalizado() == doc }
    }
    val q = query.trim().lowercase()
    if (q.isNotBlank()) {
        lista = lista.filter { inv ->
            inv.etiquetaCompleta.lowercase().contains(q) ||
                inv.receptor.nombre.lowercase().contains(q) ||
                inv.docReceptorNormalizado().contains(q.filter { c -> c.isDigit() }) ||
                inv.cliente?.razonSocial?.lowercase()?.contains(q) == true ||
                inv.etiquetaTipo().lowercase().contains(q)
        }
    }
    return lista.take(8)
}

fun Invoice.docReceptorNormalizado(): String =
    cliente?.numeroDoc?.filter { it.isDigit() }
        ?: receptor.documentoNumero.filter { it.isDigit() }

fun SaleDetail.aLineaCatalogoItem(
    catalogo: List<CatalogItem> = emptyList(),
    almacenId: String? = null,
): LineaCatalogoItem? {
    val catId = catalogItemId?.takeIf { it.isNotBlank() } ?: return null
    val item = catalogo.find { it.id == catId }
    val base = LineaCatalogoItem(
        catalogItemId = catId,
        nombre = nombre?.takeIf { it.isNotBlank() } ?: descripcion,
        descripcion = descripcion,
        unidad = unidad,
        precioUnitario = mtoPrecioUnitario,
        precioOriginalReferencia = mtoPrecioUnitario,
        cantidadOriginalReferencia = cantidad,
        afectacionIgv = tipAfeIgv,
        cantidad = cantidad,
        almacenId = almacenId,
        productoSerie = productoSerie,
        catalogItem = item,
    )
    return if (productoSerie != null) {
        base.copy(series = listOf(productoSerie))
    } else {
        base
    }
}

fun Invoice.aLineasParaAcreditar(
    catalogo: List<CatalogItem>,
    almacenId: String? = null,
): List<LineaCatalogoItem> =
    details.mapNotNull { it.aLineaCatalogoItem(catalogo, almacenId) }

fun List<LineaCatalogoItem>.prepararParaMotivoNc(codigoMotivo: String?): List<LineaCatalogoItem> =
    if (!MotivosNotaCreditoSunat.esAcreditacionPorMonto(codigoMotivo)) {
        map { linea ->
            val original = linea.precioOriginalReferencia ?: return@map linea
            linea.copy(precioUnitario = original)
        }
    } else {
        map { linea ->
            linea.copy(
                precioOriginalReferencia = linea.precioOriginalReferencia ?: linea.precioUnitarioEfectivo,
                cantidadOriginalReferencia = linea.cantidadOriginalReferencia ?: linea.cantidad,
                precioUnitario = 0.0,
            )
        }
    }

fun List<LineaCatalogoItem>.prepararParaMotivoNd(codigoMotivo: String?): List<LineaCatalogoItem> =
    if (MotivosNotaDebitoSunat.esAjustePorItems(codigoMotivo)) {
        map { linea ->
            linea.copy(
                precioOriginalReferencia = linea.precioOriginalReferencia ?: linea.precioUnitarioEfectivo,
                cantidadOriginalReferencia = linea.cantidadOriginalReferencia ?: linea.cantidad,
                precioUnitario = 0.0,
            )
        }
    } else {
        this
    }

fun List<Cliente>.buscarPorDocumento(doc: String): Cliente? {
    val normalizado = doc.filter { it.isDigit() }
    if (normalizado.length !in listOf(8, 11)) return null
    return find { it.numeroDoc == normalizado }
}
