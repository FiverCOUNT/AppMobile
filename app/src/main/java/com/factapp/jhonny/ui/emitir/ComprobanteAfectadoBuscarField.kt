package com.factapp.jhonny.ui.emitir

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun ComprobanteAfectadoBuscarField(
    value: String,
    onValueChange: (String) -> Unit,
    sugerencias: List<Invoice>,
    cargando: Boolean,
    comprobanteSeleccionado: Invoice?,
    onSeleccionar: (Invoice) -> Unit,
    onLimpiarSeleccion: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Factura afectada",
    placeholder: String = "Busca por serie F001, número o cliente…",
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { texto ->
                onLimpiarSeleccion()
                onValueChange(texto)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            trailingIcon = {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = C.accent,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = C.accent,
                unfocusedBorderColor = C.border,
                focusedLabelColor = C.accent,
                cursorColor = C.accent,
                focusedTextColor = C.textPrimary,
                unfocusedTextColor = C.textPrimary,
            ),
            shape = RoundedCornerShape(12.dp),
        )

        if (comprobanteSeleccionado != null) {
            Text(
                text = "${comprobanteSeleccionado.etiquetaTipo()} seleccionada · ${formatearSoles(comprobanteSeleccionado.totales.total)}",
                fontSize = 12.sp,
                color = C.accent,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp),
            )
        } else if (value.isNotBlank() && sugerencias.isEmpty() && !cargando) {
            Text(
                text = "Sin facturas que coincidan. Verifica el RUC o la serie.",
                fontSize = 12.sp,
                color = C.textSecondary,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp),
            )
        }

        if (sugerencias.isNotEmpty() && comprobanteSeleccionado == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Facturas",
                    fontSize = 12.sp,
                    color = C.textSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 2.dp),
                )
                sugerencias.forEach { doc ->
                    ComprobanteAfectadoSugerenciaCard(
                        comprobante = doc,
                        onClick = { onSeleccionar(doc) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ComprobanteAfectadoSugerenciaCard(
    comprobante: Invoice,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = C.accent,
                modifier = Modifier.size(22.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
            ) {
                Text(
                    text = comprobante.etiquetaCompleta,
                    fontWeight = FontWeight.Bold,
                    color = C.textPrimary,
                    fontSize = 14.sp,
                )
                Text(
                    text = comprobante.receptor.nombre.ifBlank { "Sin nombre" },
                    fontSize = 12.sp,
                    color = C.textSecondary,
                    maxLines = 1,
                )
                Text(
                    text = "${comprobante.etiquetaTipo()} · ${comprobante.details.size} ítem(s)",
                    fontSize = 11.sp,
                    color = C.textSecondary,
                )
            }
            Text(
                text = formatearSoles(comprobante.totales.total),
                fontWeight = FontWeight.SemiBold,
                color = C.primary,
                fontSize = 13.sp,
            )
        }
    }
}
