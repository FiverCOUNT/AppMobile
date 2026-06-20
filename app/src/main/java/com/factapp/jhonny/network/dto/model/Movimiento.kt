package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.network.dto.formatFechaHoraCompacto
import com.factapp.jhonny.network.dto.formatFechaLegible
import com.factapp.jhonny.network.dto.formatHoraLegible
import com.google.gson.annotations.SerializedName

/**
 * Registro de kardex: entrada, salida (entrega/traslado) o ajuste.
 *
 * Puede vincularse a una venta ([comprobanteId]) y/o a una guía de remisión ([guiaRemisionId]).
 */
data class Movimiento(
    val id: String,
    @SerializedName("company_ruc")
    val companyRuc: String,
    @SerializedName("almacen_id")
    val almacenId: String,
    val tipo: MovimientoTipo,
    val lineas: List<LineaCatalogoItem> = emptyList(),
    val fecha: Long,
    val observaciones: String? = null,
    @SerializedName("referencia_tipo")
    val referenciaTipo: String? = null,
    @SerializedName("referencia_id")
    val referenciaId: String? = null,
    val numero: String? = null,
    @SerializedName("almacen_destino_id")
    val almacenDestinoId: String? = null,
    val estado: MovimientoEstado? = null,
    /** Factura / boleta de venta asociada a la salida (opcional). */
    @SerializedName("comprobante_id")
    val comprobanteId: String? = null,
    /** ID del comprobante guía de remisión ([InvoiceTipoDoc.GUIA_EMISION]). */
    @SerializedName("guia_remision_id")
    val guiaRemisionId: String? = null,
    /** Snapshot de la guía (serie-número) cuando el API la embebe en la respuesta. */
    @SerializedName(value = "guia_remision", alternate = ["guia"])
    val guiaRemision: Invoice? = null,
    val cliente: MovimientoCliente? = null,
    @SerializedName("fecha_despacho")
    val fechaDespacho: Long? = null,
) {
    val numeroDisplay: String
        get() = numero ?: id

    val fechaEfectiva: Long
        get() = fechaDespacho ?: fecha

    val tieneGuiaRemision: Boolean
        get() = !guiaRemisionId.isNullOrBlank() || guiaRemision != null

    /** Gson puede dejar [lineas] en null aunque el tipo sea no anulable. */
    val lineasSeguras: List<LineaCatalogoItem>
        get() = lineas ?: emptyList()
}

/** Normaliza respuestas del API antes de usarlas en UI (evita NPE en listas). */
fun Movimiento.sanitizarDesdeApi(): Movimiento = copy(
    lineas = lineasSeguras.map { it.sanitizarDesdeApi() },
)

fun LineaCatalogoItem.sanitizarDesdeApi(): LineaCatalogoItem = copy(
    catalogItemId = catalogItemId ?: "",
    cantidad = cantidad ?: 0.0,
    numerosSerie = numerosSerie ?: emptyList(),
    series = series ?: emptyList(),
)

fun Movimiento.etiquetaGuiaRemision(): String? =
    guiaRemision?.etiqueta ?: guiaRemisionId?.takeIf { it.isNotBlank() }

data class MovimientoCliente(
    @SerializedName("tipo_doc")
    val tipoDoc: String,
    @SerializedName("numero_doc")
    val numeroDoc: String,
    @SerializedName("razon_social")
    val razonSocial: String? = null,
)

enum class MovimientoEstado {
    @SerializedName("BORRADOR")
    BORRADOR,

    @SerializedName("DESPACHADA")
    DESPACHADA,

    @SerializedName("ANULADA")
    ANULADA,
}

enum class MovimientoTipo {
    @SerializedName("ENTRADA")
    ENTRADA,

    @SerializedName("SALIDA")
    SALIDA,

    @SerializedName("AJUSTE")
    AJUSTE,
}

