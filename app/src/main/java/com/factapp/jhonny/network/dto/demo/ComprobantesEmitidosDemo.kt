package com.factapp.jhonny.network.dto.demo

import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.network.dto.model.SaleDetail
import com.factapp.jhonny.network.dto.model.TIPO_DOC_DNI
import com.factapp.jhonny.network.dto.model.TIPO_DOC_RUC
import com.factapp.jhonny.network.dto.model.companyReceptor
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Comprobantes emitidos (ventas) de demostración.
 * En producción: GET empresas/{ruc}/comprobantes
 */
object ComprobantesEmitidosDemo {

    fun listar(companyRuc: String): List<Invoice> {
        val hoy = LocalDate.now()
        return listOf(
            emitido(
                id = "cmp-venta-hoy-1",
                companyRuc = companyRuc,
                tipoDoc = InvoiceTipoDoc.FACTURA,
                serie = "F001",
                correlativo = "00012840",
                cliente = "Comercial Andina SAC",
                doc = "20123456789",
                fecha = hoy,
                total = 2850.0,
                estado = ComprobanteEstado.ACEPTADO,
            ),
            emitido(
                id = "cmp-venta-hoy-2",
                companyRuc = companyRuc,
                tipoDoc = InvoiceTipoDoc.BOLETA,
                serie = "B001",
                correlativo = "00098421",
                cliente = "Juan Pérez López",
                doc = "10456789012",
                fecha = hoy,
                total = 120.50,
                estado = ComprobanteEstado.ENVIADO,
            ),
            emitido(
                id = "cmp-venta-ayer",
                companyRuc = companyRuc,
                tipoDoc = InvoiceTipoDoc.FACTURA,
                serie = "F001",
                correlativo = "00012839",
                cliente = "Distribuidora Norte EIRL",
                doc = "10456789012",
                fecha = hoy.minusDays(1),
                total = 5420.0,
                estado = ComprobanteEstado.ACEPTADO,
            ),
            emitido(
                id = "cmp-venta-semana",
                companyRuc = companyRuc,
                tipoDoc = InvoiceTipoDoc.BOLETA,
                serie = "B001",
                correlativo = "00098400",
                cliente = "María García",
                doc = "45678901",
                fecha = hoy.minusDays(4),
                total = 89.90,
                estado = ComprobanteEstado.ACEPTADO,
            ),
            emitido(
                id = "cmp-venta-mes",
                companyRuc = companyRuc,
                tipoDoc = InvoiceTipoDoc.NOTA_CREDITO,
                serie = "FC01",
                correlativo = "00000045",
                cliente = "Comercial Andina SAC",
                doc = "20123456789",
                fecha = hoy.minusDays(12),
                total = 350.0,
                estado = ComprobanteEstado.ACEPTADO,
            ),
            emitido(
                id = "cmp-venta-guia",
                companyRuc = companyRuc,
                tipoDoc = InvoiceTipoDoc.GUIA_EMISION,
                serie = "T001",
                correlativo = "00000102",
                cliente = "Logística Sur SAC",
                doc = "20555666777",
                fecha = hoy.minusDays(20),
                total = 0.0,
                estado = ComprobanteEstado.ACEPTADO,
            ),
        )
    }

    private fun emitido(
        id: String,
        companyRuc: String,
        tipoDoc: String,
        serie: String,
        correlativo: String,
        cliente: String,
        doc: String,
        fecha: LocalDate,
        total: Double,
        estado: ComprobanteEstado,
    ): Invoice {
        val subtotal = if (total > 0) total / 1.18 else 0.0
        val igv = total - subtotal
        val linea = SaleDetail(
            descripcion = "Ítem demo",
            cantidad = 1.0,
            unidad = "NIU",
            mtoPrecioUnitario = subtotal,
            mtoValorVenta = subtotal,
            mtoIgv = igv,
            totalFactura = total,
        )
        val iso = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toString()
        val esBoleta = tipoDoc == InvoiceTipoDoc.BOLETA
        val tipoDocReceptor = if (doc.length == 11) TIPO_DOC_RUC else TIPO_DOC_DNI
        return Invoice(
            id = id,
            companyRuc = companyRuc,
            tipoDoc = tipoDoc,
            serie = serie,
            correlativo = correlativo,
            estado = estado,
            client = if (esBoleta) {
                Company(nombre = "")
            } else {
                companyReceptor(tipoDoc = tipoDocReceptor, numeroDoc = doc, nombre = cliente)
            },
            cliente = if (esBoleta) {
                Cliente(
                    id = "demo-cli-$id",
                    companyRuc = companyRuc,
                    tipoDoc = tipoDocReceptor,
                    numeroDoc = doc,
                    razonSocial = cliente,
                )
            } else {
                null
            },
            details = listOf(linea),
            subTotal = subtotal,
            mtoIgv = igv,
            mtoImpVenta = total,
            fechaEmision = iso,
            pdfUrl = "https://demo.factapp.local/comprobantes/$id/pdf",
            cdrZipUrl = if (estado == ComprobanteEstado.ACEPTADO || estado == ComprobanteEstado.ENVIADO) {
                "https://demo.factapp.local/comprobantes/$id/cdr.zip"
            } else {
                null
            },
        )
    }
}
