package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.network.dto.formatCantidadConUnidad
import com.factapp.jhonny.network.dto.formatFechaLegible
import com.factapp.jhonny.network.dto.formatFechaHoraCompacto
import com.google.gson.annotations.SerializedName

/** Evento de trazabilidad por ítem (línea de movimiento), no por documento completo. */
data class UbicacionProducto(
    val id: String,
    @SerializedName("linea_id")
    val lineaId: String,
    @SerializedName("movimiento_id")
    val movimientoId: String,
    @SerializedName("movimiento_numero")
    val movimientoNumero: String? = null,
    @SerializedName("tipo_movimiento")
    val tipoMovimiento: HistorialItemTipo,
    @SerializedName("es_traslado")
    val esTraslado: Boolean = false,
    val fecha: Long,
    @SerializedName("catalog_item_id")
    val catalogItemId: String,
    @SerializedName("catalog_item_nombre")
    val catalogItemNombre: String,
    @SerializedName("numero_serie")
    val numeroSerie: String? = null,
    val cantidad: Double = 1.0,
    val unidad: String? = null,
    @SerializedName("origen_nombre")
    val origenNombre: String,
    @SerializedName("destino_nombre")
    val destinoNombre: String,
)

enum class HistorialItemTipo {
    @SerializedName("ENTRADA")
    ENTRADA,

    @SerializedName("SALIDA")
    SALIDA,
}

fun UbicacionProducto.etiquetaCantidad(): String =
    formatCantidadConUnidad(cantidad, unidad ?: "NIU")

fun UbicacionProducto.etiquetaTipo(): String = when {
    tipoMovimiento == HistorialItemTipo.ENTRADA -> "Entrada"
    esTraslado -> "Traslado"
    else -> "Salida"
}

fun UbicacionProducto.etiquetaRecorrido(): String =
    "De ${origenNombre.trim()} → ${destinoNombre.trim()}"

fun UbicacionProducto.fechaLegible(): String = fecha.formatFechaLegible()

fun UbicacionProducto.fechaHoraCompacto(): String = fecha.formatFechaHoraCompacto()
