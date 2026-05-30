package com.factapp.jhonny.ui.comprobantes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.colorArgb
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.etiqueta
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.filtrarPorRango
import com.factapp.jhonny.network.dto.formatearDiaElegante
import com.factapp.jhonny.network.dto.formatearRangoElegante
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.tienePdf
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.comprobantes.DetalleComprobanteModo
import com.factapp.jhonny.ui.compras.CompraDetalleSheet
import com.factapp.jhonny.ui.compras.ComprobanteDocumentIntents
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

private val C = ComprobanteEmitColors

private enum class PresetFecha {
    HOY,
    AYER,
    ULTIMOS_7,
    ESTE_MES,
    FECHA,
}

private fun PresetFecha.rango(hoy: LocalDate, fechaElegida: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
    PresetFecha.HOY -> hoy to hoy
    PresetFecha.AYER -> hoy.minusDays(1) to hoy.minusDays(1)
    PresetFecha.ULTIMOS_7 -> hoy.minusDays(6) to hoy
    PresetFecha.ESTE_MES -> YearMonth.from(hoy).atDay(1) to hoy
    PresetFecha.FECHA -> fechaElegida to fechaElegida
}

private fun List<Invoice>.filtrarBusqueda(query: String): List<Invoice> {
    if (query.isBlank()) return this
    val q = query.trim().lowercase()
    return filter { c ->
        c.etiquetaCompleta.lowercase().contains(q) ||
            c.receptor.nombre.lowercase().contains(q) ||
            c.receptor.documentoNumero.lowercase().contains(q) ||
            c.cliente?.razonSocial?.lowercase()?.contains(q) == true ||
            c.cliente?.numeroDoc?.contains(q) == true ||
            c.etiquetaTipo().lowercase().contains(q) ||
            c.estado.etiqueta().lowercase().contains(q)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprobantesEmitidosScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val hoy = remember { LocalDate.now() }

    var todos by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var preset by remember { mutableStateOf(PresetFecha.HOY) }
    var fechaElegida by remember { mutableStateOf(hoy) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var detalle by remember { mutableStateOf<Invoice?>(null) }

    val rango by remember(preset, fechaElegida, hoy) {
        derivedStateOf { preset.rango(hoy, fechaElegida) }
    }

    LaunchedEffect(companyRuc, token, rango) {
        cargando = true
        error = null
        withContext(Dispatchers.IO) {
            ComprobanteRepository.listarEmitidos(
                companyRuc = companyRuc,
                token = token,
                desde = rango.first,
                hasta = rango.second,
            )
                .onSuccess { todos = it }
                .onFailure { error = it.message ?: "No se pudieron cargar los comprobantes" }
        }
        cargando = false
    }

    val porFecha by remember(todos, rango) {
        derivedStateOf { todos.filtrarPorRango(rango.first, rango.second) }
    }

    val filtradas by remember(porFecha, busqueda) {
        derivedStateOf { porFecha.filtrarBusqueda(busqueda) }
    }

    val subtituloHeader by remember(filtradas.size, rango) {
        derivedStateOf {
            val (ini, fin) = rango
            val periodo = if (ini == fin) formatearDiaElegante(ini) else formatearRangoElegante(ini, fin)
            "${filtradas.size} documento(s) · $periodo"
        }
    }

    if (mostrarDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaElegida
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            fechaElegida = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        preset = PresetFecha.FECHA
                        mostrarDatePicker = false
                    },
                ) { Text("Aplicar", color = C.accent) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar", color = C.textSecondary)
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = "Comprobantes emitidos",
                subtitulo = subtituloHeader,
                icono = Icons.Default.ReceiptLong,
                onVolver = onVolver,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = C.accent)
                }
                error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error!!, color = C.textSecondary)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                FiltroFechaEmitidosCard(
                                    rango = rango,
                                    preset = preset,
                                    onPreset = { p ->
                                        preset = p
                                        if (p != PresetFecha.FECHA) mostrarDatePicker = false
                                    },
                                    onElegirFecha = {
                                        preset = PresetFecha.FECHA
                                        mostrarDatePicker = true
                                    },
                                )
                                Spacer(Modifier.height(10.dp))
                                CatalogoBusquedaBar(
                                    value = busqueda,
                                    onValueChange = { busqueda = it },
                                )
                            }
                        }
                        if (filtradas.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        if (busqueda.isNotBlank()) {
                                            "Sin resultados para tu búsqueda"
                                        } else {
                                            "No hay comprobantes en este periodo"
                                        },
                                        color = C.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else {
                            items(filtradas, key = { it.id }) { doc ->
                                ComprobanteEmitidoCard(
                                    doc = doc,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { detalle = doc },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    CompraDetalleSheet(
        compra = detalle,
        onDismiss = { detalle = null },
        modo = DetalleComprobanteModo.VENTA,
    )
}

@Composable
private fun FiltroFechaEmitidosCard(
    rango: Pair<LocalDate, LocalDate>,
    preset: PresetFecha,
    onPreset: (PresetFecha) -> Unit,
    onElegirFecha: () -> Unit,
) {
    val (ini, fin) = rango
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = C.accentSoft,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = C.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Periodo",
                        fontSize = 12.sp,
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        if (ini == fin) formatearDiaElegante(ini) else formatearRangoElegante(ini, fin),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = C.primary,
                        lineHeight = 20.sp,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FiltroChip("Hoy", preset == PresetFecha.HOY) { onPreset(PresetFecha.HOY) }
                FiltroChip("Ayer", preset == PresetFecha.AYER) { onPreset(PresetFecha.AYER) }
                FiltroChip("7 días", preset == PresetFecha.ULTIMOS_7) { onPreset(PresetFecha.ULTIMOS_7) }
                FiltroChip("Este mes", preset == PresetFecha.ESTE_MES) { onPreset(PresetFecha.ESTE_MES) }
                FiltroChip("Elegir fecha", preset == PresetFecha.FECHA, onClick = onElegirFecha)
            }
        }
    }
}

