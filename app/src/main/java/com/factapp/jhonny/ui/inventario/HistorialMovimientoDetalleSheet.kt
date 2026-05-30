package com.factapp.jhonny.ui.inventario

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.factapp.jhonny.network.dto.formatCantidadConUnidad
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.resumenProductos
import com.factapp.jhonny.network.dto.model.MovimientoTipo
import com.factapp.jhonny.network.dto.model.etiquetaAlmacenDestinoHistorial
import com.factapp.jhonny.network.dto.model.etiquetaAlmacenOrigenHistorial
import com.factapp.jhonny.network.dto.model.etiquetaTipoHistorial
import com.factapp.jhonny.network.dto.model.fechaLegible
import com.factapp.jhonny.network.dto.model.horaLegible
import com.factapp.jhonny.network.dto.model.seriesEnMovimiento
import com.factapp.jhonny.network.dto.model.etiquetaGuiaRemision
import com.factapp.jhonny.network.dto.model.tituloHistorial

private data class TipoMovimientoEstilo(
    val icono: ImageVector,
    val color: Color,
    val gradiente: List<Color>,
)

private fun estiloMovimiento(mov: Movimiento): TipoMovimientoEstilo = when (mov.tipo) {
    MovimientoTipo.ENTRADA -> TipoMovimientoEstilo(
        icono = Icons.Default.Input,
        color = Color(0xFF2E7D32),
        gradiente = listOf(Color(0xFF1B5E20), Color(0xFF43A047)),
    )
    MovimientoTipo.SALIDA -> TipoMovimientoEstilo(
        icono = Icons.Default.LocalShipping,
        color = Color(0xFF1565C0),
        gradiente = listOf(Color(0xFF0D47A1), Color(0xFF1976D2)),
    )
    MovimientoTipo.AJUSTE -> TipoMovimientoEstilo(
        icono = Icons.Default.Tune,
        color = Color(0xFFF57C00),
        gradiente = listOf(Color(0xFFE65100), Color(0xFFFF9800)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialMovimientoDetalleSheet(
    movimiento: Movimiento?,
    catalogo: Map<String, CatalogItem>,
    almacenes: Map<String, Almacen>,
    onDismiss: () -> Unit,
) {
    if (movimiento == null) return

    val estilo = estiloMovimiento(movimiento)
    val detalle = MovimientoDetalleUi(
        etiqueta = movimiento.etiquetaTipoHistorial(),
        titulo = movimiento.tituloHistorial(),
        subtitulo = movimiento.resumenProductos(catalogo),
        fecha = movimiento.fechaLegible(),
        hora = movimiento.horaLegible(),
        origen = movimiento.etiquetaAlmacenOrigenHistorial(almacenes),
        destino = movimiento.etiquetaAlmacenDestinoHistorial(almacenes),
        lineas = movimiento.lineas.map { it.aDetalleLineaUi(catalogo) },
        campos = buildList {
            add(MovimientoDetalleCampoUi("N° movimiento", movimiento.numeroDisplay, Icons.Default.Tag))
            movimiento.estado?.let {
                add(MovimientoDetalleCampoUi("Estado", it.name, Icons.Default.Tag))
            }
            movimiento.cliente?.let { cliente ->
                add(
                    MovimientoDetalleCampoUi(
                        "Cliente / receptor",
                        cliente.razonSocial ?: "Doc. ${cliente.numeroDoc}",
                        Icons.Default.Person,
                    ),
                )
            }
            movimiento.referenciaTipo?.let {
                add(MovimientoDetalleCampoUi("Referencia", it.replace('_', ' '), Icons.Default.Tag))
            }
            movimiento.comprobanteId?.let {
                add(MovimientoDetalleCampoUi("Comprobante venta", it, Icons.Default.Tag))
            }
            movimiento.etiquetaGuiaRemision()?.let { guia ->
                add(MovimientoDetalleCampoUi("Guía de remisión", guia, Icons.Default.LocalShipping))
            }
            add(MovimientoDetalleCampoUi("ID interno", movimiento.id, Icons.Default.Tag, valorSecundario = true))
        },
        notas = listOfNotNull(movimiento.observaciones?.takeIf { it.isNotBlank() }),
        icono = estilo.icono,
        accentColor = estilo.color,
        headerColors = estilo.gradiente,
    )

    MovimientoDetalleComunSheet(detalle = detalle, onDismiss = onDismiss)
}

private fun LineaCatalogoItem.aDetalleLineaUi(catalogo: Map<String, CatalogItem>): MovimientoDetalleLineaUi {
    val item = catalogo[catalogItemId]
    return MovimientoDetalleLineaUi(
        nombre = item?.nombre ?: nombreEfectivo,
        cantidad = formatCantidadConUnidad(cantidad, item?.unidad ?: "NIU"),
        series = seriesEnMovimiento().orEmpty(),
        manejaSerie = tieneSeries,
    )
}
