package com.factapp.jhonny.ui.compras

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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Archive
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
import com.factapp.jhonny.network.dto.fechaEmisionLegible
import com.factapp.jhonny.network.dto.filtrarPorRango
import com.factapp.jhonny.network.dto.formatearDiaElegante
import com.factapp.jhonny.network.dto.formatearRangoElegante
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.colorArgb
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.emisorParaPdf
import com.factapp.jhonny.network.dto.etiqueta
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.tieneCdrZip
import com.factapp.jhonny.network.dto.tienePdf
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import com.factapp.jhonny.ui.theme.LeyendaTiposComprobante
import com.factapp.jhonny.ui.theme.filtrarPorTipo
import com.factapp.jhonny.ui.theme.tipoPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private val ZONA_PERU: ZoneId = ZoneId.of("America/Lima")
private val C = ComprobanteEmitColors

private enum class PresetFechaCompras {
    TODOS,
    HOY,
    AYER,
    ULTIMOS_7,
    ESTE_MES,
    FECHA,
}

private fun PresetFechaCompras.rango(hoy: LocalDate, fechaElegida: LocalDate): Pair<LocalDate, LocalDate>? =
    when (this) {
        PresetFechaCompras.TODOS -> null
        PresetFechaCompras.HOY -> hoy to hoy
        PresetFechaCompras.AYER -> hoy.minusDays(1) to hoy.minusDays(1)
        PresetFechaCompras.ULTIMOS_7 -> hoy.minusDays(6) to hoy
        PresetFechaCompras.ESTE_MES -> YearMonth.from(hoy).atDay(1) to hoy
        PresetFechaCompras.FECHA -> fechaElegida to fechaElegida
    }

