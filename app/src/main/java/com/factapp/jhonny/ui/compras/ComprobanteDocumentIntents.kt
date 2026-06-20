package com.factapp.jhonny.ui.compras

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.ComprobanteRepository.PdfFormato
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.ui.comprobantes.ComprobantePdfHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ComprobanteDocumentIntents {

    /**
     * Genera el PDF en el dispositivo (QR SUNAT + diseño FactApp) y lo abre.
     * Si falla la generación local, intenta descargarlo del backend.
     */
    suspend fun abrirPdf(
        context: Context,
        comprobante: Invoice,
        companyRuc: String,
        token: String?,
        formato: PdfFormato = PdfFormato.A4,
        emisorFallback: Company? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        if (comprobante.id.isBlank()) {
            return@withContext aviso(context, "PDF no disponible")
        }

        val bytesLocal = ComprobantePdfHelper.generarPdfBytes(comprobante, formato, emisorFallback)
        if (bytesLocal != null && bytesLocal.isNotEmpty()) {
            val sufijo = if (formato == PdfFormato.TICKET) "_ticket" else ""
            return@withContext abrirPdfLocal(context, bytesLocal, "${comprobante.etiquetaCompleta}$sufijo")
        }

        val ruc = companyRuc.ifBlank { comprobante.companyRuc }
        if (ruc.isBlank()) return@withContext aviso(context, "PDF no disponible")
        if (token.isNullOrBlank()) {
            return@withContext aviso(context, "Inicia sesión para descargar el PDF")
        }

        ComprobanteRepository.descargarArchivo(
            companyRuc = ruc,
            token = token,
            comprobanteId = comprobante.id,
            tipo = "pdf",
            formatoPdf = formato,
        ).fold(
            onSuccess = { bytes ->
                val sufijo = if (formato == PdfFormato.TICKET) "_ticket" else ""
                abrirPdfLocal(context, bytes, "${comprobante.etiquetaCompleta}$sufijo")
            },
            onFailure = {
                aviso(context, it.message ?: "No se pudo generar el PDF")
                false
            },
        )
    }

    fun abrirCdrZip(context: Context, compra: Invoice): Boolean {
        val url = compra.cdrZipUrl?.trim().orEmpty()
        if (url.isBlank()) return aviso(context, "CDR no disponible")
        return abrirUrl(context, url, "application/zip", "Descargar CDR")
    }

    private suspend fun abrirPdfLocal(context: Context, bytes: ByteArray, etiqueta: String): Boolean {
        if (bytes.isEmpty()) return aviso(context, "PDF vacío")
        return withContext(Dispatchers.Main) {
            runCatching {
                val dir = File(context.cacheDir, "comprobantes").apply { mkdirs() }
                val nombre = "comprobante_${etiqueta.replace(Regex("[^A-Za-z0-9_-]"), "_")}.pdf"
                val file = File(dir, nombre)
                file.writeBytes(bytes)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Ver PDF"))
                true
            }.getOrElse {
                aviso(context, "No se pudo abrir el PDF")
            }
        }
    }

    private fun abrirUrl(
        context: Context,
        url: String,
        mimeType: String,
        tituloChooser: String,
    ): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull()
            ?: return aviso(context, "Enlace no válido")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        return runCatching {
            context.startActivity(Intent.createChooser(intent, tituloChooser))
            true
        }.getOrElse {
            aviso(context, "No hay aplicación para abrir el archivo")
        }
    }

    private fun aviso(context: Context, mensaje: String): Boolean {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        return false
    }
}
