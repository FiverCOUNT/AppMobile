package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.LineaCatalogoItem

/** @see LineaCatalogoItem */
typealias ItemEnComprobante = LineaCatalogoItem

fun formatearSoles(monto: Double): String = "S/ %.2f".format(monto)
