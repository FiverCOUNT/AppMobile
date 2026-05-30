package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc

fun Invoice.etiquetaTipo(): String = when (tipo) {
    InvoiceTipoDoc.FACTURA -> "Factura"
    InvoiceTipoDoc.BOLETA -> "Boleta"
    InvoiceTipoDoc.NOTA_CREDITO -> "Nota de crédito"
    InvoiceTipoDoc.NOTA_DEBITO -> "Nota de débito"
    InvoiceTipoDoc.GUIA_EMISION -> "Guía de remisión"
    InvoiceTipoDoc.COD_FACTURA -> "Factura"
    InvoiceTipoDoc.COD_BOLETA -> "Boleta"
    InvoiceTipoDoc.COD_NOTA_CREDITO -> "Nota de crédito"
    InvoiceTipoDoc.COD_NOTA_DEBITO -> "Nota de débito"
    else -> tipo.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

fun ComprobanteEstado.etiqueta(): String = when (this) {
    ComprobanteEstado.BORRADOR -> "Borrador"
    ComprobanteEstado.ENVIADO -> "Enviado"
    ComprobanteEstado.ACEPTADO -> "Aceptado"
    ComprobanteEstado.RECHAZADO -> "Rechazado"
    ComprobanteEstado.ANULADO -> "Anulado"
}

fun Invoice.tienePdf(): Boolean = !pdfUrl.isNullOrBlank()

fun Invoice.tieneCdrZip(): Boolean = !cdrZipUrl.isNullOrBlank()

fun ComprobanteEstado.colorArgb(): Long = when (this) {
    ComprobanteEstado.ACEPTADO -> 0xFF2E7D32
    ComprobanteEstado.ENVIADO -> 0xFF1565C0
    ComprobanteEstado.BORRADOR -> 0xFFF57C00
    ComprobanteEstado.RECHAZADO -> 0xFFC62828
    ComprobanteEstado.ANULADO -> 0xFF757575
}
