package com.factapp.jhonny.ui.comprobantes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.ui.comprobantes.sunat.QrCodeBitmap
import com.factapp.jhonny.ui.comprobantes.sunat.SunatQrPayload
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun ComprobanteQrPreview(
    comprobante: Invoice,
    modifier: Modifier = Modifier,
) {
    val payload = remember(comprobante.id, comprobante.hashCpe, comprobante.totales.total) {
        SunatQrPayload.build(comprobante)
    }
    val bitmap = remember(payload) {
        if (payload.isBlank()) null else QrCodeBitmap.encode(payload, 360)
    }
    if (bitmap == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Verificación SUNAT",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = C.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Escanea el código para consultar la validez del comprobante",
                fontSize = 12.sp,
                color = C.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = C.surfaceSoft,
                border = BorderStroke(1.dp, C.border.copy(alpha = 0.3f)),
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Código QR del comprobante electrónico",
                    modifier = Modifier
                        .padding(12.dp)
                        .size(148.dp),
                )
            }
            comprobante.hashCpe?.takeIf { it.isNotBlank() }?.let { hash ->
                Spacer(Modifier.height(12.dp))
                Text(
                    "Hash CPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = C.textSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    hash,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = C.textPrimary.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "www.sunat.gob.pe · Consulta de validez del CPE",
                fontSize = 10.sp,
                color = C.accent,
                textAlign = TextAlign.Center,
            )
        }
    }
}
