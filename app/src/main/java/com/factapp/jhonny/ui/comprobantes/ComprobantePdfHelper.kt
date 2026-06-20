package com.factapp.jhonny.ui.comprobantes

import android.graphics.DashPathEffect
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.factapp.jhonny.network.ComprobanteRepository.PdfFormato
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.fechaEmisionLegible
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.network.dto.model.SaleDetail
import com.factapp.jhonny.network.dto.model.lineaPrincipal
import com.factapp.jhonny.network.dto.model.textoEnComprobante
import com.factapp.jhonny.ui.comprobantes.sunat.QrCodeBitmap
import com.factapp.jhonny.ui.comprobantes.sunat.SunatQrPayload
import java.io.ByteArrayOutputStream
import java.util.Locale

object ComprobantePdfHelper {

    fun generarPdfBytes(
        invoice: Invoice,
        formato: PdfFormato,
        emisorFallback: Company? = null,
    ): ByteArray? = runCatching {
        val document = PdfDocument()
        try {
            when (formato) {
                PdfFormato.TICKET -> ComprobanteTicketWriter(document, invoice, emisorFallback).render()
                PdfFormato.A4 -> ComprobanteA4Writer(document, invoice, emisorFallback).render()
            }
            ByteArrayOutputStream().use { out ->
                document.writeTo(out)
                out.toByteArray()
            }
        } finally {
            document.close()
        }
    }.getOrNull()
}

private const val SUNAT_PIE =
    "Representación impresa del comprobante electrónico. Consulte su validez en " +
        "www.sunat.gob.pe → Opciones sin Clave SOL → Consulta de Validez del CPE."

private val TIPO_DOC_TITULO = mapOf(
    InvoiceTipoDoc.COD_FACTURA to "FACTURA ELECTRÓNICA",
    InvoiceTipoDoc.COD_BOLETA to "BOLETA DE VENTA ELECTRÓNICA",
    InvoiceTipoDoc.COD_NOTA_CREDITO to "NOTA DE CRÉDITO ELECTRÓNICA",
    InvoiceTipoDoc.COD_NOTA_DEBITO to "NOTA DE DÉBITO ELECTRÓNICA",
    InvoiceTipoDoc.COD_GUIA to "GUÍA DE REMISIÓN ELECTRÓNICA",
)

private fun tituloDocumento(invoice: Invoice): String {
    val cod = when (invoice.tipo) {
        InvoiceTipoDoc.FACTURA -> InvoiceTipoDoc.COD_FACTURA
        InvoiceTipoDoc.BOLETA -> InvoiceTipoDoc.COD_BOLETA
        InvoiceTipoDoc.NOTA_CREDITO -> InvoiceTipoDoc.COD_NOTA_CREDITO
        InvoiceTipoDoc.NOTA_DEBITO -> InvoiceTipoDoc.COD_NOTA_DEBITO
        InvoiceTipoDoc.GUIA_EMISION -> InvoiceTipoDoc.COD_GUIA
        else -> invoice.tipoDoc
    }
    return TIPO_DOC_TITULO[cod] ?: invoice.etiquetaTipo().uppercase(Locale("es", "PE"))
}

private fun resolveEmisor(invoice: Invoice, fallback: Company?): Company {
    invoice.company?.takeIf { it.nombre.isNotBlank() || it.ruc.isNotBlank() }?.let { return it }
    fallback?.let { return it.copy(ruc = it.ruc.ifBlank { invoice.companyRuc }) }
    return Company(ruc = invoice.companyRuc, nombre = invoice.companyRuc)
}

private fun direccionTexto(address: Address?): String {
    if (address == null) return ""
    val partes = listOfNotNull(
        address.direccion?.takeIf { it.isNotBlank() },
        listOfNotNull(address.distrito, address.provincia, address.departamento)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
            .takeIf { it.isNotBlank() },
    )
    return partes.joinToString("\n")
}

private fun formatoSoles(value: Double): String =
    String.format(Locale("es", "PE"), "S/ %.2f", value)

