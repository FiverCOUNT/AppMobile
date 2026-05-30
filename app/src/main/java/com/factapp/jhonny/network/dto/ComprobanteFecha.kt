package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.Invoice
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Invoice.fechaEmisionLocal(): LocalDate? {
    val raw = fechaEmision?.trim().orEmpty()
    if (raw.length < 10) return null
    return runCatching { LocalDate.parse(raw.substring(0, 10)) }.getOrNull()
}

fun List<Invoice>.filtrarPorRango(fechaInicio: LocalDate, fechaFin: LocalDate): List<Invoice> =
    filter { doc ->
        val f = doc.fechaEmisionLocal() ?: return@filter false
        !f.isBefore(fechaInicio) && !f.isAfter(fechaFin)
    }

fun List<Invoice>.filtrarPorDia(dia: LocalDate): List<Invoice> =
    filtrarPorRango(dia, dia)

fun formatearDiaElegante(fecha: LocalDate): String {
    val texto = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("es", "PE"))
        .format(fecha)
    return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }
}

fun formatearRangoElegante(inicio: LocalDate, fin: LocalDate): String =
    if (inicio == fin) formatearDiaElegante(inicio)
    else "${inicio.dayOfMonth} – ${formatearDiaElegante(fin)}"
