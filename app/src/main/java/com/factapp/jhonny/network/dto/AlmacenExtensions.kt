package com.factapp.jhonny.network.dto

/** Genera un código interno único a partir del nombre (sin pedirlo al usuario). */
fun codigoDesdeNombreAlmacen(nombre: String): String {
    val palabras = nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val siglas = palabras
        .take(3)
        .joinToString("") { palabra ->
            palabra.filter { it.isLetterOrDigit() }.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        }
    val base = (siglas.ifBlank { "ALM" }).take(6)
    val sufijo = System.currentTimeMillis().toString().takeLast(4)
    return "$base$sufijo"
}
