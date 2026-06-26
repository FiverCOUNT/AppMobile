package com.factapp.jhonny

/** Opciones de guía de remisión electrónica según catálogo SUNAT. */
enum class GuiaRemisionOpcion(
    val titulo: String,
    val detalle: String,
    val codigoSunat: String,
) {
    REMITENTE(
        titulo = "GRE remitente",
        detalle = "Traslado emitido por quien envía las mercancías",
        codigoSunat = "09",
    ),
    TRANSPORTISTA(
        titulo = "GRE transportista",
        detalle = "Traslado emitido por el transportista",
        codigoSunat = "31",
    ),
    EVENTOS(
        titulo = "Eventos",
        detalle = "Registro de eventos y comunicaciones a SUNAT",
        codigoSunat = "",
    ),
}

/** Eventos de guía de remisión electrónica (Resolución SUNAT). */
enum class GuiaRemisionEventoOpcion(
    val titulo: String,
    val detalle: String,
) {
    REGISTRO_EVENTOS(
        titulo = "Registro de eventos",
        detalle = "Inicio de traslado, llegada, entrega y otros eventos",
    ),
    COMUNICACION_BAJA(
        titulo = "Comunicación de baja",
        detalle = "Dar de baja una guía de remisión emitida",
    ),
}
