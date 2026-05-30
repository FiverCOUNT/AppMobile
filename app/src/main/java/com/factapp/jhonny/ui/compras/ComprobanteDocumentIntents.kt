package com.factapp.jhonny.ui.compras

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.factapp.jhonny.network.dto.model.Invoice

object ComprobanteDocumentIntents {

    fun abrirPdf(context: Context, compra: Invoice): Boolean {
        val url = compra.pdfUrl?.trim().orEmpty()
        if (url.isBlank()) return aviso(context, "PDF no disponible")
        return abrirUrl(context, url, "application/pdf", "Abrir PDF")
    }

    fun abrirCdrZip(context: Context, compra: Invoice): Boolean {
        val url = compra.cdrZipUrl?.trim().orEmpty()
        if (url.isBlank()) return aviso(context, "CDR no disponible")
        return abrirUrl(context, url, "application/zip", "Descargar CDR")
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
        val chooser = Intent.createChooser(intent, tituloChooser)
        return ejecutar(context, chooser)
    }

    private fun ejecutar(context: Context, intent: Intent): Boolean =
        runCatching {
            context.startActivity(intent)
            true
        }.getOrElse {
            aviso(context, "No hay aplicación para abrir el archivo")
            false
        }

    private fun aviso(context: Context, mensaje: String): Boolean {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        return false
    }
}
