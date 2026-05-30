package com.factapp.jhonny.ui.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.ProductoSerieEstado
import com.factapp.jhonny.network.dto.request.RegistrarEntradaRequest
import com.factapp.jhonny.network.dto.request.RegistrarMovimientoLineaRequest
import com.factapp.jhonny.network.dto.model.aLineaCatalogoItem
import com.factapp.jhonny.network.dto.model.requireItem
import com.factapp.jhonny.network.dto.model.aRegistrarMovimientoLinea
import com.factapp.jhonny.network.dto.disponibleParaIngreso
import com.factapp.jhonny.network.dto.etiquetaStock
import com.factapp.jhonny.network.dto.model.lineasListasParaIngreso
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.ui.components.EmitFormSheetHeader
import com.factapp.jhonny.ui.components.PartialOptionCard
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import androidx.compose.material.icons.outlined.QrCodeScanner
import com.factapp.jhonny.ui.emitir.CatalogoBuscarSheet
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarIngresoSheet(
    visible: Boolean,
    productosDisponibles: List<CatalogItem>,
    companyRuc: String,
    almacenes: List<Almacen>,
    onDismiss: () -> Unit,
    onRegistrar: (RegistrarEntradaRequest) -> Unit,
) {
    if (!visible) return

    val contentScrollState = rememberScrollState()
    val lineas = remember { mutableStateListOf<LineaCatalogoItem>() }
    var observaciones by remember { mutableStateOf("") }
    var mostrarBuscar by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }
    var lineaMenuSerieId by remember { mutableStateOf<String?>(null) }
    var lineaEscaneoSerieId by remember { mutableStateOf<String?>(null) }
    var almacenSeleccionadoId by remember { mutableStateOf<String?>(null) }

    val almacenId = almacenSeleccionadoId
        ?: almacenes.firstOrNull()?.id
        .orEmpty()
    val almacenNombre = almacenes.firstOrNull { it.id == almacenId }?.nombre
    val puedeAgregarProducto = almacenId.isNotBlank()
    val puedeRegistrar = almacenId.isNotBlank() && lineas.lineasListasParaIngreso()

    LaunchedEffect(visible, almacenes) {
        if (visible) {
            lineas.clear()
            observaciones = ""
            mostrarBuscar = false
            busqueda = ""
            lineaMenuSerieId = null
            lineaEscaneoSerieId = null
            almacenSeleccionadoId = almacenes.firstOrNull()?.id
        }
    }

    BackHandler(onBack = onDismiss)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            EmitFormSheetHeader(
                titulo = "Nuevo ingreso",
                subtitulo = almacenNombre?.let { "Entrada a $it" }
                    ?: "Mercadería que entra al almacén",
                icono = Icons.Default.Warehouse,
                onVolver = onDismiss,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(contentScrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                AlmacenSelectorSection(
                    titulo = "Almacén de destino",
                    subtitulo = "¿A qué bodega entra la mercadería?",
                    almacenes = almacenes,
                    seleccionadoId = almacenId.takeIf { it.isNotBlank() },
                    onSeleccionar = { id ->
                        if (id != almacenSeleccionadoId) {
                            almacenSeleccionadoId = id
                            lineas.clear()
                        }
                    },
                    vacioMensaje = "Crea un almacén en Más opciones → Almacenes",
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { mostrarBuscar = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = puedeAgregarProducto,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, C.accent),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = C.accent)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar producto del catálogo", color = C.accent, fontWeight = FontWeight.SemiBold)
                }

                if (!puedeAgregarProducto) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Selecciona el almacén de destino antes de agregar productos.",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                    )
                }

                if (lineas.isEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Elige productos activos con inventario. Si no hay ninguno, revisa el catálogo.",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                        lineHeight = 18.sp,
                    )
                } else {
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(220.dp),
                    ) {
                        items(lineas, key = { it.requireItem().id }) { linea ->
                            LineaIngresoEditor(
                                linea = linea,
                                onCantidadChange = { nueva ->
                                    val idx = lineas.indexOfFirst { it.requireItem().id == linea.requireItem().id }
                                    if (idx >= 0) {
                                        lineas[idx] = if (linea.requireItem().manejaSerie) {
                                            lineas[idx]
                                        } else {
                                            lineas[idx].copy(cantidad = nueva)
                                        }
                                    }
                                },
                                onAbrirMenuSerial = { lineaMenuSerieId = linea.requireItem().id },
                                onEliminar = {
                                    lineas.removeAll { it.requireItem().id == linea.requireItem().id }
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Observaciones (opcional)") },
                    placeholder = { Text("Ej. Compra proveedor, nota de ingreso") },
                    singleLine = false,
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors(),
                )

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val request = RegistrarEntradaRequest(
                            companyRuc = companyRuc,
                            almacenId = almacenId,
                            lineas = lineas.map { it.aRegistrarMovimientoLinea() },
                            observaciones = observaciones.takeIf { it.isNotBlank() },
                        )
                        onDismiss()
                        onRegistrar(request)
                    },
                    enabled = puedeRegistrar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.accent),
                ) {
                    Text("Registrar ingreso", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    CatalogoBuscarSheet(
        visible = mostrarBuscar,
        items = productosDisponibles,
        busqueda = busqueda,
        onBusquedaChange = { busqueda = it },
        onDismiss = {
            mostrarBuscar = false
            busqueda = ""
        },
        onItemSeleccionado = { item ->
            if (lineas.none { it.requireItem().id == item.id }) {
                lineas += item.aLineaCatalogoItem(
                    cantidad = if (item.manejaSerie) 0.0 else 1.0,
                    almacenId = almacenId,
                )
            }
            mostrarBuscar = false
            busqueda = ""
        },
    )

    val lineaMenuSerie = lineas.firstOrNull { it.requireItem().id == lineaMenuSerieId }
    IngresoSeriesActionSheet(
        item = lineaMenuSerie?.catalogItem,
        visible = lineaMenuSerie != null,
        onDismiss = { lineaMenuSerieId = null },
        onIngresoMasivo = {
            lineaMenuSerieId = null
            lineaEscaneoSerieId = it.id
        },
    )

    val lineaEscaneoSerie = lineas.firstOrNull { it.requireItem().id == lineaEscaneoSerieId }
    EscaneoMasivoSeriesSheet(
        visible = lineaEscaneoSerie != null,
        companyRuc = companyRuc,
        catalogItem = lineaEscaneoSerie?.catalogItem,
        seriesIniciales = lineaEscaneoSerie?.series.orEmpty(),
        onDismiss = { lineaEscaneoSerieId = null },
        onConfirmar = { series ->
            val idx = lineas.indexOfFirst { it.requireItem().id == lineaEscaneoSerie?.requireItem()?.id }
            if (idx >= 0) {
                lineas[idx] = lineas[idx].copy(
                    series = series,
                    numerosSerie = emptyList(),
                    almacenId = almacenId,
                    cantidad = series.size.toDouble(),
                )
            }
            lineaEscaneoSerieId = null
        },
    )
}

@Composable
private fun LineaIngresoEditor(
    linea: LineaCatalogoItem,
    onCantidadChange: (Double) -> Unit,
    onAbrirMenuSerial: () -> Unit,
    onEliminar: () -> Unit,
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (linea.requireItem().manejaSerie) Modifier.clickable(onClick = onAbrirMenuSerial)
                else Modifier,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.5f)),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(linea.requireItem().nombre, fontWeight = FontWeight.SemiBold, color = C.textPrimary, fontSize = 14.sp)

                Text(
                    text = buildString {
                        linea.requireItem().codigo?.let { append("$it · ") }
                        if (linea.requireItem().manejaSerie) {
                            append("${linea.series.size} series capturadas · ")
                        } else {
                            linea.requireItem().etiquetaStock()?.let { append("$it · ") }
                        }
                        append(
                            if (linea.requireItem().manejaSerie) {
                                "Producto serializado"
                            } else {
                                "Cantidad a ingresar"
                            },
                        )
                    },
                    fontSize = 12.sp,
                    color = C.textSecondary,
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            if (linea.requireItem().manejaSerie) {
                IconButton(onClick = onAbrirMenuSerial, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Ingreso masivo de series", tint = C.accent)
                }
            } else {
                OutlinedTextField(

                    value = linea.cantidad.takeIf { it > 0 }?.let {
                        if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                    }.orEmpty(),
                    onValueChange = { txt ->
                        val v = txt.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onCantidadChange(v)
                    },
                    modifier = Modifier.width(60.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp),
                    colors = outlinedFieldColors(),
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = C.textSecondary)
            }
        }


    }
}

