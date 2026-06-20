package com.factapp.jhonny.network.dto

/**
 * Catálogo N.° 09 SUNAT — códigos de tipo de nota de crédito electrónica (cbc:ResponseCode).
 * Definido en la app (no en BD); los códigos son fijos por norma SUNAT.
 */
data class MotivoNotaCreditoSunat(
    val codigo: String,
    val descripcion: String,
    /** Texto enviado a SUNAT en des_motivo / motivo_nota. */
    val descripcionSunat: String,
)

object MotivosNotaCreditoSunat {

  /** Orden SUNAT / referencia completa (01–13). */
  private val todos: List<MotivoNotaCreditoSunat> = listOf(
    MotivoNotaCreditoSunat("01", "Anulación de la operación", "ANULACION DE LA OPERACION"),
    MotivoNotaCreditoSunat("02", "Anulación por error en el RUC", "ANULACION POR ERROR EN EL RUC"),
    MotivoNotaCreditoSunat("03", "Corrección por error en la descripción", "CORRECCION POR ERROR EN LA DESCRIPCION"),
    MotivoNotaCreditoSunat("04", "Descuento global", "DESCUENTO GLOBAL"),
    MotivoNotaCreditoSunat("05", "Descuento por ítem", "DESCUENTO POR ITEM"),
    MotivoNotaCreditoSunat("06", "Devolución total", "DEVOLUCION TOTAL"),
    MotivoNotaCreditoSunat("07", "Devolución por ítem", "DEVOLUCION POR ITEM"),
    MotivoNotaCreditoSunat("08", "Bonificación", "BONIFICACION"),
    MotivoNotaCreditoSunat("09", "Disminución en el valor", "DISMINUCION EN EL VALOR"),
    MotivoNotaCreditoSunat("10", "Otros conceptos", "OTROS CONCEPTOS"),
    MotivoNotaCreditoSunat("11", "Ajustes de operaciones de exportación", "AJUSTES DE OPERACIONES DE EXPORTACION"),
    MotivoNotaCreditoSunat("12", "Ajustes afectos al IVAP", "AJUSTES AFECTOS AL IVAP"),
    MotivoNotaCreditoSunat(
      "13",
      "Corrección de forma de pago / cuotas",
      "CORRECCION DEL MONTO NETO PENDIENTE DE PAGO Y/O FECHAS DE VENCIMIENTO",
    ),
  )

  /** Los más usados en retail / tienda (aparecen primero en la lista). */
  private val codigosComunes = listOf(
    "07", // Devolución por ítem
    "06", // Devolución total
    "01", // Anulación de la operación
    "05", // Descuento por ítem
    "09", // Disminución en el valor
    "04", // Descuento global
    "02", // Error en el RUC
    "03", // Error en la descripción
  )

  val comunes: List<MotivoNotaCreditoSunat> =
    codigosComunes.mapNotNull { codigo -> todos.find { it.codigo == codigo } }

  val otros: List<MotivoNotaCreditoSunat> =
    todos.filter { it.codigo !in codigosComunes.toSet() }

  /** Lista para la UI: frecuentes arriba, demás abajo. */
  val catalogo: List<MotivoNotaCreditoSunat> = comunes + otros

  fun porCodigo(codigo: String): MotivoNotaCreditoSunat? =
    todos.find { it.codigo == codigo }

  /** Motivos donde se acredita un monto (descuento), no devolución de cantidad. */
  fun esAcreditacionPorMonto(codigo: String?): Boolean =
    codigo in setOf("04", "05", "09")

  fun hintAcreditacion(codigo: String?): String? = when (codigo) {
    "04" -> "Indica el monto total del descuento global (con IGV) en cada línea o una sola línea."
    "05" -> "Por cada ítem indica el precio nuevo por unidad (con IGV). La cantidad es cuántas unidades reciben ese descuento."
    "09" -> "Indica el monto a reducir por unidad (con IGV) respecto al precio facturado."
    else -> null
  }
}