@Composable
private fun FiltroChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = C.accent,
            selectedLabelColor = Color.White,
            containerColor = C.surfaceSoft,
            labelColor = C.textPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = C.border.copy(alpha = 0.5f),
            selectedBorderColor = C.accent,
        ),
    )
}

@Composable
private fun ComprobanteEmitidoCard(
    doc: Invoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val estadoColor = Color(doc.estado.colorArgb())

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = C.accentSoft,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = C.accent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text(
                            doc.cliente?.razonSocial ?: doc.receptor.nombre,
                            fontWeight = FontWeight.Bold,
                            color = C.textPrimary,
                            fontSize = 15.sp,
                        )
                        Text(
                            buildString {
                                append(doc.etiquetaTipo())
                                append(' ')
                                append(doc.etiquetaCompleta)
                                doc.cliente?.etiquetaDocumento?.let { docEt ->
                                    append(" · ")
                                    append(docEt)
                                }
                            },
                            fontSize = 12.sp,
                            color = C.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatearSoles(doc.totales.total),
                        fontWeight = FontWeight.Bold,
                        color = C.primary,
                        fontSize = 16.sp,
                    )
                    doc.fechaEmision?.take(10)?.let {
                        Text(it, fontSize = 11.sp, color = C.textSecondary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = estadoColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        doc.estado.etiqueta(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = estadoColor,
                    )
                }
                Text(
                    "${doc.lineas.size} línea(s)",
                    fontSize = 12.sp,
                    color = C.textSecondary,
                )
            }
            if (doc.tienePdf()) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = C.border.copy(alpha = 0.35f),
                )
                TextButton(
                    onClick = { ComprobanteDocumentIntents.abrirPdf(context, doc) },
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Icon(
                        Icons.Outlined.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = C.accent,
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("Ver PDF", fontSize = 13.sp, color = C.accent)
                }
            }
        }
    }
}
