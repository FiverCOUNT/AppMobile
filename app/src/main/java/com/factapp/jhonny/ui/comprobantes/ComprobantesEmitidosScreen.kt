package com.factapp.jhonny.ui.comprobantes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.colorArgb
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.emisorParaPdf
import com.factapp.jhonny.network.dto.etiqueta
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.fechaEmisionLegible
import com.factapp.jhonny.network.dto.filtrarPorRango
import com.factapp.jhonny.network.dto.formatearDiaElegante
import com.factapp.jhonny.network.dto.formatearRangoElegante
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.motivoRechazoSunat
import com.factapp.jhonny.network.dto.puedeReenviar
import com.factapp.jhonny.network.dto.tienePdf
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Receipt
import com.factapp.jhonny.network.ComprobanteRepository.PdfFormato
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.comprobantes.DetalleComprobanteModo
import com.factapp.jhonny.ui.compras.CompraDetalleSheet
import com.factapp.jhonny.ui.compras.ComprobanteDocumentIntents
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import com.factapp.jhonny.ui.theme.LeyendaTiposComprobante
import com.factapp.jhonny.ui.theme.filtrarPorTipo
import com.factapp.jhonny.ui.theme.tipoPalette
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth

private val ZONA_PERU: ZoneId = ZoneId.of("America/Lima")

private val C = ComprobanteEmitColors

private enum class PresetFecha {
    TODOS,
    HOY,
    AYER,
    ULTIMOS_7,
    ESTE_MES,
    FECHA,
}