private fun List<Invoice>.filtrarCompras(query: String): List<Invoice> {
    if (query.isBlank()) return this
    val q = query.trim().lowercase()
    return filter { c ->
        c.etiquetaCompleta.lowercase().contains(q) ||
            c.proveedor.nombre.lowercase().contains(q) ||
            c.proveedor.ruc.lowercase().contains(q) ||
            c.companyRuc.lowercase().contains(q) ||
            c.etiquetaTipo().lowercase().contains(q) ||
            c.estado.etiqueta().lowercase().contains(q)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val emisorPdf = usuario.emisorParaPdf()
    val hoy = remember { LocalDate.now() }

    var todos by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var preset by remember { mutableStateOf(PresetFechaCompras.ULTIMOS_7) }
    var fechaElegida by remember { mutableStateOf(hoy) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var compraDetalle by remember { mutableStateOf<Invoice?>(null) }
    var tipoFiltro by remember { mutableStateOf<String?>(null) }

    val rango by remember(preset, fechaElegida, hoy) {
        derivedStateOf { preset.rango(hoy, fechaElegida) }
    }

    LaunchedEffect(companyRuc, token, preset, rango) {
        if (companyRuc.isBlank()) {
            cargando = false
            error = "Sin empresa vinculada"
            todos = emptyList()
            return@LaunchedEffect
        }
        cargando = true
        error = null
        withContext(Dispatchers.IO) {
            ComprobanteRepository.listarCompras(
                companyRuc = companyRuc,
                token = token,
                desde = rango?.first,
                hasta = rango?.second,
            )
                .onSuccess { todos = it }
                .onFailure { error = it.message ?: "No se pudieron cargar las compras" }
        }
        cargando = false
    }

    val porFecha by remember(todos, rango, preset) {
        derivedStateOf {
            val rangoActual = rango
            if (preset == PresetFechaCompras.TODOS || rangoActual == null) todos
            else todos.filtrarPorRango(rangoActual.first, rangoActual.second, ZONA_PERU)
        }
    }

    val filtradas by remember(porFecha, busqueda, tipoFiltro) {
        derivedStateOf {
            porFecha.filtrarCompras(busqueda).filtrarPorTipo(tipoFiltro)
        }
    }

    val subtituloHeader by remember(filtradas.size, rango, preset, tipoFiltro) {
        derivedStateOf {
            val rangoActual = rango
            val periodo = when {
                preset == PresetFechaCompras.TODOS || rangoActual == null -> "todos los periodos"
                rangoActual.first == rangoActual.second -> formatearDiaElegante(rangoActual.first)
                else -> formatearRangoElegante(rangoActual.first, rangoActual.second)
            }
            val tipo = LeyendaTiposComprobante.find { it.siglas == tipoFiltro }?.etiquetaCorta
            buildString {
                append("${filtradas.size} documento(s) recibido(s) · $periodo")
                if (tipo != null) append(" · $tipo")
            }
        }
    }

    if (mostrarDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaElegida
                .atStartOfDay(ZoneId.systemDefault())
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
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        preset = PresetFechaCompras.FECHA
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
        titulo = "Compras",
        subtitulo = subtituloHeader,
        icono = Icons.Default.ShoppingCart,
        onVolver = onVolver,
    ) { padding ->
        Column(
            modifier = Modifier
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
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                FiltroFechaComprasCard(
                                    rango = rango,
                                    preset = preset,
                                    onPreset = { p ->
                                        preset = p
                                        if (p != PresetFechaCompras.FECHA) mostrarDatePicker = false
                                    },
                                    onElegirFecha = {
                                        preset = PresetFechaCompras.FECHA
                                        mostrarDatePicker = true
                                    },
                                )
                                Spacer(Modifier.height(10.dp))
                                CatalogoBusquedaBar(
                                    value = busqueda,
                                    onValueChange = { busqueda = it },
                                    placeholder = "Buscar proveedor, RUC o número…",
                                )
                                if (porFecha.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    LeyendaTiposComprasRow(
                                        seleccionado = tipoFiltro,
                                        onSeleccionar = { siglas ->
                                            tipoFiltro = if (tipoFiltro == siglas) null else siglas
                                        },
                                    )
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
                                            busqueda.isNotBlank() -> "Sin resultados para tu búsqueda"
                                            tipoFiltro != null -> "No hay comprobantes de este tipo en el periodo"
                                            else ->
                                                "No hay compras en este periodo.\n" +
                                                    "Aparecen cuando otra empresa te factura con tu RUC."
                                        },
                                        color = C.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 20.sp,
                                    )
                                }
                            }
                        } else {
                            items(filtradas, key = { it.id }) { compra ->
                                CompraRecibidaCard(
                                    compra = compra,
                                    companyRuc = companyRuc,
                                    token = token,
                                    emisorFallback = emisorPdf,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { compraDetalle = compra },
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
        compra = compraDetalle,
        onDismiss = { compraDetalle = null },
        companyRuc = companyRuc,
        token = token,
        emisorFallback = emisorPdf,
    )
}

@Composable
private fun LeyendaTiposComprasRow(
    seleccionado: String?,
    onSeleccionar: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LeyendaTiposComprobante
            .filter { it.siglas in setOf("FACT", "BOL", "NC", "ND") }
            .forEach { tipo ->
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
                            Text(tipo.siglas, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(tipo.etiquetaCorta, fontSize = 12.sp)
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
private fun FiltroFechaComprasCard(
    rango: Pair<LocalDate, LocalDate>?,
    preset: PresetFechaCompras,
    onPreset: (PresetFechaCompras) -> Unit,
    onElegirFecha: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surfaceSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = C.accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Periodo", fontSize = 12.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
                    Text(
                        when {
                            preset == PresetFechaCompras.TODOS || rango == null -> "Todos los periodos"
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
                FiltroChipCompras("Todos", preset == PresetFechaCompras.TODOS) { onPreset(PresetFechaCompras.TODOS) }
                FiltroChipCompras("Hoy", preset == PresetFechaCompras.HOY) { onPreset(PresetFechaCompras.HOY) }
                FiltroChipCompras("Ayer", preset == PresetFechaCompras.AYER) { onPreset(PresetFechaCompras.AYER) }
                FiltroChipCompras("7 días", preset == PresetFechaCompras.ULTIMOS_7) { onPreset(PresetFechaCompras.ULTIMOS_7) }
                FiltroChipCompras("Este mes", preset == PresetFechaCompras.ESTE_MES) { onPreset(PresetFechaCompras.ESTE_MES) }
                FiltroChipCompras("Elegir fecha", preset == PresetFechaCompras.FECHA, onClick = onElegirFecha)
            }
        }
    }
}

@Composable
private fun FiltroChipCompras(
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
private fun CompraRecibidaCard(
    compra: Invoice,
    companyRuc: String,
    token: String?,
    emisorFallback: Company? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var descargandoPdf by remember(compra.id) { mutableStateOf(false) }
    val estadoColor = Color(compra.estado.colorArgb())
    val tipoPalette = compra.tipoPalette()
    val proveedor = compra.proveedor
    val muestraDocumentos = compra.tienePdf() || compra.tieneCdrZip()

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
            Column(Modifier.weight(1f)) {
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
                                    text = proveedor.nombre,
                                    fontWeight = FontWeight.Bold,
                                    color = C.textPrimary,
                                    fontSize = 15.sp,
                                )
                                Text(
                                    text = "RUC ${proveedor.ruc}",
                                    fontSize = 12.sp,
                                    color = C.textSecondary,
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatearSoles(compra.totales.total),
                                fontWeight = FontWeight.Bold,
                                color = C.primary,
                                fontSize = 16.sp,
                            )
                            compra.fechaEmisionLegible()?.let {
                                Text(text = it, fontSize = 11.sp, color = C.textSecondary)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${compra.etiquetaTipo()} ${compra.etiquetaCompleta}",
                            fontSize = 13.sp,
                            color = C.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = estadoColor.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = compra.estado.etiqueta(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = estadoColor,
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${compra.lineas.size} línea(s) · IGV ${formatearSoles(compra.totales.igv)}",
                        fontSize = 12.sp,
                        color = C.textSecondary,
                    )
                }
                if (muestraDocumentos) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = C.border.copy(alpha = 0.35f),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (compra.tienePdf()) {
                            TextButton(
                                enabled = !descargandoPdf,
                                onClick = {
                                    descargandoPdf = true
                                    scope.launch {
                                        ComprobanteDocumentIntents.abrirPdf(
                                            context = context,
                                            comprobante = compra,
                                            companyRuc = companyRuc,
                                            token = token,
                                            emisorFallback = emisorFallback,
                                        )
                                        descargandoPdf = false
                                    }
                                },
                            ) {
                                if (descargandoPdf) {
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
                                    if (descargandoPdf) "Abriendo…" else "Ver PDF",
                                    fontSize = 13.sp,
                                    color = C.accent,
                                )
                            }
                        }
                        if (compra.tieneCdrZip()) {
                            TextButton(
                                onClick = { ComprobanteDocumentIntents.abrirCdrZip(context, compra) },
                            ) {
                                Icon(
                                    Icons.Outlined.Archive,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = C.primary,
                                )
                                Spacer(Modifier.size(6.dp))
                                Text("CDR (.zip)", fontSize = 13.sp, color = C.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