@Composable
private fun IngresoSeriesActionSheet(
    item: CatalogItem?,
    visible: Boolean,
    onDismiss: () -> Unit,
    onIngresoMasivo: (CatalogItem) -> Unit,
) {
    if (!visible || item == null) return

    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = "Series del producto",
        subtitle = item.nombre,
        theme = PartialSheetTheme.Emit,
    ) {
        PartialOptionCard(
            icon = Icons.Outlined.QrCodeScanner,
            titulo = "Ingreso masivo de series",
            detalle = "Escanea o pega números de serie en lote",
            onClick = {
                onIngresoMasivo(item)
                onDismiss()
            },
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EscaneoMasivoSeriesSheet(
    visible: Boolean,
    companyRuc: String,
    catalogItem: CatalogItem?,
    seriesIniciales: List<ProductoSerie>,
    onDismiss: () -> Unit,
    onConfirmar: (List<ProductoSerie>) -> Unit,
) {
    if (!visible || catalogItem == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var input by remember(catalogItem.id) { mutableStateOf("") }
    val series = remember(catalogItem.id) { mutableStateListOf<ProductoSerie>() }

    LaunchedEffect(catalogItem.id, visible) {
        if (visible) {
            series.clear()
            series.addAll(seriesIniciales)
            input = ""
        }
    }

    fun agregarDesdeTexto(raw: String) {
        val existentes = series.map { it.numeroSerie.lowercase() }.toMutableSet()
        val nuevas = raw.split("\n", "\r", ",", ";", "\t", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { existentes.add(it.lowercase()) }
            .mapIndexed { index, numero ->
                ProductoSerie(
                    id = "scan-${System.currentTimeMillis()}-$index",
                    companyRuc = companyRuc,
                    catalogItemId = catalogItem.id,
                    numeroSerie = numero,
                    estado = ProductoSerieEstado.DISPONIBLE,
                )
            }
        if (nuevas.isNotEmpty()) series.addAll(nuevas)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
        modifier = Modifier.imePadding(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EmitFormSheetHeader(
                titulo = "Escaneo masivo",
                subtitulo = catalogItem.nombre,
                mostrarDragHandle = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
            Text(
                text = "Escanea con pistola (Enter) o pega lote de series.",
                color = C.textSecondary,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { nuevo ->
                    if ('\n' in nuevo || '\r' in nuevo) {
                        agregarDesdeTexto(nuevo)
                        input = ""
                    } else {
                        input = nuevo
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Serie / lote de series") },
                placeholder = { Text("Ej. DL-SN-2026-00489") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = outlinedFieldColors(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        agregarDesdeTexto(input)
                        input = ""
                    },
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar")
                }
                TextButton(onClick = { series.clear() }) {
                    Text("Limpiar", color = C.accent)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Series capturadas: ${series.size}",
                color = C.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = C.surface.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, C.border.copy(alpha = 0.35f)),
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .height(160.dp)
                        .padding(8.dp),
                ) {
                    items(series, key = { it.id }) { serie ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = C.surface.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, C.border.copy(alpha = 0.3f)),
                        ) {
                            Text(
                                text = serie.numeroSerie,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                color = C.textPrimary,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { onConfirmar(series.toList()) },
                enabled = series.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = C.accent),
            ) {
                Text("Confirmar series", fontWeight = FontWeight.Bold)
            }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = C.borderFocused,
    unfocusedBorderColor = C.border,
    focusedContainerColor = C.surface,
    unfocusedContainerColor = C.surface,
)
