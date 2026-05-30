package com.factapp.jhonny.ui.emitir

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.etiquetaStock
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoBuscarSheet(
    visible: Boolean,
    items: List<CatalogItem>,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onItemSeleccionado: (CatalogItem) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filtrados = items.filter { item ->
        if (busqueda.isBlank()) return@filter true
        val q = busqueda.trim().lowercase()
        item.nombre.lowercase().contains(q) ||
            item.codigo?.lowercase()?.contains(q) == true ||
            item.descripcion?.lowercase()?.contains(q) == true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ComprobanteEmitHeader(
                titulo = "Buscar en catálogo",
                subtitulo = "Toca un ítem para agregarlo al comprobante",
                mostrarDragHandle = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(C.background)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = onBusquedaChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nombre, código o descripción") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = C.accent)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.borderFocused,
                        unfocusedBorderColor = C.border,
                        focusedContainerColor = C.surface,
                        unfocusedContainerColor = C.surface,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (filtrados.isEmpty()) {
                    Text(
                        text = if (items.isEmpty()) {
                            "No hay ítems en el catálogo"
                        } else {
                            "Sin resultados para \"$busqueda\""
                        },
                        color = C.textSecondary,
                        fontSize = 14.sp,
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(320.dp),
                    ) {
                        items(filtrados, key = { it.id }) { item ->
                            CatalogoBuscarFila(
                                item = item,
                                onClick = { onItemSeleccionado(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogoBuscarFila(
    item: CatalogItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    fontWeight = FontWeight.SemiBold,
                    color = C.textPrimary,
                )
                Text(
                    text = buildString {
                        append(item.tipo.etiqueta)
                        item.codigo?.let { append(" · $it") }
                        item.etiquetaStock()?.let { append(" · $it") }
                    },
                    fontSize = 12.sp,
                    color = C.textSecondary,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatearSoles(item.precioUnitario),
                    fontWeight = FontWeight.Bold,
                    color = C.accent,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = C.accent,
                )
            }
        }
    }
}
