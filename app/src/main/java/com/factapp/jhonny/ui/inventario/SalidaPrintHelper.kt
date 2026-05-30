package com.factapp.jhonny.ui.inventario

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.etiquetaDestino
import com.factapp.jhonny.network.dto.formatCantidadConUnidad
import com.factapp.jhonny.network.dto.model.resumenSeries
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object SalidaPrintHelper {

    fun imprimirSalida(
        context: Context,
        salida: Movimiento,
        catalogo: Map<String, CatalogItem>,
        almacenes: Map<String, Almacen>,
    ): Boolean = runCatching {
        val file = crearPdfSalida(context, salida, catalogo, almacenes)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(
            "Salida ${salida.numeroDisplay}",
            PdfFilePrintAdapter(file),
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build(),
        )
        true
    }.getOrElse {
        Toast.makeText(context, "No se pudo preparar la impresión", Toast.LENGTH_LONG).show()
        false
    }

    private fun crearPdfSalida(
        context: Context,
        salida: Movimiento,
        catalogo: Map<String, CatalogItem>,
        almacenes: Map<String, Almacen>,
    ): File {
        val dir = File(context.cacheDir, "salidas_pdf").apply { mkdirs() }
        val nombreArchivo = "salida_${salida.numeroDisplay.replace(Regex("[^A-Za-z0-9_-]"), "_")}.pdf"
        val file = File(dir, nombreArchivo)

        val document = PdfDocument()
        try {
            val writer = SalidaPdfWriter(document)
            writer.header("CONSTANCIA DE SALIDA", salida.numeroDisplay)

            val fecha = salida.fechaDespacho ?: salida.fecha
            val origen = almacenes[salida.almacenId]?.nombre ?: salida.almacenId
            val destino = salida.etiquetaDestino(almacenes) ?: "Sin destino"
            val cliente = salida.cliente

            writer.section("Datos de la salida")
            writer.keyValue("Numero", salida.numeroDisplay)
            writer.keyValue("Fecha", fecha.take(16).replace('T', ' '))
            writer.keyValue("Estado", salida.estado?.name ?: "Sin estado")
            writer.keyValue("Origen", origen)
            writer.keyValue("Destino", destino)
            cliente?.let {
                writer.keyValue("Receptor", it.razonSocial ?: "Doc. ${it.numeroDoc}")
                writer.keyValue("Documento", "${it.tipoDoc} - ${it.numeroDoc}")
            }
            salida.observaciones?.takeIf { it.isNotBlank() }?.let {
                writer.keyValue("Observaciones", it)
            }

            writer.section("Productos")
            writer.tableHeader()
            salida.lineas.forEachIndexed { index, linea ->
                val item = catalogo[linea.catalogItemId]
                val nombre = item?.nombre ?: linea.nombreEfectivo
                val unidad = item?.unidad ?: "NIU"
                val cantidad = formatCantidadConUnidad(linea.cantidad, unidad)
                val series = linea.resumenSeries()
                writer.productRow(index + 1, nombre, cantidad, series)
            }

            writer.signature()
            writer.finish()

            FileOutputStream(file).use { output ->
                document.writeTo(output)
            }
        } finally {
            document.close()
        }
        return file
    }
}

private class PdfFilePrintAdapter(
    private val file: File,
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder(file.name)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build(),
            true,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onWriteCancelled()
            return
        }
        val target = destination ?: run {
            callback.onWriteFailed("Destino de impresion no disponible")
            return
        }
        runCatching {
            FileInputStream(file).use { input ->
                FileOutputStream(target.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
        }.onSuccess {
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }.onFailure {
            callback.onWriteFailed(it.message)
        }
    }
}

