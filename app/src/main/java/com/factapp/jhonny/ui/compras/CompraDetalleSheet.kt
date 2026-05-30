package com.factapp.jhonny.ui.compras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.colorArgb
import com.factapp.jhonny.network.dto.etiqueta
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.model.lineaPrincipal
import com.factapp.jhonny.network.dto.model.textoEnComprobante
import com.factapp.jhonny.network.dto.tieneCdrZip
import com.factapp.jhonny.network.dto.tienePdf
import com.factapp.jhonny.ui.comprobantes.DetalleComprobanteModo
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun CompraDetalleSheet(
    compra: Invoice?,
    onDismiss: () -> Unit,
    modo: DetalleComprobanteModo = DetalleComprobanteModo.COMPRA,
) {
    if (compra == null) return

    val context = LocalContext.current
    val esVenta = modo == DetalleComprobanteModo.VENTA
    val tituloReceptor = if (esVenta) "Cliente / receptor" else "Proveedor"
    val tituloDocumento = if (esVenta) "Documento receptor" else "Documento proveedor"
    val receptorNombre = compra.cliente?.razonSocial ?: compra.receptor.nombre
    val receptorDocumento = compra.cliente?.etiquetaDocumento
        ?: compra.receptor.documentoNumero

    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = compra.etiquetaCompleta,
        subtitle = receptorNombre,
        theme = PartialSheetTheme.Emit,
    ) {
        val estadoColor = Color(compra.estado.colorArgb())
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = estadoColor.copy(alpha = 0.1f),
        ) {
            Text(
                text = "${compra.etiquetaTipo()} · ${compra.estado.etiqueta()}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = estadoColor,
            )
        }

        if (compra.tienePdf() || compra.tieneCdrZip()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (compra.tienePdf()) {
                    TextButton(
                        onClick = { ComprobanteDocumentIntents.abrirPdf(context, compra) },
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, null, Modifier.size(18.dp), tint = C.accent)
                        Spacer(Modifier.size(6.dp))
                        Text("Descargar PDF", fontSize = 13.sp, color = C.accent)
                    }
                }
                if (compra.tieneCdrZip()) {
                    TextButton(
                        onClick = { ComprobanteDocumentIntents.abrirCdrZip(context, compra) },
                    ) {
                        Icon(Icons.Outlined.Archive, null, Modifier.size(18.dp), tint = C.primary)
                        Spacer(Modifier.size(6.dp))
                        Text("CDR (.zip)", fontSize = 13.sp, color = C.primary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DetalleFila(tituloReceptor, receptorNombre)
            DetalleFila(tituloDocumento, receptorDocumento)
            if (esVenta) {
                compra.cliente?.address?.lineaPrincipal?.let {
                    DetalleFila("Dirección", it)
                }
                compra.cliente?.telefono?.takeIf { it.isNotBlank() }?.let {
                    DetalleFila("Teléfono", it)
                }
                compra.emisor?.nombre?.let { DetalleFila("Emisor", it) }
                compra.facturas?.takeIf { it.isNotEmpty() }?.let { refs ->
                    DetalleFila(
                        "Facturas vinculadas",
                        refs.joinToString(", ") { it.etiquetaCompleta },
                    )
                }
            }
            compra.fechaEmision?.let { DetalleFila("Fecha emisión", it.take(10)) }
            compra.observaciones?.let { DetalleFila("Observaciones", it) }

            Spacer(Modifier.height(4.dp))
            Text(
                "Detalle",
                fontWeight = FontWeight.Bold,
                color = C.primary,
                fontSize = 14.sp,
            )
            compra.lineas.forEach { linea ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = C.surfaceSoft,
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            linea.textoEnComprobante(),
                            fontWeight = FontWeight.SemiBold,
                            color = C.textPrimary,
                            fontSize = 14.sp,
                        )
                        Text(
                            "${linea.cantidad} ${linea.unidad} × ${formatearSoles(linea.precioUnitario)}",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                        )
                        Text(
                            formatearSoles(linea.total),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = C.accent,
                        )
                    }
                }
            }

            HorizontalDivider(color = C.border.copy(alpha = 0.4f))
            DetalleFila("Subtotal", formatearSoles(compra.totales.subtotal))
            DetalleFila("IGV", formatearSoles(compra.totales.igv))
            DetalleFila("Total", formatearSoles(compra.totales.total), destacado = true)
        }
    }
}

@Composable
private fun DetalleFila(
    etiqueta: String,
    valor: String,
    destacado: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            etiqueta,
            fontSize = if (destacado) 14.sp else 13.sp,
            color = C.textSecondary,
            fontWeight = if (destacado) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            valor,
            fontSize = if (destacado) 15.sp else 13.sp,
            color = if (destacado) C.primary else C.textPrimary,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