private abstract class ComprobantePdfWriterBase(
    protected val document: PdfDocument,
    protected val invoice: Invoice,
    emisorFallback: Company?,
) {
    protected val emisor = resolveEmisor(invoice, emisorFallback)
    protected val qrBitmap: Bitmap? = QrCodeBitmap.encode(SunatQrPayload.build(invoice), 420)

    protected val navy = Color.rgb(30, 58, 95)
    protected val accent = Color.rgb(37, 99, 235)
    protected val textPrimary = Color.rgb(31, 41, 55)
    protected val textMuted = Color.rgb(107, 114, 128)
    protected val border = Color.rgb(229, 231, 235)
    protected val surfaceSoft = Color.rgb(248, 250, 252)

    protected val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = navy
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    protected val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = navy
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    protected val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textPrimary
        textSize = 9.5f
    }
    protected val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textMuted
        textSize = 8f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    protected val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textMuted
        textSize = 7f
    }
    protected val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = border
        strokeWidth = 1f
    }
    protected val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    protected val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = border
        strokeWidth = 1f
    }

    abstract fun render()

    protected fun drawQr(canvas: Canvas, x: Float, y: Float, size: Float) {
        val bmp = qrBitmap ?: return
        val dst = RectF(x, y, x + size, y + size)
        canvas.drawBitmap(bmp, null, dst, null)
    }

    protected fun wrappedHeight(text: String, maxWidth: Float, paint: Paint, lineHeight: Float): Float {
        var remaining = text
        var lines = 0
        while (remaining.isNotBlank()) {
            val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
            var line = remaining.take(count)
            if (count < remaining.length) {
                val sp = line.lastIndexOf(' ')
                if (sp > 0) line = line.take(sp)
            }
            remaining = remaining.drop(line.length).trimStart()
            lines++
        }
        return lines.coerceAtLeast(1) * lineHeight
    }

    protected fun wrappedCenteredHeight(
        text: String,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float,
    ): Float {
        val measurePaint = Paint(paint).apply { textAlign = Paint.Align.LEFT }
        return wrappedHeight(text, maxWidth, measurePaint, lineHeight)
    }

    protected fun drawWrappedCentered(
        canvas: Canvas,
        text: String,
        centerX: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float = 12f,
    ): Float {
        var y = startY
        var remaining = text
        val linePaint = Paint(paint).apply { textAlign = Paint.Align.LEFT }
        while (remaining.isNotBlank()) {
            val count = linePaint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
            var line = remaining.take(count)
            if (count < remaining.length) {
                val sp = line.lastIndexOf(' ')
                if (sp > 0) line = line.take(sp)
            }
            val trimmed = line.trim()
            val lineWidth = linePaint.measureText(trimmed)
            canvas.drawText(trimmed, centerX - lineWidth / 2f, y, linePaint)
            remaining = remaining.drop(line.length).trimStart()
            y += lineHeight
        }
        return y
    }

    protected fun drawDashedLine(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        val dashed = Paint(linePaint).apply {
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }
        canvas.drawLine(x1, y1, x2, y2, dashed)
    }

    protected fun drawWrapped(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float = 12f,
    ): Float {
        var y = startY
        var remaining = text
        while (remaining.isNotBlank()) {
            val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
            var line = remaining.take(count)
            if (count < remaining.length) {
                val sp = line.lastIndexOf(' ')
                if (sp > 0) line = line.take(sp)
            }
            canvas.drawText(line.trim(), x, y, paint)
            remaining = remaining.drop(line.length).trimStart()
            y += lineHeight
        }
        return y
    }
}

