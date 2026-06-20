package com.factapp.jhonny.ui.comprobantes

import kotlin.math.floor
import kotlin.math.round

internal object MontoEnLetras {

    fun soles(monto: Double): String {
        val total = if (monto.isFinite()) monto else 0.0
        val entero = floor(total).toInt()
        val centimos = round((total - entero) * 100).toInt().coerceIn(0, 99)
        return "SON: ${unidades(entero)} Y ${centimos.toString().padStart(2, '0')}/100 SOLES"
    }

    private fun unidades(num: Int): String {
        if (num == 0) return "CERO"
        if (num == 100) return "CIEN"

        val u = arrayOf(
            "", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
            "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE",
        )
        val d = arrayOf("", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA")
        val c = arrayOf(
            "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS",
        )

        if (num < 20) return u[num]
        if (num < 100) {
            val dec = num / 10
            val rest = num % 10
            if (num < 30) {
                return if (rest == 0) "VEINTE" else "VEINTI${u[rest].lowercase()}".replace("veintiuno", "veintiún").uppercase()
            }
            return if (rest == 0) d[dec] else "${d[dec]} Y ${u[rest]}"
        }
        if (num < 1000) {
            val cent = num / 100
            val rest = num % 100
            val base = if (num == 100) "CIEN" else c[cent]
            return if (rest == 0) base else "$base ${unidades(rest)}"
        }
        if (num < 1_000_000) {
            val miles = num / 1000
            val rest = num % 1000
            val pref = if (miles == 1) "MIL" else "${unidades(miles)} MIL"
            return if (rest == 0) pref else "$pref ${unidades(rest)}"
        }
        val millones = num / 1_000_000
        val rest = num % 1_000_000
        val pref = if (millones == 1) "UN MILLÓN" else "${unidades(millones)} MILLONES"
        return if (rest == 0) pref else "$pref ${unidades(rest)}"
    }
}