private fun PresetFecha.rango(hoy: LocalDate, fechaElegida: LocalDate): Pair<LocalDate, LocalDate>? = when (this) {
    PresetFecha.TODOS -> null
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
    onVolver: (() -> Unit)? = null,
    comprobanteIdFocus: String? = null,
    onComprobanteFocusClear: (() -> Unit)? = null,
) {
    BackHandler(enabled = onVolver != null, onBack = { onVolver?.invoke() })

    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val emisorPdf = usuario.emisorParaPdf()
    val hoy = remember { LocalDate.now() }

    var todos by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var preset by remember { mutableStateOf(PresetFecha.ULTIMOS_7) }
    var fechaElegida by remember { mutableStateOf(hoy) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var detalle by remember { mutableStateOf<Invoice?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var tipoFiltro by remember { mutableStateOf<String?>(null) }
    var autoDetalleAbierto by remember(comprobanteIdFocus) { mutableStateOf(false) }

    LaunchedEffect(comprobanteIdFocus) {
        if (!comprobanteIdFocus.isNullOrBlank()) {
            preset = PresetFecha.TODOS
            busqueda = ""
            tipoFiltro = null
            autoDetalleAbierto = false
        }
    }

    val rango by remember(preset, fechaElegida, hoy) {
        derivedStateOf { preset.rango(hoy, fechaElegida) }
    }

    LaunchedEffect(companyRuc, token, preset, rango, refreshKey) {
        if (companyRuc.isBlank()) {
            cargando = false
            error = "Sin empresa vinculada"
            todos = emptyList()
            return@LaunchedEffect
        }
        cargando = true
        error = null
        withContext(Dispatchers.IO) {
            ComprobanteRepository.listarEmitidos(
                companyRuc = companyRuc,
                token = token,
                desde = rango?.first,
                hasta = rango?.second,
            )
                .onSuccess { todos = it }
                .onFailure { error = it.message ?: "No se pudieron cargar los comprobantes" }
        }
        cargando = false
    }

    val porFecha by remember(todos, rango, preset) {
        derivedStateOf {
            val rangoActual = rango
            if (preset == PresetFecha.TODOS || rangoActual == null) todos
            else todos.filtrarPorRango(rangoActual.first, rangoActual.second, ZONA_PERU)
        }
    }

    val filtradas by remember(porFecha, busqueda, tipoFiltro, comprobanteIdFocus) {
        derivedStateOf {
            val base = porFecha
                .filtrarBusqueda(busqueda)
                .filtrarPorTipo(tipoFiltro)
            val focusId = comprobanteIdFocus?.trim().orEmpty()
            if (focusId.isBlank()) base
            else base.filter { it.id == focusId }
        }
    }

    LaunchedEffect(todos, comprobanteIdFocus, cargando, autoDetalleAbierto) {
        val focusId = comprobanteIdFocus?.trim().orEmpty()
        if (focusId.isBlank() || cargando || autoDetalleAbierto) return@LaunchedEffect
        todos.find { it.id == focusId }?.let { doc ->
            detalle = doc
            autoDetalleAbierto = true
        }
    }

    val subtituloHeader by remember(filtradas.size, rango, preset, tipoFiltro, comprobanteIdFocus) {
        derivedStateOf {
            val focusId = comprobanteIdFocus?.trim().orEmpty()
            if (focusId.isNotBlank()) {
                val doc = filtradas.firstOrNull()
                return@derivedStateOf if (doc != null) {
                    "Comprobante vinculado · ${doc.etiquetaCompleta}"
                } else {
                    "Comprobante vinculado · no encontrado en el listado"
                }
            }
            val rangoActual = rango
            val periodo = when {
                preset == PresetFecha.TODOS || rangoActual == null -> "todos los periodos"
                rangoActual.first == rangoActual.second -> formatearDiaElegante(rangoActual.first)
                else -> formatearRangoElegante(rangoActual.first, rangoActual.second)
            }
            val tipo = LeyendaTiposComprobante.find { it.siglas == tipoFiltro }?.etiquetaCorta
            buildString {
                append("${filtradas.size} documento(s) · $periodo")
                if (tipo != null) append(" · $tipo")
            }
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

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Comprobantes emitidos",
        subtitulo = subtituloHeader,
        icono = Icons.Default.ReceiptLong,
        onVolver = onVolver,
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
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
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
                            Column(
                                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                if (!comprobanteIdFocus.isNullOrBlank()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = C.accent.copy(alpha = 0.1f),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    "Vista filtrada",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = C.accent,
                                                )
                                                Text(
                                                    "Mostrando solo el comprobante vinculado al movimiento",
                                                    fontSize = 11.sp,
                                                    color = C.textSecondary,
                                                )
                                            }
                                            if (onComprobanteFocusClear != null) {
                                                TextButton(onClick = onComprobanteFocusClear) {
                                                    Text("Ver todos", color = C.accent)
                                                }
                                            }
                                        }
                                    }
                                } else {
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
                                    if (porFecha.isNotEmpty()) {
                                        Spacer(Modifier.height(12.dp))
                                        LeyendaTiposComprobanteRow(
                                            seleccionado = tipoFiltro,
                                            onSeleccionar = { siglas ->
                                                tipoFiltro = if (tipoFiltro == siglas) null else siglas
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        if (filtradas.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        when {
                                            !comprobanteIdFocus.isNullOrBlank() ->
                                                "No se encontró el comprobante vinculado"
                                            busqueda.isNotBlank() ->
                                                "Sin resultados para tu búsqueda"
                                            tipoFiltro != null ->
                                                "No hay comprobantes de este tipo en el periodo"
                                            else ->
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
                                    companyRuc = companyRuc,
                                    token = token,
                                    emisorFallback = emisorPdf,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { detalle = doc },
                                    onReenviado = { refreshKey++ },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    CompraDetalleSheet(
        compra = detalle,
        onDismiss = { detalle = null },
        modo = DetalleComprobanteModo.VENTA,
        companyRuc = companyRuc,
        token = token,
        emisorFallback = emisorPdf,
        onReenviado = {
            refreshKey++
            detalle = null
        },
    )
}

@Composable
private fun LeyendaTiposComprobanteRow(
    seleccionado: String?,
    onSeleccionar: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LeyendaTiposComprobante.forEach { tipo ->
            val activo = seleccionado == tipo.siglas
            FilterChip(
                selected = activo,
                onClick = { onSeleccionar(tipo.siglas) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (activo) Color.White else tipo.accent,
                                    CircleShape,
                                ),
                        )
                        Text(
                            tipo.siglas,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            tipo.etiquetaCorta,
                            fontSize = 12.sp,
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = tipo.accent,
                    selectedLabelColor = Color.White,
                    containerColor = tipo.soft.copy(alpha = 0.55f),
                    labelColor = tipo.title,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = activo,
                    borderColor = tipo.accent.copy(alpha = 0.45f),
                    selectedBorderColor = tipo.accent,
                ),
            )
        }
    }
}

@Composable
private fun FiltroFechaEmitidosCard(
    rango: Pair<LocalDate, LocalDate>?,
    preset: PresetFecha,
    onPreset: (PresetFecha) -> Unit,
    onElegirFecha: () -> Unit,
) {
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
                        when {
                            preset == PresetFecha.TODOS || rango == null -> "Todos los comprobantes"
                            rango.first == rango.second -> formatearDiaElegante(rango.first)
                            else -> formatearRangoElegante(rango.first, rango.second)
                        },
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
                FiltroChip("Todos", preset == PresetFecha.TODOS) { onPreset(PresetFecha.TODOS) }
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
    companyRuc: String,
    token: String?,
    emisorFallback: Company? = null,
    onClick: () -> Unit,
    onReenviado: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var descargandoPdfFormato by remember(doc.id) { mutableStateOf<PdfFormato?>(null) }
    var reenviando by remember(doc.id) { mutableStateOf(false) }
    val estadoColor = Color(doc.estado.colorArgb())
    val tipoPalette = doc.tipoPalette()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(tipoPalette.accent),
            )
            Column(
                Modifier
                    .weight(1f)
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
                        color = tipoPalette.soft,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                tipoPalette.icon,
                                contentDescription = null,
                                tint = tipoPalette.accent,
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
                                append(doc.etiquetaCompleta)
                                doc.cliente?.etiquetaDocumento?.let { docEt ->
                                    append(" · ")
                                    append(docEt)
                                }
                            },
                            fontSize = 12.sp,
                            color = tipoPalette.title,
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
                    doc.fechaEmisionLegible()?.let {
                        Text(it, fontSize = 11.sp, color = C.textSecondary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (doc.estado == ComprobanteEstado.RECHAZADO) {
                doc.motivoRechazoSunat()?.let { motivo ->
                    Text(
                        text = motivo,
                        fontSize = 12.sp,
                        color = estadoColor,
                        lineHeight = 16.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
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
            if (doc.tienePdf() || doc.puedeReenviar()) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = C.border.copy(alpha = 0.35f),
                )
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (doc.puedeReenviar()) {
                        TextButton(
                            enabled = !reenviando,
                            onClick = {
                                reenviando = true
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        ComprobanteRepository.reenviar(companyRuc, token, doc.id)
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
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                            onReenviado()
                                        },
                                        onFailure = {
                                            android.widget.Toast.makeText(
                                                context,
                                                it.message ?: "No se pudo reenviar",
                                                android.widget.Toast.LENGTH_LONG,
                                            ).show()
                                        },
                                    )
                                }
                            },
                        ) {
                            if (reenviando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = C.primary,
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = C.primary,
                                )
                            }
                            Spacer(Modifier.size(6.dp))
                            Text(
                                if (reenviando) "Reenviando…" else "Reenviar",
                                fontSize = 13.sp,
                                color = C.primary,
                            )
                        }
                    }
                    if (doc.tienePdf()) {
                        TextButton(
                            enabled = descargandoPdfFormato == null,
                            onClick = {
                                descargandoPdfFormato = PdfFormato.A4
                                scope.launch {
                                    ComprobanteDocumentIntents.abrirPdf(
                                        context = context,
                                        comprobante = doc,
                                        companyRuc = companyRuc,
                                        token = token,
                                        formato = PdfFormato.A4,
                                        emisorFallback = emisorFallback,
                                    )
                                    descargandoPdfFormato = null
                                }
                            },
                        ) {
                            if (descargandoPdfFormato == PdfFormato.A4) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = C.accent,
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = C.accent,
                                )
                            }
                            Spacer(Modifier.size(6.dp))
                            Text(
                                if (descargandoPdfFormato == PdfFormato.A4) "Descargando…" else "PDF A4",
                                fontSize = 13.sp,
                                color = C.accent,
                            )
                        }
                        TextButton(
                            enabled = descargandoPdfFormato == null,
                            onClick = {
                                descargandoPdfFormato = PdfFormato.TICKET
                                scope.launch {
                                    ComprobanteDocumentIntents.abrirPdf(
                                        context = context,
                                        comprobante = doc,
                                        companyRuc = companyRuc,
                                        token = token,
                                        formato = PdfFormato.TICKET,
                                        emisorFallback = emisorFallback,
                                    )
                                    descargandoPdfFormato = null
                                }
                            },
                        ) {
                            if (descargandoPdfFormato == PdfFormato.TICKET) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = C.accent,
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = C.accent,
                                )
                            }
                            Spacer(Modifier.size(6.dp))
                            Text(
                                if (descargandoPdfFormato == PdfFormato.TICKET) "Descargando…" else "Ticket",
                                fontSize = 13.sp,
                                color = C.accent,
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
