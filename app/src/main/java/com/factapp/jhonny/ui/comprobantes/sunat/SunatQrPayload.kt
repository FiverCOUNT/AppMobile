package com.factapp.jhonny.ui.comprobantes.sunat

import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.network.dto.toLocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ZONA_PERU: ZoneId = ZoneId.of("America/Lima")

/**
 * Cadena pipe-delimited para QR SUNAT (Anexo F RS 185-2015 / flexibilización 340-2017).
 * RUC|TIPO|SERIE|NUMERO|IGV|TOTAL|FECHA|TIPO_DOC_CLIENTE|NUM_DOC_CLIENTE|HASH
 */
object SunatQrPayload {

    fun build(invoice: Invoice): String {
        val ruc = invoice.company?.ruc?.takeIf { it.isNotBlank() }
            ?: invoice.companyRuc.takeIf { it.isNotBlank() }
            ?: return ""

        val tipo = codigoTipoDoc(invoice.tipoDoc)
        val igv = formatoMonto(invoice.totales.igv)
        val total = formatoMonto(invoice.totales.total)
        val fecha = invoice.fechaEmision
            ?.toLocalDate(ZONA_PERU)
            ?.format(DateTimeFormatter.ISO_LOCAL_DATE)
            ?: ""

        val receptor = invoice.receptor
        val tipoCliente = receptor.tipoDoc?.takeIf { it.isNotBlank() } ?: "-"
        val numCliente = receptor.numeroDoc?.takeIf { it.isNotBlank() } ?: "-"
        val hash = invoice.hashCpe?.takeIf { it.isNotBlank() } ?: ""

        return listOf(
            ruc,
            tipo,
            invoice.serie,
            invoice.correlativo,
            igv,
            total,
            fecha,
            tipoCliente,
            numCliente,
            hash,
        ).joinToString("|")
    }

    private fun codigoTipoDoc(tipoDoc: String): String = when (tipoDoc.uppercase()) {
        InvoiceTipoDoc.FACTURA, InvoiceTipoDoc.COD_FACTURA -> "01"
        InvoiceTipoDoc.BOLETA, InvoiceTipoDoc.COD_BOLETA -> "03"
        InvoiceTipoDoc.NOTA_CREDITO, InvoiceTipoDoc.COD_NOTA_CREDITO -> "07"
        InvoiceTipoDoc.NOTA_DEBITO, InvoiceTipoDoc.COD_NOTA_DEBITO -> "08"
        InvoiceTipoDoc.GUIA_EMISION, InvoiceTipoDoc.COD_GUIA -> "09"
        else -> tipoDoc.take(2).padStart(2, '0')
    }

    private fun formatoMonto(value: Double): String =
        String.format(Locale.US, "%.2f", value)
}