private class ComprobanteA4Writer(
    document: PdfDocument,
    invoice: Invoice,
    emisorFallback: Company?,
) : ComprobantePdfWriterBase(document, invoice, emisorFallback) {

    private val pageW = 595
    private val pageH = 842
    private val margin = 40f
    private val contentW = pageW - margin * 2

    override fun render() {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, 1).create())
        val canvas = page.canvas
        var y = margin

        // Franja superior
        fillPaint.color = navy
        canvas.drawRect(margin, y, margin + contentW, y + 4f, fillPaint)
        y += 14f

        val boxW = 188f
        val boxX = margin + contentW - boxW

        // Emisor (izquierda)
        val nombreComercial = emisor.nombreComercial?.takeIf { it.isNotBlank() } ?: emisor.nombre
        canvas.drawText(nombreComercial, margin, y + 14f, brandPaint)
        var leftY = y + 32f
        if (emisor.nombre.isNotBlank() && emisor.nombre != nombreComercial) {
            canvas.drawText(emisor.nombre, margin, leftY, bodyPaint)
            leftY += 12f
        }
        val rucEmisor = emisor.ruc.ifBlank { invoice.companyRuc }
        canvas.drawText("RUC $rucEmisor", margin, leftY, bodyPaint)
        leftY += 12f
        direccionTexto(emisor.address).takeIf { it.isNotBlank() }?.let {
            leftY = drawWrapped(canvas, it, margin, leftY, contentW - boxW - 14f, bodyPaint)
        }

        // Caja documento (derecha)
        val boxY = y
        val boxH = 72f
        fillPaint.color = surfaceSoft
        canvas.drawRoundRect(RectF(boxX, boxY, boxX + boxW, boxY + boxH), 8f, 8f, fillPaint)
        canvas.drawRoundRect(RectF(boxX, boxY, boxX + boxW, boxY + boxH), 8f, 8f, strokePaint)

        val docTitlePaint = Paint(titlePaint).apply {
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(tituloDocumento(invoice), boxX + boxW / 2f, boxY + 18f, docTitlePaint)
        val numPaint = Paint(brandPaint).apply {
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(invoice.etiquetaCompleta, boxX + boxW / 2f, boxY + 40f, numPaint)
        val fechaPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.CENTER }
        invoice.fechaEmisionLegible()?.let {
            canvas.drawText(it, boxX + boxW / 2f, boxY + 58f, fechaPaint)
        }

        y = maxOf(leftY, boxY + boxH) + 16f
        canvas.drawLine(margin, y, margin + contentW, y, linePaint)
        y += 14f

        // Bloque cliente
        val clientH = 58f
        fillPaint.color = surfaceSoft
        canvas.drawRoundRect(RectF(margin, y, margin + contentW, y + clientH), 6f, 6f, fillPaint)
        canvas.drawText("CLIENTE / RECEPTOR", margin + 10f, y + 14f, labelPaint)
        val receptor = invoice.receptor
        canvas.drawText(receptor.nombre, margin + 10f, y + 30f, Paint(bodyPaint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        })
        canvas.drawText(
            "Doc. ${receptor.documentoTipo}: ${receptor.documentoNumero}",
            margin + 10f,
            y + 44f,
            bodyPaint,
        )
        y += clientH + 12f

        invoice.motivoNota?.takeIf { it.isNotBlank() }?.let { motivo ->
            canvas.drawText("Motivo: $motivo", margin, y, bodyPaint)
            y += 14f
        }
        invoice.documentoAfectado?.let { doc ->
            canvas.drawText(
                "Doc. afectado: ${doc.serie}-${doc.correlativo}",
                margin,
                y,
                bodyPaint,
            )
            y += 14f
        }

        // Tabla
        y = drawTable(canvas, y, invoice.details)

        // Totales
        y += 8f
        val totalsW = 200f
        val totalsX = margin + contentW - totalsW
        val letrasH = wrappedHeight(MontoEnLetras.soles(invoice.totales.total), totalsX - margin - 8f, bodyPaint, 12f)
        drawWrapped(
            canvas,
            MontoEnLetras.soles(invoice.totales.total),
            margin,
            y,
            totalsX - margin - 8f,
            bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) },
        )

        var ty = y
        val filas = listOf(
            "Op. gravada" to invoice.totales.mtoOperGravadas,
            "Op. exonerada" to invoice.totales.mtoOperExoneradas,
            "Op. inafecta" to invoice.totales.mtoOperInafectas,
            "IGV (18%)" to invoice.totales.igv,
        )
        filas.forEach { (label, monto) ->
            val valText = if (monto != null) formatoSoles(monto) else "S/ 0.00"
            canvas.drawText(label, totalsX, ty, labelPaint)
            canvas.drawText(valText, totalsX + totalsW - bodyPaint.measureText(valText), ty + 9f, bodyPaint)
            ty += 22f
        }
        fillPaint.color = navy
        canvas.drawRoundRect(RectF(totalsX, ty, totalsX + totalsW, ty + 26f), 4f, 4f, fillPaint)
        val totalLabel = Paint(bodyPaint).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("TOTAL", totalsX + 10f, ty + 17f, totalLabel)
        val totalVal = formatoSoles(invoice.totales.total)
        canvas.drawText(totalVal, totalsX + totalsW - totalLabel.measureText(totalVal) - 10f, ty + 17f, totalLabel)

        y = maxOf(y + letrasH, ty + 36f) + 10f

        // QR + pie legal
        val qrSize = 88f
        drawQr(canvas, margin, y, qrSize)
        val pieX = margin + qrSize + 12f
        val pieW = contentW - qrSize - 12f
        canvas.drawText("Código QR SUNAT", pieX, y + 10f, labelPaint)
        drawWrapped(canvas, SUNAT_PIE, pieX, y + 22f, pieW, smallPaint, 9f)
        invoice.hashCpe?.takeIf { it.isNotBlank() }?.let { hash ->
            drawWrapped(canvas, "Hash: $hash", pieX, y + qrSize - 8f, pieW, smallPaint, 8f)
        }

        invoice.observaciones?.takeIf { it.isNotBlank() }?.let { obs ->
            y += qrSize + 10f
            canvas.drawText("Observaciones: $obs", margin, y, smallPaint)
        }

        document.finishPage(page)
    }

    private fun drawTable(canvas: Canvas, startY: Float, details: List<SaleDetail>): Float {
        var y = startY
        val cols = floatArrayOf(36f, 220f, 52f, 68f, 72f)
        val headers = arrayOf("Cant.", "Descripción", "P.U.", "IGV", "Total")

        fillPaint.color = navy
        canvas.drawRect(margin, y, margin + contentW, y + 20f, fillPaint)
        val headerPaint = Paint(bodyPaint).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 8f
        }
        var cx = margin + 6f
        headers.forEachIndexed { i, h ->
            canvas.drawText(h, cx, y + 14f, headerPaint)
            cx += cols[i]
        }
        y += 22f

        details.forEachIndexed { index, line ->
            val desc = line.textoEnComprobante().ifBlank { "Ítem" }
            val rowH = maxOf(18f, wrappedHeight(desc, cols[1] - 8f, bodyPaint, 11f) + 6f)
            if (index % 2 == 1) {
                fillPaint.color = surfaceSoft
                canvas.drawRect(margin, y, margin + contentW, y + rowH, fillPaint)
            }
            var colX = margin + 6f
            canvas.drawText(String.format(Locale.US, "%.2f", line.cantidad), colX, y + 12f, bodyPaint)
            colX += cols[0]
            drawWrapped(canvas, desc, colX, y + 12f, cols[1] - 8f, bodyPaint, 11f)
            colX += cols[1]
            canvas.drawText(formatoSoles(line.precioUnitario), colX, y + 12f, bodyPaint)
            colX += cols[2]
            canvas.drawText(formatoSoles(line.igv), colX, y + 12f, bodyPaint)
            colX += cols[3]
            canvas.drawText(formatoSoles(line.total), colX, y + 12f, bodyPaint)
            y += rowH
            canvas.drawLine(margin, y, margin + contentW, y, linePaint)
        }
        return y
    }
}

