package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.UbicacionProducto

/** Orden común: registro más reciente primero. */
@JvmName("ordenadosMovimientosPorFechaReciente")
fun List<Movimiento>.ordenadosPorFechaReciente(): List<Movimiento> =
    sortedByDescending { it.fechaEfectiva }

@JvmName("ordenadosUbicacionesPorFechaReciente")
fun List<UbicacionProducto>.ordenadosPorFechaReciente(): List<UbicacionProducto> =
    sortedByDescending { it.fecha }
