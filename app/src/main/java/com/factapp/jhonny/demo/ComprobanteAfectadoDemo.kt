package com.factapp.jhonny.demo

import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.aLineaCatalogoItem

/**
 * Simula las líneas del comprobante original al emitir nota de crédito (sin API aún).
 * En producción: GET comprobante/{serie-numero}/lineas
 */
object ComprobanteAfectadoDemo {

    fun referenciaEsValida(docReferencia: String): Boolean {
        val ref = docReferencia.trim()
        return ref.length >= 10 && ref.contains("-")
    }

    fun lineasParaNotaCredito(
        docReferencia: String,
        catalogo: List<CatalogItem>,
    ): List<LineaCatalogoItem> {
        if (!referenciaEsValida(docReferencia) || catalogo.isEmpty()) return emptyList()

        val productos = catalogo.filter { it.esProducto }.take(2)
        val servicios = catalogo.filter { it.esServicio }.take(1)
        val origen = (productos + servicios).ifEmpty { catalogo.take(3) }

        return origen.mapIndexed { index, item ->
            item.aLineaCatalogoItem(cantidad = if (index == 0) 2.0 else 1.0)
        }
    }
}
