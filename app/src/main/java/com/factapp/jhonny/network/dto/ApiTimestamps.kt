package com.factapp.jhonny.network.dto

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private val MESES_CORTOS = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

/** Formatea epoch ms en la zona horaria del dispositivo. */
fun Long.formatFechaLegible(zone: ZoneId = ZoneId.systemDefault()): String {
    val zdt = Instant.ofEpochMilli(this).atZone(zone)
    val mes = MESES_CORTOS.getOrElse(zdt.monthValue - 1) { zdt.month.toString() }
    return "${zdt.dayOfMonth} $mes ${zdt.year}"
}

fun Long.formatHoraLegible(zone: ZoneId = ZoneId.systemDefault()): String {
    val zdt = Instant.ofEpochMilli(this).atZone(zone)
    return String.format(Locale.getDefault(), "%02d:%02d", zdt.hour, zdt.minute)
}

fun Long.formatFechaHoraCompacto(zone: ZoneId = ZoneId.systemDefault()): String =
    "${formatFechaLegible(zone)} ${formatHoraLegible(zone)}"

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

/** Convierte YYYY-MM-DD (emisión fiscal) a epoch ms al inicio del día local. */
fun parseFechaEmisionIso(iso: String?, zone: ZoneId = ZoneId.systemDefault()): Long? {
    if (iso.isNullOrBlank() || iso.length < 10) return null
    return runCatching {
        LocalDate.parse(iso.substring(0, 10))
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

fun Long?.formatFechaLegibleOrEmpty(zone: ZoneId = ZoneId.systemDefault()): String =
    this?.formatFechaLegible(zone).orEmpty()

fun Long?.formatFechaCorta(zone: ZoneId = ZoneId.systemDefault()): String? =
    this?.toLocalDate(zone)?.toString()
