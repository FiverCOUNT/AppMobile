package com.factapp.jhonny.ui.emitir

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.factapp.jhonny.GuiaRemisionEventoOpcion
import com.factapp.jhonny.GuiaRemisionOpcion
import com.factapp.jhonny.ui.components.PartialOptionCard
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme

private val GuiaTeal = Color(0xFF00838F)
private val GuiaTealSoft = Color(0xFFB2EBF2)
private val GuiaTealDark = Color(0xFF006064)
private val GuiaOrange = Color(0xFFEF6C00)
private val GuiaOrangeSoft = Color(0xFFFFE0B2)
private val GuiaPurple = Color(0xFF6A1B9A)
private val GuiaPurpleSoft = Color(0xFFE1BEE7)

@Composable
fun GuiaRemisionOpcionesSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpcion: (GuiaRemisionOpcion) -> Unit,
) {
    if (!visible) return
    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = "Guía de remisión",
        subtitle = "Elige el tipo de GRE según SUNAT",
        theme = PartialSheetTheme.Emit,
    ) {
        GuiaRemisionOpcion.entries.forEachIndexed { index, opcion ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            val (tint, soft, title) = when (opcion) {
                GuiaRemisionOpcion.REMITENTE -> Triple(GuiaTeal, GuiaTealSoft, GuiaTealDark)
                GuiaRemisionOpcion.TRANSPORTISTA -> Triple(GuiaOrange, GuiaOrangeSoft, GuiaOrange)
                GuiaRemisionOpcion.EVENTOS -> Triple(GuiaPurple, GuiaPurpleSoft, GuiaPurple)
            }
            val icon = when (opcion) {
                GuiaRemisionOpcion.REMITENTE -> Icons.Default.Outbox
                GuiaRemisionOpcion.TRANSPORTISTA -> Icons.Default.LocalShipping
                GuiaRemisionOpcion.EVENTOS -> Icons.Default.Event
            }
            val detalle = buildString {
                if (opcion.codigoSunat.isNotBlank()) append("Cat. ${opcion.codigoSunat} · ")
                append(opcion.detalle)
            }
            PartialOptionCard(
                icon = icon,
                titulo = opcion.titulo,
                detalle = detalle,
                theme = PartialSheetTheme.Emit,
                iconTint = tint,
                iconBackground = soft,
                tituloColor = title,
                onClick = { onOpcion(opcion) },
            )
        }
    }
}

@Composable
fun GuiaRemisionEventosSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEvento: (GuiaRemisionEventoOpcion) -> Unit,
) {
    if (!visible) return
    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = "Eventos de guía",
        subtitle = "Comunicaciones a SUNAT sobre la GRE",
        theme = PartialSheetTheme.Emit,
    ) {
        GuiaRemisionEventoOpcion.entries.forEachIndexed { index, evento ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            val icon = when (evento) {
                GuiaRemisionEventoOpcion.REGISTRO_EVENTOS -> Icons.Default.Event
                GuiaRemisionEventoOpcion.COMUNICACION_BAJA -> Icons.Default.Cancel
            }
            PartialOptionCard(
                icon = icon,
                titulo = evento.titulo,
                detalle = evento.detalle,
                theme = PartialSheetTheme.Emit,
                iconTint = GuiaPurple,
                iconBackground = GuiaPurpleSoft,
                tituloColor = GuiaPurple,
                onClick = { onEvento(evento) },
            )
        }
    }
}
