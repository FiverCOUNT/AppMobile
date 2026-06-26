package com.factapp.jhonny

import com.factapp.jhonny.network.dto.model.dniValido

enum class TipoComprobante(
    val titulo: String,
    val detalle: String,
    val serie: String,
) {
    FACTURA(
        titulo = "Factura electrónica",
        detalle = "Venta gravada a cliente con RUC",
        serie = "F001",
    ),
    BOLETA(
        titulo = "Boleta electrónica",
        detalle = "Consumidor final con DNI",
        serie = "B001",
    ),
    NOTA_CREDITO(
        titulo = "Nota de crédito",
        detalle = "Anulación o descuento sobre documento",
        serie = "FC01",
    ),
    NOTA_DEBITO(
        titulo = "Nota de débito",
        detalle = "Ajuste de importe sobre documento",
        serie = "FD01",
    ),
    GUIA_EMISION(
        titulo = "GRE remitente",
        detalle = "Guía de remisión electrónica · remitente",
        serie = "T001",
    ),
    GUIA_TRANSPORTISTA(
        titulo = "GRE transportista",
        detalle = "Guía de remisión electrónica · transportista",
        serie = "V001",
    );

    val esNota: Boolean
        get() = this == NOTA_CREDITO || this == NOTA_DEBITO

    val esGuiaEmision: Boolean
        get() = this == GUIA_EMISION

    val esGuiaTransportista: Boolean
        get() = this == GUIA_TRANSPORTISTA

    val esGuiaRemision: Boolean
        get() = esGuiaEmision || esGuiaTransportista

    /** Valor enviado al API en [com.factapp.jhonny.network.dto.EmitirComprobanteRequest.tipo]. */
    val tipoApi: String
        get() = name

    /** Documento del receptor completo para buscar cliente registrado. */
    fun docReceptorListo(doc: String): Boolean {
        val d = doc.filter { it.isDigit() }
        return when (this) {
            FACTURA, NOTA_CREDITO, NOTA_DEBITO -> d.length == 11
            BOLETA -> dniValido(d)
            GUIA_EMISION, GUIA_TRANSPORTISTA -> d.length == 8 || d.length == 11
        }
    }
}
