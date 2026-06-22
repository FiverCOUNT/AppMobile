package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.Invoice
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ZONA_PERU: ZoneId = ZoneId.of("America/Lima")

/** Atajos de periodo para listados de comprobantes (API `desde` / `hasta`). */
enum class PresetPeriodoComprobante {
    HOY,
    AYER,
    ULTIMOS_7,
    ESTE_MES,
    FECHA,
    ;

    fun rango(hoy: LocalDate, fechaElegida: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
        HOY -> hoy to hoy
        AYER -> hoy.minusDays(1) to hoy.minusDays(1)
        ULTIMOS_7 -> hoy.minusDays(6) to hoy
        ESTE_MES -> YearMonth.from(hoy).atDay(1) to hoy
        FECHA -> fechaElegida to fechaElegida
    }

    fun etiquetaCorta(): String = when (this) {
        HOY -> "Hoy"
        AYER -> "Ayer"
        ULTIMOS_7 -> "7 días"
        ESTE_MES -> "Este mes"
        FECHA -> "Fecha"
    }

    fun etiquetaPeriodo(hoy: LocalDate, fechaElegida: LocalDate): String = when (this) {
        HOY, AYER, FECHA -> formatearDiaElegante(rango(hoy, fechaElegida).first)
        ULTIMOS_7, ESTE_MES -> {
            val (inicio, fin) = rango(hoy, fechaElegida)
            formatearRangoElegante(inicio, fin)
        }
    }
}

fun Invoice.fechaEmisionLocal(zone: ZoneId = ZONA_PERU): LocalDate? {
    val ms = fechaEmision ?: return null
    return ms.toLocalDate(zone)
}

fun List<Invoice>.filtrarPorRango(
    fechaInicio: LocalDate,
    fechaFin: LocalDate,
    zone: ZoneId = ZONA_PERU,
): List<Invoice> =
    filter { doc ->
        val f = doc.fechaEmisionLocal(zone) ?: return@filter false
        !f.isBefore(fechaInicio) && !f.isAfter(fechaFin)
    }

fun List<Invoice>.filtrarPorDia(dia: LocalDate, zone: ZoneId = ZONA_PERU): List<Invoice> =
    filtrarPorRango(dia, dia, zone)

/** Más reciente primero (fecha de emisión / registro). */
fun List<Invoice>.ordenadosPorFechaEmisionReciente(): List<Invoice> =
    sortedByDescending { it.fechaEmision ?: 0L }

fun formatearDiaElegante(fecha: LocalDate): String {
    val texto = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("es", "PE"))
        .format(fecha)
    return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }
}

fun formatearRangoElegante(inicio: LocalDate, fin: LocalDate): String =
    if (inicio == fin) formatearDiaElegante(inicio)
    else "${inicio.dayOfMonth} – ${formatearDiaElegante(fin)}"

fun Invoice.fechaEmisionLegible(): String? =
    fechaEmision?.formatFechaLegible()