fun Movimiento.etiquetaDestino(almacenes: Map<String, Almacen> = emptyMap()): String? {
    almacenDestinoId?.let { id ->
        almacenes[id]?.let { return "Traslado → ${it.nombre}" }
        return "Traslado → $id"
    }
    cliente?.let {
        return it.razonSocial?.takeIf { n -> n.isNotBlank() }
            ?: "Doc. ${it.numeroDoc}"
    }
    return null
}

fun Movimiento.etiquetaTipoHistorial(): String = when (tipo) {
    MovimientoTipo.ENTRADA -> if (referenciaTipo == "DEVOLUCION_CLIENTE") "Devolución" else "Ingreso"
    MovimientoTipo.SALIDA -> if (almacenDestinoId != null) "Traslado" else "Salida"
    MovimientoTipo.AJUSTE -> "Ajuste"
}

fun Movimiento.tituloHistorial(): String = when (tipo) {
    MovimientoTipo.ENTRADA -> if (referenciaTipo == "DEVOLUCION_CLIENTE") {
        "Devolución ${numeroDisplay}"
    } else {
        "Ingreso al almacén"
    }
    MovimientoTipo.SALIDA -> if (almacenDestinoId != null) {
        "Traslado ${numeroDisplay}"
    } else {
        "Despacho ${numeroDisplay}"
    }
    MovimientoTipo.AJUSTE -> "Ajuste ${numeroDisplay}"
}

fun Movimiento.resumenProductos(catalogo: Map<String, CatalogItem> = emptyMap()): String {
    val nombres = lineasSeguras
        .map { catalogo[it.catalogItemId]?.nombre ?: it.nombreEfectivo }
        .distinct()
    return when (nombres.size) {
        0 -> "Sin productos"
        1 -> nombres.first()
        2 -> "${nombres[0]}, ${nombres[1]}"
        else -> "${nombres.first()} y ${nombres.size - 1} más"
    }
}

fun Movimiento.detalleHistorial(catalogo: Map<String, CatalogItem> = emptyMap()): String {
    observaciones?.takeIf { it.isNotBlank() }?.let { return it }
    val totalUds = lineasSeguras.sumOf { it.cantidad }
    val conSeries = lineasSeguras.any { it.tieneSeries }
    return buildString {
        append(resumenProductos(catalogo))
        append(" · ")
        append("${totalUds.toInt()} uds")
        if (conSeries) append(" · con series")
    }
}

fun Movimiento.fechaLegible(): String = fechaEfectiva.formatFechaLegible()

fun Movimiento.horaLegible(): String? = fechaEfectiva.formatHoraLegible()

fun Movimiento.fechaHoraCompacto(): String = fechaEfectiva.formatFechaHoraCompacto()

fun nombreAlmacenHistorial(almacenes: Map<String, Almacen>, id: String?): String =
    when {
        id.isNullOrBlank() -> "—"
        else -> almacenes[id]?.nombre ?: id
    }

fun Movimiento.etiquetaAlmacenOrigenHistorial(almacenes: Map<String, Almacen>): String =
    when (tipo) {
        MovimientoTipo.ENTRADA -> when (referenciaTipo) {
            "DEVOLUCION_CLIENTE" -> {
                val c = cliente
                when {
                    c == null -> "Cliente"
                    !c.razonSocial.isNullOrBlank() -> c.razonSocial
                    else -> "Doc. ${c.numeroDoc}"
                }
            }
            else -> "Recepción externa"
        }
        else -> nombreAlmacenHistorial(almacenes, almacenId)
    }

fun Movimiento.etiquetaAlmacenDestinoHistorial(almacenes: Map<String, Almacen>): String =
    when (tipo) {
        MovimientoTipo.ENTRADA -> nombreAlmacenHistorial(almacenes, almacenId)
        else -> when {
            almacenDestinoId != null -> nombreAlmacenHistorial(almacenes, almacenDestinoId)
            cliente != null -> {
                val c = cliente
                c.razonSocial?.takeIf { it.isNotBlank() } ?: "Doc. ${c.numeroDoc}"
            }
            else -> "—"
        }
    }
