package com.factapp.jhonny.network.dto

import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.MovimientoCliente
import com.factapp.jhonny.network.dto.model.aMovimientoCliente
import com.factapp.jhonny.network.dto.model.aRegistrarMovimientoLinea
import com.factapp.jhonny.network.dto.request.RegistrarEntradaRequest

const val REFERENCIA_DEVOLUCION_CLIENTE = "DEVOLUCION_CLIENTE"

fun List<LineaCatalogoItem>.lineasParaIngresoDevolucion(almacenId: String): List<LineaCatalogoItem> =
    filter {
        it.cantidad > 0 &&
            it.catalogItemId.isNotBlank() &&
            it.aplicaIngresoDevolucionNotaCredito()
    }.map { linea ->
        linea.copy(almacenId = linea.almacenId?.takeIf { it.isNotBlank() } ?: almacenId)
    }

fun construirEntradaDevolucionNotaCredito(
    companyRuc: String,
    almacenId: String,
    lineasDevolucion: List<LineaCatalogoItem>,
    notaCreditoId: String,
    etiquetaNotaCredito: String,
    comprobanteAfectadoId: String?,
    etiquetaComprobanteAfectado: String?,
    clienteSeleccionado: Cliente?,
    receptor: Company,
): RegistrarEntradaRequest {
    val lineasIngreso = lineasDevolucion.lineasParaIngresoDevolucion(almacenId)
    val clienteId = clienteSeleccionado?.id
    val clienteMovimiento = clienteSeleccionado?.aMovimientoCliente()
        ?: receptor.aMovimientoClienteDevolucion()
    return RegistrarEntradaRequest(
        companyRuc = companyRuc,
        almacenId = almacenId,
        lineas = lineasIngreso.map { it.aRegistrarMovimientoLinea() },
        observaciones = buildString {
            append("Ingreso automático por NC $etiquetaNotaCredito")
            etiquetaComprobanteAfectado?.let { append(" (afecta $it)") }
        },
        clienteId = clienteId,
        cliente = if (clienteId == null) clienteMovimiento else null,
        referenciaTipo = REFERENCIA_DEVOLUCION_CLIENTE,
        referenciaId = notaCreditoId,
        comprobanteId = comprobanteAfectadoId?.takeIf { it.isNotBlank() },
    )
}

private fun Company.aMovimientoClienteDevolucion(): MovimientoCliente? {
    val doc = documentoNumero.filter { it.isDigit() }
    if (doc.isBlank()) return null
    val tipo = tipoDoc ?: if (doc.length == 11) "6" else "1"
    return MovimientoCliente(
        tipoDoc = tipo,
        numeroDoc = doc,
        razonSocial = nombre.takeIf { it.isNotBlank() },
    )
}
