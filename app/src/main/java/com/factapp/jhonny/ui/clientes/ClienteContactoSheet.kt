package com.factapp.jhonny.ui.clientes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

private data class ContactoEstilo(
    val icono: ImageVector,
    val tint: Color,
    val fondoIcono: Color,
)

private val EstiloLlamar = ContactoEstilo(
    icono = Icons.Outlined.Call,
    tint = Color(0xFF2D6A9F),
    fondoIcono = Color(0xFFE8F1F8),
)
private val EstiloWhatsApp = ContactoEstilo(
    icono = Icons.AutoMirrored.Outlined.Chat,
    tint = Color(0xFF1A7A5E),
    fondoIcono = Color(0xFFE6F4EE),
)
private val EstiloCompartir = ContactoEstilo(
    icono = Icons.Outlined.IosShare,
    tint = Color(0xFF4A5F7A),
    fondoIcono = Color(0xFFEEF2F6),
)

@Composable
fun ClienteContactoSheet(
    cliente: Cliente?,
    onDismiss: () -> Unit,
) {
    if (cliente == null) return

    val context = LocalContext.current
    val telefono = cliente.telefono?.trim().orEmpty()
    val tieneTelefono = telefono.isNotBlank()

    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = "Contactar",
        subtitle = cliente.razonSocial,
        theme = PartialSheetTheme.Emit,
    ) {
        if (!tieneTelefono) {
            Text(
                text = "Sin teléfono registrado",
                fontSize = 12.sp,
                color = C.textSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = C.surface,
            shadowElevation = 1.dp,
            tonalElevation = 0.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ContactoOpcionFila(
                    estilo = EstiloLlamar,
                    titulo = "Llamar",
                    detalle = telefono.takeIf { tieneTelefono },
                    habilitado = tieneTelefono,
                    mostrarDivisor = true,
                    onClick = {
                        if (ClienteContactIntents.llamar(context, telefono)) onDismiss()
                    },
                )
                ContactoOpcionFila(
                    estilo = EstiloWhatsApp,
                    titulo = "WhatsApp",
                    detalle = if (tieneTelefono) "Mensaje directo" else null,
                    habilitado = tieneTelefono,
                    mostrarDivisor = true,
                    onClick = {
                        if (ClienteContactIntents.abrirWhatsApp(context, telefono)) onDismiss()
                    },
                )
                ContactoOpcionFila(
                    estilo = EstiloCompartir,
                    titulo = "Compartir",
                    detalle = "Enviar ficha del cliente",
                    habilitado = true,
                    mostrarDivisor = false,
                    onClick = {
                        if (ClienteContactIntents.compartirCliente(context, cliente)) onDismiss()
                    },
                )
            }
        }

        if (tieneTelefono) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = C.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = telefono,
                    fontSize = 12.sp,
                    color = C.textSecondary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ContactoOpcionFila(
    estilo: ContactoEstilo,
    titulo: String,
    detalle: String?,
    habilitado: Boolean,
    mostrarDivisor: Boolean,
    onClick: () -> Unit,
) {
    val alpha = if (habilitado) 1f else 0.38f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = habilitado, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = estilo.fondoIcono,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = estilo.icono,
                        contentDescription = titulo,
                        tint = estilo.tint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = C.textPrimary,
                )
                detalle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = C.textSecondary,
                        lineHeight = 16.sp,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = C.textSecondary.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp),
            )
        }
        if (mostrarDivisor) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp, end = 14.dp),
                thickness = 0.5.dp,
                color = C.border.copy(alpha = 0.35f),
            )
        }
    }
}
