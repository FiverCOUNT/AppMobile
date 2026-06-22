package com.factapp.jhonny.ui.emitir

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.factapp.jhonny.network.dto.PresetPeriodoComprobante
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import java.time.LocalDate

private val C = ComprobanteEmitColors

@Composable
fun ComprobanteAfectadoPeriodoFiltros(
    preset: PresetPeriodoComprobante,
    onPreset: (PresetPeriodoComprobante) -> Unit,
    onElegirFecha: () -> Unit,
    hoy: LocalDate,
    fechaElegida: LocalDate,
    cantidadVisible: Int,
    cargando: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = when {
                cargando -> "Cargando comprobantes…"
                cantidadVisible == 0 -> "Periodo: ${preset.etiquetaPeriodo(hoy, fechaElegida)} · sin resultados"
                else -> "${cantidadVisible} comprobante(s) · ${preset.etiquetaPeriodo(hoy, fechaElegida)}"
            },
            fontSize = 12.sp,
            color = C.textSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PresetPeriodoComprobante.entries.forEach { opcion ->
                val selected = preset == opcion
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (opcion == PresetPeriodoComprobante.FECHA) onElegirFecha()
                        else onPreset(opcion)
                    },
                    label = {
                        Text(
                            text = if (opcion == PresetPeriodoComprobante.FECHA) "Elegir fecha" else opcion.etiquetaCorta(),
                            fontSize = 13.sp,
                        )
                    },
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
        }
    }
}
