package com.factapp.jhonny.ui.inventario

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun AlmacenSelectorSection(
    titulo: String,
    subtitulo: String,
    almacenes: List<Almacen>,
    seleccionadoId: String?,
    onSeleccionar: (String) -> Unit,
    vacioMensaje: String = "No hay almacenes configurados",
) {
    Text(titulo, fontWeight = FontWeight.SemiBold, color = C.primary, fontSize = 14.sp)
    Text(subtitulo, fontSize = 12.sp, color = C.textSecondary)
    Spacer(Modifier.height(8.dp))
    if (almacenes.isEmpty()) {
        Text(vacioMensaje, fontSize = 13.sp, color = C.textSecondary)
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            almacenes.forEach { alm ->
                AlmacenFilterChip(
                    label = alm.nombre,
                    selected = seleccionadoId == alm.id,
                    onClick = { onSeleccionar(alm.id) },
                )
            }
        }
    }
}

@Composable
fun AlmacenFilterChip(
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
