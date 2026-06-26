package com.factapp.jhonny.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc

data class ComprobanteTipoPalette(
    val etiquetaCorta: String,
    val siglas: String,
    val accent: Color,
    val soft: Color,
    val title: Color,
    val icon: ImageVector,
)

fun Invoice.tipoPalette(): ComprobanteTipoPalette = when (tipo) {
    InvoiceTipoDoc.FACTURA, InvoiceTipoDoc.COD_FACTURA -> ComprobanteTipoPalette(
        etiquetaCorta = "Factura",
        siglas = "FACT",
        accent = Color(0xFF1565C0),
        soft = Color(0xFFBBDEFB),
        title = Color(0xFF0D47A1),
        icon = Icons.Default.Receipt,
    )
    InvoiceTipoDoc.BOLETA, InvoiceTipoDoc.COD_BOLETA -> ComprobanteTipoPalette(
        etiquetaCorta = "Boleta",
        siglas = "BOL",
        accent = Color(0xFF2E7D32),
        soft = Color(0xFFC8E6C9),
        title = Color(0xFF1B5E20),
        icon = Icons.Default.Description,
    )
    InvoiceTipoDoc.NOTA_CREDITO, InvoiceTipoDoc.COD_NOTA_CREDITO -> ComprobanteTipoPalette(
        etiquetaCorta = "Nota crédito",
        siglas = "NC",
        accent = Color(0xFF6A1B9A),
        soft = Color(0xFFE1BEE7),
        title = Color(0xFF4A148C),
        icon = Icons.Default.History,
    )
    InvoiceTipoDoc.NOTA_DEBITO, InvoiceTipoDoc.COD_NOTA_DEBITO -> ComprobanteTipoPalette(
        etiquetaCorta = "Nota débito",
        siglas = "ND",
        accent = Color(0xFFEF6C00),
        soft = Color(0xFFFFE0B2),
        title = Color(0xFFE65100),
        icon = Icons.Default.Add,
    )
    InvoiceTipoDoc.GUIA_EMISION, InvoiceTipoDoc.COD_GUIA -> ComprobanteTipoPalette(
        etiquetaCorta = "Guía",
        siglas = "GRE",
        accent = Color(0xFF00838F),
        soft = Color(0xFFB2EBF2),
        title = Color(0xFF006064),
        icon = Icons.Default.LocalShipping,
    )
    InvoiceTipoDoc.GUIA_TRANSPORTISTA, InvoiceTipoDoc.COD_GUIA_TRANSPORTISTA -> ComprobanteTipoPalette(
        etiquetaCorta = "GRE transp.",
        siglas = "GRT",
        accent = Color(0xFFEF6C00),
        soft = Color(0xFFFFE0B2),
        title = Color(0xFFE65100),
        icon = Icons.Default.LocalShipping,
    )
    else -> ComprobanteTipoPalette(
        etiquetaCorta = "Comprobante",
        siglas = "DOC",
        accent = ComprobanteEmitColors.accent,
        soft = ComprobanteEmitColors.accentSoft,
        title = ComprobanteEmitColors.primary,
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
    )
}

fun Invoice.coincideFiltroTipo(siglas: String): Boolean =
    tipoPalette().siglas.equals(siglas, ignoreCase = true)

fun List<Invoice>.filtrarPorTipo(siglas: String?): List<Invoice> {
    if (siglas.isNullOrBlank()) return this
    return filter { it.coincideFiltroTipo(siglas) }
}

val LeyendaTiposComprobante: List<ComprobanteTipoPalette> = listOf(
    ComprobanteTipoPalette("Factura", "FACT", Color(0xFF1565C0), Color(0xFFBBDEFB), Color(0xFF0D47A1), Icons.Default.Receipt),
    ComprobanteTipoPalette("Boleta", "BOL", Color(0xFF2E7D32), Color(0xFFC8E6C9), Color(0xFF1B5E20), Icons.Default.Description),
    ComprobanteTipoPalette("Nota crédito", "NC", Color(0xFF6A1B9A), Color(0xFFE1BEE7), Color(0xFF4A148C), Icons.Default.History),
    ComprobanteTipoPalette("Nota débito", "ND", Color(0xFFEF6C00), Color(0xFFFFE0B2), Color(0xFFE65100), Icons.Default.Add),
    ComprobanteTipoPalette("Guía", "GRE", Color(0xFF00838F), Color(0xFFB2EBF2), Color(0xFF006064), Icons.Default.LocalShipping),
    ComprobanteTipoPalette("GRE transp.", "GRT", Color(0xFFEF6C00), Color(0xFFFFE0B2), Color(0xFFE65100), Icons.Default.LocalShipping),
)
