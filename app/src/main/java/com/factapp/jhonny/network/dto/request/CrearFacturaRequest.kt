package com.factapp.jhonny.network.dto.request

import com.factapp.jhonny.network.dto.model.Legend
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.google.gson.annotations.SerializedName

/**
 * Body para `POST /facturas`.
 *
 * Solo datos de negocio. El backend asigna UBL 2.1, tipo doc 01 y tipo operación por defecto (0101).
 */
data class CrearFacturaRequest(
    val serie: String,
    @SerializedName("fecha_emision")
    val fechaEmision: String,
    @SerializedName("tipo_moneda")
    val tipoMoneda: String = "PEN",
    @SerializedName("forma_pago")
    val formaPago: String = Invoice.FORMA_PAGO_CONTADO,
    @SerializedName("fecha_vencimiento")
    val fechaVencimiento: String? = null,
    @SerializedName("cod_local")
    val codLocal: String? = null,
    val cliente: Company,
    val items: List<CrearFacturaLineaRequest>,
    val leyendas: List<Legend>? = null,
    @SerializedName("enviar_automatico")
    val enviarAutomatico: Boolean? = true,
    val observacion: String? = null,
)

/**
 * Línea al crear factura.
 *
 * El backend recalcula IGV; basta descripcion, unidad, cantidad, precio_unitario, tip_afe_igv.
 */
data class CrearFacturaLineaRequest(
    val codigo: String? = null,
    @SerializedName("catalog_item_id")
    val catalogItemId: String? = null,
    val descripcion: String,
    val unidad: String = "NIU",
    val cantidad: Double,
    @SerializedName("precio_unitario")
    val precioUnitario: Double,
    @SerializedName("tip_afe_igv")
    val tipAfeIgv: String = "10",
    @SerializedName("porcentaje_igv")
    val porcentajeIgv: Double? = 18.0,
)
