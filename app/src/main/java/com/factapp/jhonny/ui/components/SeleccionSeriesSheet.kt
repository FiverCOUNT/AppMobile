package com.factapp.jhonny.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.ProductoSerieEstado
import com.factapp.jhonny.network.dto.model.delAlmacen
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

private enum class FiltroSeriesLista { TODAS, SELECCIONADAS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionSeriesSheet(
    visible: Boolean,
    companyRuc: String,
    almacenId: String,
    token: String?,
    catalogItem: CatalogItem?,
    seriesIniciales: List<ProductoSerie>,
    seriesExcluidasIds: Set<String> = emptySet(),
    /** Si se indica, solo se muestran estas series (p. ej. devolución NC) en lugar del stock disponible. */
    seriesEntregadas: List<ProductoSerie>? = null,
    onDismiss: () -> Unit,
    onConfirmar: (List<ProductoSerie>) -> Unit,
) {
    if (!visible || catalogItem == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var cargando by remember(catalogItem.id) { mutableStateOf(true) }
    var disponibles by remember(catalogItem.id) { mutableStateOf<List<ProductoSerie>>(emptyList()) }
    val seleccionadas = remember(catalogItem.id) { mutableStateListOf<String>() }
    var busquedaSeries by remember(catalogItem.id) { mutableStateOf("") }
    var filtroLista by remember(catalogItem.id) { mutableStateOf(FiltroSeriesLista.TODAS) }
    val modoDevolucion = seriesEntregadas != null

    val idsSeleccionados = seleccionadas.toList()
    val seriesFiltradas = remember(disponibles, busquedaSeries, filtroLista, idsSeleccionados, seriesExcluidasIds, modoDevolucion) {
        disponibles.filter { serie ->
            if (seriesExcluidasIds.contains(serie.id) && !seleccionadas.contains(serie.id)) return@filter false
            if (!modoDevolucion && serie.estado != ProductoSerieEstado.DISPONIBLE) {
                return@filter false
            }
            val coincideBusqueda = busquedaSeries.isBlank() ||
                serie.numeroSerie.contains(busquedaSeries.trim(), ignoreCase = true)
            val coincideFiltro = when (filtroLista) {
                FiltroSeriesLista.TODAS -> true
                FiltroSeriesLista.SELECCIONADAS -> seleccionadas.contains(serie.id)
            }
            coincideBusqueda && coincideFiltro
        }
    }

    suspend fun cargarDisponibles(): List<ProductoSerie> {
        if (modoDevolucion) return seriesEntregadas.orEmpty()
        if (almacenId.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            InventarioRepository.listarSeriesDisponibles(
                companyRuc = companyRuc,
                catalogItemId = catalogItem.id,
                token = token,
                almacenId = almacenId,
            ).getOrElse { emptyList() }
        }.delAlmacen(almacenId)
            .filter { !seriesExcluidasIds.contains(it.id) }
    }

    LaunchedEffect(catalogItem.id, visible, almacenId, seriesEntregadas, seriesExcluidasIds) {
        if (!visible) return@LaunchedEffect
        cargando = true
        busquedaSeries = ""
        filtroLista = FiltroSeriesLista.TODAS
        val lista = cargarDisponibles()
        disponibles = lista
        val idsValidos = lista.map { it.id }.toSet()
        seleccionadas.clear()
        seleccionadas.addAll(
            seriesIniciales.map { it.id }.filter { idsValidos.contains(it) },
        )
        cargando = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { scaffoldContentWithoutTopInset() },
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ComprobanteEmitHeader(
                titulo = if (modoDevolucion) "Series entregadas" else "Series disponibles",
                subtitulo = catalogItem.nombre,
                onVolver = onDismiss,
                mostrarDragHandle = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
                if (!modoDevolucion && almacenId.isBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Elige primero el almacén de salida para ver las series de esa bodega.",
                        color = C.textSecondary,
                        fontSize = 13.sp,
                    )
                    return@Column
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = busquedaSeries,
                    onValueChange = { busquedaSeries = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filtrar por número de serie") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = C.accent)
                    },
                    trailingIcon = {
                        if (busquedaSeries.isNotEmpty()) {
                            IconButton(onClick = { busquedaSeries = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = C.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = seriesFieldColors(),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SeriesFilterChip(
                        label = "Todas (${disponibles.size})",
                        selected = filtroLista == FiltroSeriesLista.TODAS,
                        onClick = { filtroLista = FiltroSeriesLista.TODAS },
                    )
                    SeriesFilterChip(
                        label = "Seleccionadas (${seleccionadas.size})",
                        selected = filtroLista == FiltroSeriesLista.SELECCIONADAS,
                        onClick = { filtroLista = FiltroSeriesLista.SELECCIONADAS },
                    )
                }
                Spacer(Modifier.height(10.dp))
                when {
                    cargando -> Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = C.accent)
                    }
                    disponibles.isEmpty() -> Text(
                        if (modoDevolucion) {
                            "No hay series entregadas al cliente para este producto en el documento afectado."
                        } else {
                            "No hay series disponibles en este almacén."
                        },
                        color = C.textSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    seriesFiltradas.isEmpty() -> Text(
                        "Ninguna serie coincide con el filtro.",
                        color = C.textSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.height(220.dp),
                    ) {
                        items(seriesFiltradas, key = { it.id }) { serie ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (seleccionadas.contains(serie.id)) {
                                            seleccionadas.remove(serie.id)
                                        } else {
                                            seleccionadas.add(serie.id)
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (seleccionadas.contains(serie.id)) C.accentSoft else C.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (seleccionadas.contains(serie.id)) C.accent else C.border.copy(alpha = 0.35f),
                                ),
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = seleccionadas.contains(serie.id),
                                        onCheckedChange = { checked ->
                                            if (checked) seleccionadas.add(serie.id)
                                            else seleccionadas.remove(serie.id)
                                        },
                                    )
                                    Text(serie.numeroSerie, color = C.textPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val alm = almacenId.takeIf { it.isNotBlank() }
                        val confirmadas = seleccionadas.mapNotNull { id ->
                            disponibles.find { it.id == id }?.let { serie ->
                                if (alm != null) serie.copy(almacenId = alm) else serie
                            }
                        }
                        if (confirmadas.isEmpty()) return@Button
                        onConfirmar(confirmadas)
                    },
                    enabled = seleccionadas.isNotEmpty() && !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.accent),
                ) {
                    Text(
                        "Confirmar ${seleccionadas.size} serie(s)",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesFilterChip(
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
private fun seriesFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = C.borderFocused,
    unfocusedBorderColor = C.border,
    focusedContainerColor = C.surface,
    unfocusedContainerColor = C.surface,
)
