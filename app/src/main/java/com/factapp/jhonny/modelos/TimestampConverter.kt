package com.factapp.jhonny.modelos

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimestampConverter {

    private val apiFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    /**
     * Convierte un valor de fecha del JSON a epoch millis.
     * Soporta:
     * - ISO-8601 (ej: 2026-05-14T00:10:30Z)
     * - epoch seconds (10 dígitos)
     * - epoch millis (13+ dígitos)
     */
    fun fromJson(value: String?): Long? {
        if (value.isNullOrBlank()) return null

        val raw = value.trim()
        return when {
            raw.matches(Regex("^-?\\d{10}$")) -> raw.toLong() * 1000L
            raw.matches(Regex("^-?\\d{11,}$")) -> raw.toLong()
            else -> OffsetDateTime.parse(raw, apiFormatter).toInstant().toEpochMilli()
        }
    }

    /**
     * Convierte epoch millis al formato ISO-8601 para enviar a la API.
     * Resultado ejemplo: 2026-05-14T00:10:30Z
     */
    fun toJson(epochMillis: Long?): String? {
        if (epochMillis == null) return null
        return apiFormatter.format(Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC))
    }
}
