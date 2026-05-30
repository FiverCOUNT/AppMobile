package com.factapp.jhonny.ui.clientes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.lineaPrincipal

object ClienteContactIntents {

    fun llamar(context: Context, telefono: String): Boolean {
        val normalizado = normalizarTelefono(telefono) ?: return aviso(context, "Número no válido")
        return ejecutar(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalizado")))
    }

    fun abrirWhatsApp(context: Context, telefono: String): Boolean {
        val wa = normalizarWhatsApp(telefono) ?: return aviso(context, "Número no válido para WhatsApp")
        val uri = Uri.parse("https://wa.me/$wa")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            return ejecutar(context, intent)
        }
        return ejecutar(context, Intent(Intent.ACTION_VIEW, uri))
    }

    fun compartirCliente(context: Context, cliente: Cliente): Boolean {
        val texto = buildString {
            appendLine("📋 Cliente")
            appendLine(cliente.razonSocial)
            appendLine(cliente.etiquetaDocumento)
            cliente.telefono?.takeIf { it.isNotBlank() }?.let { appendLine("Teléfono: $it") }
            cliente.address.lineaPrincipal?.takeIf { it.isNotBlank() }?.let { appendLine("Dirección: $it") }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Cliente: ${cliente.razonSocial}")
            putExtra(Intent.EXTRA_TEXT, texto.trim())
        }
        return ejecutar(context, Intent.createChooser(intent, "Enviar datos del cliente"))
    }

    private fun normalizarTelefono(raw: String): String? {
        val limpio = raw.filter { it.isDigit() || it == '+' }
        if (limpio.isBlank()) return null
        return limpio
    }

    /** Dígitos internacionales sin + (Perú: 51 + 9 dígitos si empieza en 9). */
    fun normalizarWhatsApp(raw: String): String? {
        var digits = raw.filter { it.isDigit() }
        if (digits.isBlank()) return null
        if (digits.length == 9 && digits.startsWith("9")) digits = "51$digits"
        if (digits.startsWith("51") && digits.length >= 11) return digits
        if (digits.length >= 10) return digits
        return null
    }

    private fun ejecutar(context: Context, intent: Intent): Boolean =
        runCatching {
            context.startActivity(intent)
            true
        }.getOrElse {
            aviso(context, "No se pudo abrir la aplicación")
            false
        }

    private fun aviso(context: Context, mensaje: String): Boolean {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        return false
    }
}
