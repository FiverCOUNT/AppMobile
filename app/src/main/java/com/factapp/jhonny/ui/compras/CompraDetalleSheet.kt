package com.factapp.jhonny.ui.compras

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.ComprobanteRepository.PdfFormato
import com.factapp.jhonny.network.dto.colorArgb
import com.factapp.jhonny.network.dto.etiqueta
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.fechaEmisionLegible
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.lineaPrincipal
import com.factapp.jhonny.network.dto.model.textoEnComprobante
import com.factapp.jhonny.network.dto.motivoRechazoSunat
import com.factapp.jhonny.network.dto.puedeReenviar
import com.factapp.jhonny.network.dto.tienePdf
import com.factapp.jhonny.ui.comprobantes.ComprobanteQrPreview
import com.factapp.jhonny.ui.comprobantes.DetalleComprobanteModo
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

@Composable
fun CompraDetalleSheet(
    compra: Invoice?,
    onDismiss: () -> Unit,
    modo: DetalleComprobanteModo = DetalleComprobanteModo.COMPRA,
    companyRuc: String = "",
    token: String? = null,
    emisorFallback: Company? = null,
    onReenviado: () -> Unit = {},
) {
    if (compra == null) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var descargandoPdfFormato by remember(compra.id) { mutableStateOf<PdfFormato?>(null) }
    var reenviando by remember(compra.id) { mutableStateOf(false) }
    val esVenta = modo == DetalleComprobanteModo.VENTA
    val tituloReceptor = if (esVenta) "Cliente / receptor" else "Proveedor"
    val tituloDocumento = if (esVenta) "Documento receptor" else "Documento proveedor"
    val receptorNombre = compra.cliente?.razonSocial ?: compra.receptor.nombre
    val receptorDocumento = compra.cliente?.etiquetaDocumento
        ?: compra.receptor.documentoNumero
    val estadoColor = Color(compra.estado.colorArgb())
    val scroll = rememberScrollState()

    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = compra.etiquetaCompleta,
        subtitle = "${compra.etiquetaTipo()} · $receptorNombre",
        theme = PartialSheetTheme.Emit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = estadoColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, estadoColor.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = compra.estado.etiqueta(),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = estadoColor,
                    )
                }
                Text(
                    text = formatearSoles(compra.totales.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = C.primary,
                    modifier = Modifier.weight(1f),
                )
            }

            if (esVenta && compra.estado == ComprobanteEstado.RECHAZADO) {
                compra.motivoRechazoSunat()?.let { motivo ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = estadoColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, estadoColor.copy(alpha = 0.25f)),
                    ) {
                        Text(
                            text = motivo,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            color = estadoColor,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            if (esVenta && (compra.tienePdf() || compra.puedeReenviar())) {
                AccionesComprobanteCard(
                    compra = compra,
                    descargandoPdfFormato = descargandoPdfFormato,
                    reenviando = reenviando,
                    onReenviar = {
                        reenviando = true
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                ComprobanteRepository.reenviar(
                                    companyRuc.ifBlank { compra.companyRuc },
                                    token,
                                    compra.id,
                                )
                            }
                            reenviando = false
                            result.fold(
                                onSuccess = { actualizado ->
                                    val msg = if (actualizado.sunatOk == true) {
                                        "${actualizado.etiquetaCompleta} aceptado por SUNAT"
                                    } else {
                                        actualizado.motivoRechazoSunat()
                                            ?: "${actualizado.etiquetaCompleta} sigue rechazado"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    onReenviado()
                                },
                                onFailure = {
                                    Toast.makeText(
                                        context,
                                        it.message ?: "No se pudo reenviar",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                },
                            )
                        }
                    },
                    onPdfA4 = {
                        descargandoPdfFormato = PdfFormato.A4
                        scope.launch {
                            ComprobanteDocumentIntents.abrirPdf(
                                context = context,
                                comprobante = compra,
                                companyRuc = companyRuc.ifBlank { compra.companyRuc },
                                token = token,
                                formato = PdfFormato.A4,
                                emisorFallback = emisorFallback,
                            )
                            descargandoPdfFormato = null
                        }
                    },
                    onTicket = {
                        descargandoPdfFormato = PdfFormato.TICKET
                        scope.launch {
                            ComprobanteDocumentIntents.abrirPdf(
                                context = context,
                                comprobante = compra,
                                companyRuc = companyRuc.ifBlank { compra.companyRuc },
                                token = token,
                                formato = PdfFormato.TICKET,
                                emisorFallback = emisorFallback,
                            )
                            descargandoPdfFormato = null
                        }
                    },
                )
            } else if (!esVenta && compra.tienePdf()) {
                OutlinedButton(
                    onClick = {
                        descargandoPdfFormato = PdfFormato.A4
                        scope.launch {
                            ComprobanteDocumentIntents.abrirPdf(
                                context = context,
                                comprobante = compra,
                                companyRuc = companyRuc.ifBlank { compra.companyRuc },
                                token = token,
                                formato = PdfFormato.A4,
                                emisorFallback = emisorFallback,
                            )
                            descargandoPdfFormato = null
                        }
                    },
                    enabled = descargandoPdfFormato == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Ver PDF")
                }
            }

            DetalleSectionCard(
                titulo = tituloReceptor,
                icono = Icons.Outlined.Person,
            ) {
                DetalleFila(tituloDocumento, receptorDocumento)
                DetalleFila("Nombre", receptorNombre)
                if (esVenta) {
                    compra.cliente?.address?.lineaPrincipal?.let { DetalleFila("Dirección", it) }
                    compra.cliente?.telefono?.takeIf { it.isNotBlank() }?.let { DetalleFila("Teléfono", it) }
                    compra.emisor?.nombre?.let { DetalleFila("Emisor", it) }
                    compra.facturas?.takeIf { it.isNotEmpty() }?.let { refs ->
                        DetalleFila(
                            "Facturas vinculadas",
                            refs.joinToString(", ") { it.etiquetaCompleta },
                        )
                    }
                }
            }

            DetalleSectionCard(
                titulo = "Datos del comprobante",
                icono = Icons.Outlined.CalendarMonth,
            ) {
                compra.fechaEmisionLegible()?.let { DetalleFila("Fecha de emisión", it) }
                DetalleFila("Moneda", compra.tipoMoneda)
                compra.observaciones?.takeIf { it.isNotBlank() }?.let { DetalleFila("Observaciones", it) }
            }

            if (compra.lineas.isNotEmpty()) {
                DetalleSectionCard(
                    titulo = "Detalle de ítems",
                    icono = Icons.Outlined.ShoppingBag,
                ) {
                    compra.lineas.forEachIndexed { index, linea ->
                        if (index > 0) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = C.border.copy(alpha = 0.25f))
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            linea.textoEnComprobante(),
                            fontWeight = FontWeight.SemiBold,
                            color = C.textPrimary,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${linea.cantidad} ${linea.unidad ?: "UND"} × ${formatearSoles(linea.precioUnitario)}",
                                fontSize = 12.sp,
                                color = C.textSecondary,
                            )
                            Text(
                                formatearSoles(linea.total),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = C.accent,
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = C.primary.copy(alpha = 0.06f)),
                border = BorderStroke(1.dp, C.primary.copy(alpha = 0.15f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Totales",
                        fontWeight = FontWeight.Bold,
                        color = C.primary,
                        fontSize = 14.sp,
                    )
                    DetalleFila("Subtotal", formatearSoles(compra.totales.subtotal))
                    DetalleFila("IGV", formatearSoles(compra.totales.igv))
                    HorizontalDivider(color = C.border.copy(alpha = 0.35f))
                    DetalleFila("Total", formatearSoles(compra.totales.total), destacado = true)
                }
            }

            if (esVenta && compra.estado != ComprobanteEstado.BORRADOR) {
                ComprobanteQrPreview(comprobante = compra)
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun AccionesComprobanteCard(
    compra: Invoice,
    descargandoPdfFormato: PdfFormato?,
    reenviando: Boolean,
    onReenviar: () -> Unit,
    onPdfA4: () -> Unit,
    onTicket: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Documentos",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = C.textSecondary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (compra.tienePdf()) {
                    AccionDocButton(
                        label = if (descargandoPdfFormato == PdfFormato.A4) "…" else "PDF A4",
                        icon = Icons.Outlined.PictureAsPdf,
                        enabled = descargandoPdfFormato == null && !reenviando,
                        loading = descargandoPdfFormato == PdfFormato.A4,
                        modifier = Modifier.weight(1f),
                        onClick = onPdfA4,
                    )
                    AccionDocButton(
                        label = if (descargandoPdfFormato == PdfFormato.TICKET) "…" else "Ticket",
                        icon = Icons.Outlined.Receipt,
                        enabled = descargandoPdfFormato == null && !reenviando,
                        loading = descargandoPdfFormato == PdfFormato.TICKET,
                        modifier = Modifier.weight(1f),
                        onClick = onTicket,
                    )
                }
            }
            if (compra.puedeReenviar()) {
                OutlinedButton(
                    onClick = onReenviar,
                    enabled = !reenviando && descargandoPdfFormato == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (reenviando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = C.primary,
                        )
                    } else {
                        Icon(Icons.Outlined.Refresh, null, Modifier.size(18.dp), tint = C.primary)
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        if (reenviando) "Reenviando a SUNAT…" else "Reenviar a SUNAT",
                        color = C.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccionDocButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = C.accent,
            )
        } else {
            Icon(icon, null, Modifier.size(18.dp), tint = C.accent)
        }
        Spacer(Modifier.size(6.dp))
        Text(label, fontSize = 13.sp, color = C.accent)
    }
}

@Composable
private fun DetalleSectionCard(
    titulo: String,
    icono: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icono, contentDescription = null, tint = C.accent, modifier = Modifier.size(20.dp))
                Text(
                    titulo,
                    fontWeight = FontWeight.Bold,
                    color = C.primary,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            etiqueta,
            fontSize = if (destacado) 14.sp else 13.sp,
            color = C.textSecondary,
            fontWeight = if (destacado) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(0.45f),
        )
        Text(
            valor,
            fontSize = if (destacado) 16.sp else 13.sp,
            color = if (destacado) C.primary else C.textPrimary,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(0.55f),
        )
    }
}
