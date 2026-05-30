package com.factapp.jhonny.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

data class MovimientoDetalleUi(
    val etiqueta: String,
    val titulo: String,
    val subtitulo: String,
    val fecha: String? = null,
    val hora: String? = null,
    val origen: String,
    val destino: String,
    val lineas: List<MovimientoDetalleLineaUi>,
    val campos: List<MovimientoDetalleCampoUi>,
    val notasTitulo: String = "Notas",
    val notas: List<String> = emptyList(),
    val icono: ImageVector,
    val accentColor: Color,
    val headerColors: List<Color> = listOf(accentColor, accentColor),
)

data class MovimientoDetalleLineaUi(
    val nombre: String,
    val cantidad: String,
    val series: List<String> = emptyList(),
    val manejaSerie: Boolean = false,
)

data class MovimientoDetalleCampoUi(
    val etiqueta: String,
    val valor: String,
    val icono: ImageVector = Icons.Default.Tag,
    val valorSecundario: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoDetalleComunSheet(
    detalle: MovimientoDetalleUi?,
    onDismiss: () -> Unit,
) {
    if (detalle == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(C.border),
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(detalle.headerColors))
                    .padding(20.dp),
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(detalle.icono, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                        ) {
                            Text(
                                detalle.etiqueta.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.8.sp,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            detalle.titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            lineHeight = 22.sp,
                        )
                        Text(
                            detalle.subtitulo,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 28.dp),
            ) {
                if (detalle.fecha != null || detalle.hora != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        detalle.fecha?.let {
                            MovimientoMetaChip(
                                icono = Icons.Default.AccessTime,
                                titulo = "Fecha",
                                valor = it,
                                modifier = Modifier.weight(1f),
                                tint = detalle.accentColor,
                            )
                        }
                        detalle.hora?.let {
                            MovimientoMetaChip(
                                icono = Icons.Default.AccessTime,
                                titulo = "Hora",
                                valor = it,
                                modifier = Modifier.weight(1f),
                                tint = detalle.accentColor,
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                MovimientoSectionTitle("Almacenes")
                MovimientoRutaCard(detalle.origen, detalle.destino, detalle.accentColor)

                Spacer(Modifier.height(14.dp))
                MovimientoSectionTitle("Productos")
                MovimientoProductosSection(detalle.lineas, detalle.accentColor)

                if (detalle.campos.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    MovimientoSectionTitle("Detalle del movimiento")
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = C.surface,
                        shadowElevation = 1.dp,
                    ) {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            detalle.campos.forEachIndexed { index, campo ->
                                if (index > 0) MovimientoDetalleDivider()
                                MovimientoDetalleFila(campo, detalle.accentColor)
                            }
                        }
                    }
                }

                if (detalle.notas.any { it.isNotBlank() }) {
                    Spacer(Modifier.height(14.dp))
                    MovimientoSectionTitle(detalle.notasTitulo)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = C.surfaceSoft,
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            detalle.notas.filter { it.isNotBlank() }.forEachIndexed { index, nota ->
                                if (index > 0) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = C.border.copy(alpha = 0.4f))
                                    Spacer(Modifier.height(8.dp))
                                }
                                Text(nota, fontSize = 14.sp, color = C.textPrimary, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovimientoSectionTitle(texto: String) {
    Text(
        texto,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = C.primary,
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun MovimientoRutaCard(origen: String, destino: String, tint: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = C.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MovimientoAlmacenPunto("Origen", origen, tint, Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, null, tint = C.textSecondary, modifier = Modifier.size(20.dp))
            MovimientoAlmacenPunto("Destino", destino, tint, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MovimientoAlmacenPunto(
    etiqueta: String,
    nombre: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warehouse, null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(etiqueta, fontSize = 10.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            nombre,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = C.textPrimary,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun MovimientoProductosSection(lineas: List<MovimientoDetalleLineaUi>, tint: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = C.surface,
        shadowElevation = 1.dp,
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            lineas.forEachIndexed { index, linea ->
                if (index > 0) MovimientoDetalleDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = tint.copy(alpha = 0.1f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Inventory2, null, tint = tint, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                linea.nombre,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = C.textPrimary,
                                lineHeight = 19.sp,
                            )
                            Text(
                                linea.cantidad,
                                fontSize = 12.sp,
                                color = C.textSecondary,
                                fontWeight = FontWeight.Medium,
                            )
                            if (linea.manejaSerie || linea.series.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "N° de serie",
                                    fontSize = 10.sp,
                                    color = C.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(6.dp))
                                if (linea.series.isEmpty()) {
                                    Text(
                                        "Producto con serie (sin números registrados)",
                                        fontSize = 12.sp,
                                        color = C.textSecondary,
                                        fontStyle = FontStyle.Italic,
                                    )
                                } else {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        linea.series.forEach { serie ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = tint.copy(alpha = 0.08f),
                                            ) {
                                                Text(
                                                    serie,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = tint,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovimientoDetalleFila(campo: MovimientoDetalleCampoUi, tint: Color) {
    val color = if (campo.valorSecundario) C.textSecondary else tint
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.1f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(campo.icono, null, tint = color, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(campo.etiqueta, fontSize = 11.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
            Text(
                campo.valor,
                fontSize = 14.sp,
                color = if (campo.valorSecundario) C.textSecondary else C.textPrimary,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 19.sp,
            )
        }
    }
}

@Composable
private fun MovimientoMetaChip(
    icono: ImageVector,
    titulo: String,
    valor: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = C.surface,
        shadowElevation = 1.dp,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, null, tint = tint, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(titulo, fontSize = 11.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
            Text(valor, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = C.textPrimary)
        }
    }
}

@Composable
private fun MovimientoDetalleDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp, end = 14.dp),
        thickness = 0.5.dp,
        color = C.border.copy(alpha = 0.35f),
    )
}
