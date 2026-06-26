package com.factapp.jhonny.network.dto.model

object SaleDetailEstado {
    const val ACTIVO = "ACTIVO"
    const val DEVUELTO = "DEVUELTO"
    const val ACREDITADO = "ACREDITADO"
}

fun SaleDetail.etiquetaEstado(): String? = when (estado?.uppercase()) {
    SaleDetailEstado.DEVUELTO -> "Devuelto"
    SaleDetailEstado.ACREDITADO -> "Acreditado"
    else -> null
}

fun Invoice.cantidadLineasActivasNotaCredito(): Int =
    details.count { it.estaDisponibleParaNotaCredito() }

fun Invoice.tieneLineasPendientesNotaCredito(): Boolean =
    cantidadLineasActivasNotaCredito() > 0