private class ComprobanteTicketWriter(
    document: PdfDocument,
    invoice: Invoice,
    emisorFallback: Company?,
) : ComprobantePdfWriterBase(document, invoice, emisorFallback) {

    private val ticketW = 226
    private val margin = 10f
    private val contentW = ticketW - margin * 2
    private val centerX = ticketW / 2f

    override fun render() {
        val pageH = layoutTicket(canvas = null).toInt().coerceIn(420, 2400)
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(ticketW, pageH, 1).create(),
        )
        layoutTicket(canvas = page.canvas)
        document.finishPage(page)
    }

    /** Calcula altura o dibuja el ticket (canvas null = solo medir). */
    private fun layoutTicket(canvas: Canvas?): Float {
        var y = 14f

        fun sep(spacing: Float = 10f) {
            canvas?.let { drawDashedLine(it, margin, y, ticketW - margin, y) }
            y += spacing
        }

        val headerPaint = Paint(brandPaint).apply { textSize = 11f }
        val subCenterPaint = Paint(smallPaint).apply { textSize = 7.5f }
        val docTitlePaint = Paint(titlePaint).apply { textSize = 8f }
        val numeroPaint = Paint(brandPaint).apply { textSize = 10.5f }
        val itemTitlePaint = Paint(bodyPaint).apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val itemMetaPaint = Paint(smallPaint).apply { textSize = 7.5f }
        val totalPaint = Paint(brandPaint).apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rightPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT; textSize = 8f }
        val thanksPaint = Paint(bodyPaint).apply {
            textAlign = Paint.Align.CENTER
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = accent
        }

        val nombre = emisor.nombreComercial?.takeIf { it.isNotBlank() } ?: emisor.nombre
        y = if (canvas != null) {
            drawWrappedCentered(canvas, nombre, centerX, y, contentW, headerPaint, 12f)
        } else {
            y + wrappedCenteredHeight(nombre, contentW, headerPaint, 12f)
        }
        y += 2f

        val rucLine = "RUC ${emisor.ruc.ifBlank { invoice.companyRuc }}"
        y = if (canvas != null) {
            drawWrappedCentered(canvas, rucLine, centerX, y, contentW, subCenterPaint, 9f)
        } else {
            y + wrappedCenteredHeight(rucLine, contentW, subCenterPaint, 9f)
        }

        val dir = emisor.address?.lineaPrincipal?.takeIf { it.isNotBlank() }
            ?: direccionTexto(emisor.address).takeIf { it.isNotBlank() }
        if (dir != null) {
            y += 2f
            y = if (canvas != null) {
                drawWrappedCentered(canvas, dir, centerX, y, contentW, subCenterPaint, 9f)
            } else {
                y + wrappedCenteredHeight(dir, contentW, subCenterPaint, 9f)
            }
        }

        y += 4f
        sep()

        val titulo = tituloDocumento(invoice)
        y = if (canvas != null) {
            drawWrappedCentered(canvas, titulo, centerX, y, contentW, docTitlePaint, 10f)
        } else {
            y + wrappedCenteredHeight(titulo, contentW, docTitlePaint, 10f)
        }
        y += 2f
        y = if (canvas != null) {
            drawWrappedCentered(canvas, invoice.etiquetaCompleta, centerX, y, contentW, numeroPaint, 12f)
        } else {
            y + wrappedCenteredHeight(invoice.etiquetaCompleta, contentW, numeroPaint, 12f)
        }
        invoice.fechaEmisionLegible()?.let { fecha ->
            y += 2f
            y = if (canvas != null) {
                drawWrappedCentered(canvas, fecha, centerX, y, contentW, subCenterPaint, 9f)
            } else {
                y + wrappedCenteredHeight(fecha, contentW, subCenterPaint, 9f)
            }
        }

        y += 4f
        sep()

        canvas?.drawText("CLIENTE", margin, y, labelPaint)
        y += 10f
        val clienteNombre = invoice.receptor.nombre.ifBlank { "—" }
        y = if (canvas != null) {
            drawWrapped(canvas, clienteNombre, margin, y, contentW, Paint(bodyPaint).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 8.5f
            }, 10f)
        } else {
            y + wrappedHeight(clienteNombre, contentW, bodyPaint, 10f)
        }
        val docCliente = "${invoice.receptor.documentoTipo}: ${invoice.receptor.documentoNumero}"
        y = if (canvas != null) {
            drawWrapped(canvas, docCliente, margin, y, contentW, itemMetaPaint, 9f)
        } else {
            y + wrappedHeight(docCliente, contentW, itemMetaPaint, 9f)
        }

        y += 4f
        sep()

        canvas?.drawText("DETALLE", margin, y, labelPaint)
        y += 10f

        invoice.details.forEach { line ->
            val desc = line.textoEnComprobante().ifBlank { "Ítem" }
            y = if (canvas != null) {
                drawWrapped(canvas, desc, margin, y, contentW, itemTitlePaint, 10f)
            } else {
                y + wrappedHeight(desc, contentW, itemTitlePaint, 10f)
            }
            val qty = if (line.cantidad % 1.0 == 0.0) {
                line.cantidad.toInt().toString()
            } else {
                String.format(Locale("es", "PE"), "%.2f", line.cantidad)
            }
            val qtyLine = "$qty x ${formatoSoles(line.precioUnitario)}"
            val totalLine = formatoSoles(line.total)
            if (canvas != null) {
                canvas.drawText(qtyLine, margin, y, itemMetaPaint)
                canvas.drawText(totalLine, ticketW - margin, y, rightPaint)
            }
            y += 11f
        }

        y += 2f
        sep()

        val totalsTop = y
        y += 6f
        val gravada = formatoSoles(invoice.totales.mtoOperGravadas ?: 0.0)
        val igv = formatoSoles(invoice.totales.igv)
        val total = formatoSoles(invoice.totales.total)
        if (canvas != null) {
            canvas.drawText("Op. gravada", margin, y, itemMetaPaint)
            canvas.drawText(gravada, ticketW - margin, y, rightPaint)
        }
        y += 10f
        if (canvas != null) {
            canvas.drawText("IGV (18%)", margin, y, itemMetaPaint)
            canvas.drawText(igv, ticketW - margin, y, rightPaint)
        }
        y += 12f
        if (canvas != null) {
            fillPaint.color = surfaceSoft
            canvas.drawRoundRect(margin, y - 2f, ticketW - margin, y + 14f, 4f, 4f, fillPaint)
            strokePaint.color = border
            canvas.drawRoundRect(margin, y - 2f, ticketW - margin, y + 14f, 4f, 4f, strokePaint)
            canvas.drawText("TOTAL", margin + 6f, y + 9f, Paint(labelPaint).apply { color = navy })
            canvas.drawText(
                total,
                ticketW - margin - 6f,
                y + 10f,
                Paint(totalPaint).apply { textAlign = Paint.Align.RIGHT },
            )
        }
        y += 18f

        val letras = MontoEnLetras.soles(invoice.totales.total)
        y = if (canvas != null) {
            drawWrappedCentered(canvas, letras, centerX, y, contentW, subCenterPaint, 9f)
        } else {
            y + wrappedCenteredHeight(letras, contentW, subCenterPaint, 9f)
        }

        y += 6f
        sep()

        val qrSize = 68f
        if (canvas != null) {
            val qrX = (ticketW - qrSize) / 2f
            fillPaint.color = Color.WHITE
            canvas.drawRoundRect(qrX - 3f, y - 3f, qrX + qrSize + 3f, y + qrSize + 3f, 6f, 6f, fillPaint)
            canvas.drawRoundRect(qrX - 3f, y - 3f, qrX + qrSize + 3f, y + qrSize + 3f, 6f, 6f, strokePaint)
            drawQr(canvas, qrX, y, qrSize)
        }
        y += qrSize + 8f

        y = if (canvas != null) {
            drawWrappedCentered(canvas, SUNAT_PIE, centerX, y, contentW, smallPaint, 8f)
        } else {
            y + wrappedCenteredHeight(SUNAT_PIE, contentW, smallPaint, 8f)
        }

        y += 6f
        canvas?.drawText("¡Gracias por su compra!", centerX, y, thanksPaint)
        y += 14f

        return y.coerceAtLeast(totalsTop + 80f)
    }
}
