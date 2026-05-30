package com.factapp.jhonny

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
        titulo = "Guía de emisión",
        detalle = "Traslado vinculado a una o más facturas",
        serie = "T001",
    );

    val esNota: Boolean
        get() = this == NOTA_CREDITO || this == NOTA_DEBITO

    val esGuiaEmision: Boolean
        get() = this == GUIA_EMISION

    /** Valor enviado al API en [com.factapp.jhonny.network.dto.EmitirComprobanteRequest.tipo]. */
    val tipoApi: String
        get() = name
}
