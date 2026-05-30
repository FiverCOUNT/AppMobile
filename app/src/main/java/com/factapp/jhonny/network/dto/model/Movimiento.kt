package com.factapp.jhonny.network.dto.model

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
    val lineas: List<LineaCatalogoItem>,
    val fecha: String,
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
    val fechaDespacho: String? = null,
) {
    val numeroDisplay: String
        get() = numero ?: id

    val fechaEfectiva: String
        get() = fechaDespacho ?: fecha

    val tieneGuiaRemision: Boolean
        get() = !guiaRemisionId.isNullOrBlank() || guiaRemision != null
}

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
    MovimientoTipo.ENTRADA -> "Ingreso"
    MovimientoTipo.SALIDA -> if (almacenDestinoId != null) "Traslado" else "Salida"
    MovimientoTipo.AJUSTE -> "Ajuste"
}

fun Movimiento.tituloHistorial(): String = when (tipo) {
    MovimientoTipo.ENTRADA -> "Ingreso al almacén"
    MovimientoTipo.SALIDA -> if (almacenDestinoId != null) {
        "Traslado ${numeroDisplay}"
    } else {
        "Despacho ${numeroDisplay}"
    }
    MovimientoTipo.AJUSTE -> "Ajuste ${numeroDisplay}"
}

fun Movimiento.resumenProductos(catalogo: Map<String, CatalogItem> = emptyMap()): String {
    val nombres = lineas
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
    val totalUds = lineas.sumOf { it.cantidad }
    val conSeries = lineas.any { it.tieneSeries }
    return buildString {
        append(resumenProductos(catalogo))
        append(" · ")
        append("${totalUds.toInt()} uds")
        if (conSeries) append(" · con series")
    }
}

fun Movimiento.fechaLegible(): String = fechaIsoLegible(fechaEfectiva)

fun Movimiento.horaLegible(): String? {
    val f = fechaEfectiva
    if (f.length < 16 || f[10] != 'T') return null
    return f.substring(11, 16)
}

fun fechaIsoLegible(fecha: String): String {
    val parte = fecha.take(10)
    val partes = parte.split("-")
    if (partes.size != 3) return parte
    val meses = listOf(
        "ene", "feb", "mar", "abr", "may", "jun",
        "jul", "ago", "sep", "oct", "nov", "dic",
    )
    val mes = partes[1].toIntOrNull()?.minus(1)?.let { meses.getOrNull(it) } ?: partes[1]
    return "${partes[2].toIntOrNull() ?: partes[2]} $mes ${partes[0]}"
}

fun nombreAlmacenHistorial(almacenes: Map<String, Almacen>, id: String?): String =
    when {
        id.isNullOrBlank() -> "—"
        else -> almacenes[id]?.nombre ?: id
    }

fun Movimiento.etiquetaAlmacenOrigenHistorial(almacenes: Map<String, Almacen>): String =
    when (tipo) {
        MovimientoTipo.ENTRADA -> "Recepción externa"
        else -> nombreAlmacenHistorial(almacenes, almacenId)
    }

fun Movimiento.etiquetaAlmacenDestinoHistorial(almacenes: Map<String, Almacen>): String =
    when {
        almacenDestinoId != null -> nombreAlmacenHistorial(almacenes, almacenDestinoId)
        cliente != null -> {
            val c = cliente
            c.razonSocial?.takeIf { it.isNotBlank() } ?: "Doc. ${c.numeroDoc}"
        }
        tipo == MovimientoTipo.ENTRADA -> nombreAlmacenHistorial(almacenes, almacenId)
        else -> "—"
    }
