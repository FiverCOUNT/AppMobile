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
fun Invoice.esAfectablePorNotaCredito(): Boolean =
    esListableComoDocumentoAfectado(soloFacturas = true) && details.isNotEmpty()

fun Invoice.esListableComoDocumentoAfectado(soloFacturas: Boolean): Boolean {
    if (!estadoPermiteNotaSobreDocumento()) return false
    return if (soloFacturas) esFacturaListable() else esFacturaListable() || esBoletaListable()
}

private fun Invoice.esFacturaListable(): Boolean =
    tipoDoc == InvoiceTipoDoc.COD_FACTURA ||
        tipo == InvoiceTipoDoc.FACTURA ||
        tipoDoc.equals(InvoiceTipoDoc.FACTURA, ignoreCase = true) ||
        serie.uppercase().startsWith("F")

private fun Invoice.esBoletaListable(): Boolean =
    tipoDoc == InvoiceTipoDoc.COD_BOLETA ||
        tipo == InvoiceTipoDoc.BOLETA ||
        tipoDoc.equals(InvoiceTipoDoc.BOLETA, ignoreCase = true) ||
        serie.uppercase().startsWith("B")

private fun Invoice.estadoPermiteNotaSobreDocumento(): Boolean = when (estado) {
    ComprobanteEstado.RECHAZADO, ComprobanteEstado.ANULADO -> false
    ComprobanteEstado.ACEPTADO, ComprobanteEstado.ENVIADO -> true
    ComprobanteEstado.BORRADOR -> sunatOk == true || id.isNotBlank()
    else -> id.isNotBlank()
}

fun List<Invoice>.aptosParaNotaCredito(): List<Invoice> =
    filter { it.esAfectablePorNotaCredito() }

fun List<Invoice>.listablesComoDocumentoAfectado(soloFacturas: Boolean): List<Invoice> =
    filter { it.esListableComoDocumentoAfectado(soloFacturas) }

/** Facturas y boletas aceptadas pueden ser afectadas por una nota de débito. */
fun Invoice.esAfectablePorNotaDebito(): Boolean =
    esListableComoDocumentoAfectado(soloFacturas = false) && details.isNotEmpty()

fun List<Invoice>.aptosParaNotaDebito(): List<Invoice> =
    filter { it.esAfectablePorNotaDebito() }

fun List<Invoice>.filtrarComprobantesAfectados(
    query: String,
    docCliente: String = "",
    soloFacturas: Boolean = true,
): List<Invoice> {
    var lista = listablesComoDocumentoAfectado(soloFacturas)
    val doc = docCliente.filter { it.isDigit() }
    if (doc.length in listOf(8, 11)) {
        val porDoc = lista.filter { it.coincideDocReceptor(doc) }
        if (porDoc.isNotEmpty()) lista = porDoc
    }
    val q = query.trim()
    if (q.isNotBlank()) {
        lista = lista.filter { it.coincideConBusquedaAfectado(q) }
    }
    return lista.take(20)
}

private fun Invoice.coincideConBusquedaAfectado(query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isBlank()) return true
    val qDigits = q.filter { it.isDigit() }
    val qSinSeparadores = q.replace("-", "").replace(" ", "")
    val etiqueta = etiquetaCompleta.lowercase()
    val etiquetaSinSeparadores = etiqueta.replace("-", "").replace(" ", "")
    val corrDigits = correlativoNumerico()
    val numeroSinSerie = numeroEtiquetaSinSerie()

    if (qDigits.isNotEmpty()) {
        if (corrDigits.contains(qDigits) || corrDigits.endsWith(qDigits)) return true
        if (numeroSinSerie.contains(qDigits) || numeroSinSerie.endsWith(qDigits)) return true
        if (etiquetaSinSeparadores.contains(qDigits) || etiquetaSinSeparadores.endsWith(qDigits)) {
            return true
        }
    }

    return etiqueta.contains(q) ||
        etiquetaSinSeparadores.contains(qSinSeparadores) ||
        serie.lowercase().contains(q) ||
        receptor.nombre.lowercase().contains(q) ||
        cliente?.razonSocial?.lowercase()?.contains(q) == true ||
        (qDigits.isNotEmpty() && docReceptorNormalizado().contains(qDigits)) ||
        etiquetaTipo().lowercase().contains(q)
}

private fun Invoice.correlativoNumerico(): String =
    correlativo.filter { it.isDigit() }.ifBlank {
        numero.filter { it.isDigit() }
    }.ifBlank {
        etiquetaCompleta.substringAfter("-", "").filter { it.isDigit() }
    }

private fun Invoice.numeroEtiquetaSinSerie(): String {
    val despuesSerie = etiquetaCompleta.substringAfter("-", "")
    return despuesSerie.filter { it.isDigit() }.ifBlank { correlativoNumerico() }
}

fun Invoice.docReceptorNormalizado(): String =
    docReceptorCandidatos().firstOrNull().orEmpty()

fun Invoice.coincideDocReceptor(doc: String): Boolean {
    val normalizado = doc.filter { it.isDigit() }
    if (normalizado.isBlank()) return true
    return docReceptorCandidatos().any { it == normalizado }
}

private fun Invoice.docReceptorCandidatos(): List<String> = buildList {
    cliente?.numeroDoc?.filter { it.isDigit() }?.takeIf { it.isNotBlank() }?.let { add(it) }
    client.numeroDoc?.filter { it.isDigit() }?.takeIf { it.isNotBlank() }?.let { add(it) }
    client.ruc.filter { it.isDigit() }.takeIf { it.isNotBlank() }?.let { add(it) }
}.distinct()

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
