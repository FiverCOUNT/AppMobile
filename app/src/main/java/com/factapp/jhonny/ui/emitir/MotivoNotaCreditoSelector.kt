package com.factapp.jhonny.ui.emitir

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.MotivoNotaCreditoSunat
import com.factapp.jhonny.network.dto.MotivosNotaCreditoSunat
import com.factapp.jhonny.ui.components.EmitFormSheetHeader
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun MotivoNotaCreditoSelector(
    codigoSeleccionado: String?,
    onCodigoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarOpciones by remember { mutableStateOf(false) }
    val seleccionado = codigoSeleccionado?.let { MotivosNotaCreditoSunat.porCodigo(it) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Motivo de la nota de crédito (SUNAT)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = C.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { mostrarOpciones = true },
        ) {
            OutlinedTextField(
                value = seleccionado?.let { "${it.codigo} · ${it.descripcion}" }.orEmpty(),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
            placeholder = {
                Text(
                    "Selecciona un motivo del catálogo 09",
                    color = C.textSecondary.copy(alpha = 0.55f),
                    fontSize = 14.sp,
                )
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = C.accent)
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = C.border.copy(alpha = 0.5f),
                disabledContainerColor = C.surfaceSoft,
                disabledTextColor = C.textPrimary,
                disabledPlaceholderColor = C.textSecondary.copy(alpha = 0.55f),
            ),
        )
        }
        Text(
            text = "Catálogo N.° 09 — tipo de nota de crédito electrónica",
            fontSize = 11.sp,
            color = C.textSecondary,
            modifier = Modifier.padding(start = 2.dp, top = 4.dp),
        )
    }

    if (mostrarOpciones) {
        MotivoNotaCreditoOpcionesSheet(
            codigoSeleccionado = codigoSeleccionado,
            onDismiss = { mostrarOpciones = false },
            onSeleccionar = { codigo ->
                onCodigoChange(codigo)
                mostrarOpciones = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MotivoNotaCreditoOpcionesSheet(
    codigoSeleccionado: String?,
    onDismiss: () -> Unit,
    onSeleccionar: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EmitFormSheetHeader(
                titulo = "Motivo SUNAT",
                subtitulo = "Catálogo N.° 09 · ${MotivosNotaCreditoSunat.catalogo.size} opciones",
                mostrarDragHandle = true,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(C.background)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = "Más usados",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = C.accent,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    )
                }
                items(MotivosNotaCreditoSunat.comunes, key = { "comun-${it.codigo}" }) { motivo ->
                    MotivoNotaCreditoOpcionCard(
                        motivo = motivo,
                        seleccionado = motivo.codigo == codigoSeleccionado,
                        onClick = { onSeleccionar(motivo.codigo) },
                    )
                }
                item {
                    Text(
                        text = "Otros motivos SUNAT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = C.textSecondary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                    )
                }
                items(MotivosNotaCreditoSunat.otros, key = { "otro-${it.codigo}" }) { motivo ->
                    MotivoNotaCreditoOpcionCard(
                        motivo = motivo,
                        seleccionado = motivo.codigo == codigoSeleccionado,
                        onClick = { onSeleccionar(motivo.codigo) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MotivoNotaCreditoOpcionCard(
    motivo: MotivoNotaCreditoSunat,
    seleccionado: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) C.accentSoft else C.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = motivo.codigo,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = C.accent,
                modifier = Modifier.width(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = motivo.descripcion,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = C.textPrimary,
                )
            }
            if (seleccionado) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = C.accent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
