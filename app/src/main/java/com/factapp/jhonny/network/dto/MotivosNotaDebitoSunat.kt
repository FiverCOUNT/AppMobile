package com.factapp.jhonny.network.dto

/**
 * Catálogo N.° 10 SUNAT — códigos de tipo de nota de débito electrónica (cbc:ResponseCode).
 */
data class MotivoNotaDebitoSunat(
    val codigo: String,
    val descripcion: String,
    val descripcionSunat: String,
)

object MotivosNotaDebitoSunat {

    private val todos: List<MotivoNotaDebitoSunat> = listOf(
        MotivoNotaDebitoSunat("01", "Intereses por mora", "INTERESES POR MORA"),
        MotivoNotaDebitoSunat("02", "Aumento en el valor", "AUMENTO EN EL VALOR"),
        MotivoNotaDebitoSunat("03", "Penalidades / otros conceptos", "PENALIDADES/ OTROS CONCEPTOS"),
        MotivoNotaDebitoSunat("11", "Ajustes de operaciones de exportación", "AJUSTES DE OPERACIONES DE EXPORTACION"),
        MotivoNotaDebitoSunat("12", "Ajustes afectos al IVAP", "AJUSTES AFECTOS AL IVAP"),
    )

    private val codigosComunes = listOf("02", "01", "03")

    val comunes: List<MotivoNotaDebitoSunat> =
        codigosComunes.mapNotNull { codigo -> todos.find { it.codigo == codigo } }

    val otros: List<MotivoNotaDebitoSunat> =
        todos.filter { it.codigo !in codigosComunes.toSet() }

    val catalogo: List<MotivoNotaDebitoSunat> = comunes + otros

    fun porCodigo(codigo: String): MotivoNotaDebitoSunat? =
        todos.find { it.codigo == codigo }

    /** Motivo 02: carga ítems de la factura/boleta y monto adicional por unidad. */
    fun esAjustePorItems(codigo: String?): Boolean = codigo == "02"

    /** Motivos 01, 03, etc.: concepto libre desde catálogo (intereses, penalidad…). */
    fun esConceptoLibre(codigo: String?): Boolean =
        codigo != null && !esAjustePorItems(codigo)

    fun hintDebitacion(codigo: String?): String? = when (codigo) {
        "01" -> "Indica el comprobante afectado y agrega una línea del catálogo con el monto de intereses (con IGV)."
        "02" -> "Por cada ítem indica el monto adicional por unidad (con IGV). La cantidad es cuántas unidades reciben ese aumento."
        "03" -> "Indica el comprobante afectado y agrega líneas del catálogo con el monto de la penalidad u otro cargo."
        else -> null
    }
}
