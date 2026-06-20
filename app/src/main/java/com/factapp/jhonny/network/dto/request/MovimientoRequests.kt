package com.factapp.jhonny.network.dto.request

import com.factapp.jhonny.network.dto.model.MovimientoCliente
import com.google.gson.annotations.SerializedName

/** Body POST ingreso de mercadería al almacén. */
data class RegistrarEntradaRequest(
    @SerializedName("company_ruc")
    val companyRuc: String,
    @SerializedName("almacen_id")
    val almacenId: String,
    val lineas: List<RegistrarMovimientoLineaRequest>,
    val observaciones: String? = null,
    @SerializedName("cliente_id")
    val clienteId: String? = null,
    val cliente: MovimientoCliente? = null,
) {
    /** Payload enviado al API (RUC va en la URL). */
    fun toApiBody(): RegistrarEntradaApiBody = RegistrarEntradaApiBody(
        almacenId = almacenId,
        lineas = lineas,
        observaciones = observaciones,
        clienteId = clienteId,
        cliente = cliente,
    )
}

/** Body real de `POST /empresas/{ruc}/inventario/entradas`. */
data class RegistrarEntradaApiBody(
    @SerializedName("almacen_id")
    val almacenId: String,
    val lineas: List<RegistrarMovimientoLineaRequest>,
    val observaciones: String? = null,
    @SerializedName("cliente_id")
    val clienteId: String? = null,
    val cliente: MovimientoCliente? = null,
)

/**
 * Body POST salida / entrega / traslado.
 */
data class RegistrarSalidaRequest(
    @SerializedName("company_ruc")
    val companyRuc: String,
    @SerializedName("almacen_id")
    val almacenId: String,
    @SerializedName("almacen_destino_id")
    val almacenDestinoId: String? = null,
    @SerializedName("comprobante_id")
    val comprobanteId: String? = null,
    @SerializedName("guia_remision_id")
    val guiaRemisionId: String? = null,
    val lineas: List<RegistrarMovimientoLineaRequest>,
    @SerializedName("cliente_id")
    val clienteId: String? = null,
    /** Solo si no hay [clienteId]: el backend crea o reutiliza un registro en `clientes`. */
    val cliente: MovimientoCliente? = null,
)

/**
 * Línea al registrar un movimiento (entrada o salida).
 */
data class RegistrarMovimientoLineaRequest(
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    val cantidad: Double,
    @SerializedName("series")
    val numerosSerie: List<String>? = null,
    @SerializedName("serie_ids")
    val serieIds: List<String>? = null,
)
