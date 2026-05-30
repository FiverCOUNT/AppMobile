package com.factapp.jhonny.network.dto.demo

import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.network.dto.model.SaleDetail
import com.factapp.jhonny.network.dto.model.companyReceptor

/**
 * Facturas de compra (comprobantes recibidos de proveedores) de demostración.
 * En producción: GET compras o comprobantes con rol COMPRA.
 */
object ComprasDemo {

    fun listar(companyRuc: String): List<Invoice> = listOf(
        compra(
            id = "cmp-compra-001",
            companyRuc = companyRuc,
            serie = "F001",
            correlativo = "00045231",
            proveedor = "Distribuidora Norte EIRL",
            docProveedor = "10456789012",
            fecha = "2026-05-18T10:15:00Z",
            details = listOf(
                linea("Laptop 15\"", 5.0, 2450.0),
                linea("Mouse inalámbrico", 10.0, 45.0),
            ),
            estado = ComprobanteEstado.ACEPTADO,
        ),
        compra(
            id = "cmp-compra-002",
            companyRuc = companyRuc,
            serie = "E001",
            correlativo = "00012890",
            proveedor = "Papelera Central SAC",
            docProveedor = "20111222333",
            fecha = "2026-05-16T14:30:00Z",
            details = listOf(
                linea("Resma A4", 50.0, 18.5),
                linea("Toner impresora", 4.0, 320.0),
            ),
            estado = ComprobanteEstado.ACEPTADO,
        ),
        compra(
            id = "cmp-compra-003",
            companyRuc = companyRuc,
            serie = "F003",
            correlativo = "00000892",
            proveedor = "Importaciones del Pacífico SA",
            docProveedor = "20555666777",
            fecha = "2026-05-12T09:00:00Z",
            details = listOf(linea("Monitor 27\" 4K", 12.0, 890.0)),
            estado = ComprobanteEstado.ENVIADO,
        ),
        compra(
            id = "cmp-compra-004",
            companyRuc = companyRuc,
            serie = "F001",
            correlativo = "00045180",
            proveedor = "Servicios Integrales del Sur SA",
            docProveedor = "20555666777",
            fecha = "2026-05-08T16:45:00Z",
            details = listOf(linea("Mantenimiento anual software", 1.0, 4200.0)),
            estado = ComprobanteEstado.BORRADOR,
        ),
        compra(
            id = "cmp-compra-005",
            companyRuc = companyRuc,
            serie = "F001",
            correlativo = "00044901",
            proveedor = "Comercial Andina SAC",
            docProveedor = "20123456789",
            fecha = "2026-05-02T11:20:00Z",
            details = listOf(
                linea("Teclado mecánico", 8.0, 185.0),
                linea("Webcam HD", 6.0, 120.0),
            ),
            estado = ComprobanteEstado.ACEPTADO,
        ),
    )

    private fun linea(descripcion: String, cantidad: Double, precio: Double): SaleDetail {
        val sub = cantidad * precio
        val igv = sub * 0.18
        return SaleDetail(
            descripcion = descripcion,
            cantidad = cantidad,
            unidad = "NIU",
            mtoPrecioUnitario = precio,
            mtoValorVenta = sub,
            mtoIgv = igv,
            totalFactura = sub + igv,
        )
    }

    private fun compra(
        id: String,
        companyRuc: String,
        serie: String,
        correlativo: String,
        proveedor: String,
        docProveedor: String,
        fecha: String,
        details: List<SaleDetail>,
        estado: ComprobanteEstado,
    ): Invoice {
        val subtotal = details.sumOf { it.subtotal }
        val igv = details.sumOf { it.igv }
        val total = details.sumOf { it.total }
        return Invoice(
            id = id,
            companyRuc = companyRuc,
            tipoDoc = InvoiceTipoDoc.FACTURA,
            serie = serie,
            correlativo = correlativo,
            estado = estado,
            client = companyReceptor(
                tipoDoc = if (docProveedor.length == 11) "6" else "1",
                numeroDoc = docProveedor,
                nombre = proveedor,
            ),
            details = details,
            subTotal = subtotal,
            mtoIgv = igv,
            mtoImpVenta = total,
            observacion = "Factura de compra · proveedor",
            fechaEmision = fecha,
            cdrEstado = estado.name,
            pdfUrl = urlsDemo(id, "pdf"),
            cdrZipUrl = if (estado == ComprobanteEstado.ACEPTADO || estado == ComprobanteEstado.ENVIADO) {
                urlsDemo(id, "cdr.zip")
            } else {
                null
            },
        )
    }

    /** Rutas de ejemplo; en producción el API devuelve URLs firmadas. */
    private fun urlsDemo(id: String, sufijo: String): String =
        "https://demo.factapp.local/comprobantes/$id/$sufijo"
}