private class SalidaPdfWriter(
    private val document: PdfDocument,
) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 42f
    private var pageNumber = 0
    private var page: PdfDocument.Page = newPage()
    private var canvas: Canvas = page.canvas
    private var y = 52f

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(20, 45, 70)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(72, 94, 114)
        textSize = 12f
    }
    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(20, 45, 70)
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(72, 94, 114)
        textSize = 10.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(35, 48, 60)
        textSize = 11f
    }
    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(91, 111, 130)
        textSize = 9.5f
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(218, 226, 234)
        strokeWidth = 1f
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(25, 118, 210)
        strokeWidth = 2f
    }

    fun header(title: String, number: String) {
        canvas.drawText(title, margin, y, titlePaint)
        canvas.drawText("Movimiento $number", margin, y + 22f, subtitlePaint)
        canvas.drawLine(margin, y + 38f, pageWidth - margin, y + 38f, accentPaint)
        y += 64f
    }

    fun section(title: String) {
        ensure(38f)
        y += 8f
        canvas.drawText(title, margin, y, sectionPaint)
        y += 16f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 14f
    }

    fun keyValue(label: String, value: String) {
        ensure(28f)
        canvas.drawText(label.uppercase(), margin, y, labelPaint)
        wrappedText(value, margin + 116f, y, pageWidth - margin * 2 - 116f, bodyPaint)
        y += 20f
    }

    fun tableHeader() {
        ensure(28f)
        canvas.drawText("#", margin, y, labelPaint)
        canvas.drawText("PRODUCTO", margin + 28f, y, labelPaint)
        canvas.drawText("CANTIDAD", pageWidth - margin - 88f, y, labelPaint)
        y += 12f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 14f
    }

    fun productRow(index: Int, name: String, quantity: String, series: String?) {
        ensure(52f)
        val rowStart = y
        canvas.drawText(index.toString(), margin, y, bodyPaint)
        val linesUsed = wrappedText(name, margin + 28f, y, 300f, bodyPaint)
        canvas.drawText(quantity, pageWidth - margin - 88f, y, bodyPaint)
        y = rowStart + (linesUsed * 14f)
        if (!series.isNullOrBlank()) {
            y += 4f
            wrappedText("Serie(s): $series", margin + 28f, y, pageWidth - margin * 2 - 28f, smallPaint)
            y += 14f
        }
        y += 8f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 12f
    }

    fun signature() {
        ensure(130f)
        y += 38f
        val start = margin + 55f
        val end = pageWidth - margin - 55f
        canvas.drawLine(start, y, end, y, linePaint)
        y += 18f
        centered("Nombre y firma de quien recibe la salida", start, end, bodyPaint)
        y += 18f
        centered("DNI / documento: ______________________________", start, end, smallPaint)
    }

    fun finish() {
        footer()
        document.finishPage(page)
    }

    private fun wrappedText(
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
    ): Int {
        var currentY = startY
        var remaining = text
        var count = 0
        while (remaining.isNotBlank()) {
            val chars = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
            var line = remaining.take(chars)
            if (chars < remaining.length) {
                val lastSpace = line.lastIndexOf(' ')
                if (lastSpace > 0) line = line.take(lastSpace)
            }
            canvas.drawText(line.trim(), x, currentY, paint)
            remaining = remaining.drop(line.length).trimStart()
            currentY += 14f
            count += 1
        }
        return count.coerceAtLeast(1)
    }

    private fun centered(text: String, start: Float, end: Float, paint: Paint) {
        val width = paint.measureText(text)
        canvas.drawText(text, start + ((end - start - width) / 2f), y, paint)
    }

    private fun ensure(space: Float) {
        if (y + space <= pageHeight - 48f) return
        footer()
        document.finishPage(page)
        page = newPage()
        canvas = page.canvas
        y = 52f
    }

    private fun footer() {
        canvas.drawText("Generado desde FactApp", margin, pageHeight - 28f, smallPaint)
        canvas.drawText("Pagina $pageNumber", pageWidth - margin - 52f, pageHeight - 28f, smallPaint)
    }

    private fun newPage(): PdfDocument.Page {
        pageNumber += 1
        return document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create(),
        )
    }
}
