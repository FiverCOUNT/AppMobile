package com.factapp.jhonny.ui.emitir

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun EmitirAlmacenOrigenCard(
    almacenes: List<Almacen>,
    seleccionadoId: String?,
    cargando: Boolean,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val seleccionado = almacenes.find { it.id == seleccionadoId }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(C.surfaceSoft)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = C.accentSoft) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = C.accent,
                        modifier = Modifier
                            .padding(9.dp)
                            .size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Almacén de salida",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = C.primary,
                    )
                    Text(
                        text = "Despacho y series desde esta bodega",
                        fontSize = 12.sp,
                        color = C.textSecondary,
                    )
                }
            }

            if (seleccionado != null && !cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(C.primaryDeep.copy(alpha = 0.95f), C.accent.copy(alpha = 0.9f)),
                            ),
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = seleccionado.nombre,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (seleccionado.codigo.isNotBlank()) {
                                Text(
                                    text = seleccionado.codigo,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when {
                    cargando -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = C.accent,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Cargando bodegas…", fontSize = 13.sp, color = C.textSecondary)
                        }
                    }
                    almacenes.isEmpty() -> {
                        Text(
                            text = "No hay almacenes configurados.",
                            fontSize = 13.sp,
                            color = C.textSecondary,
                        )
                    }
                    else -> {
                        if (almacenes.size > 1) {
                            Text(
                                text = "Cambiar bodega",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = C.textSecondary,
                            )
                        }
                        almacenes.forEach { alm ->
                            AlmacenOrigenOpcion(
                                almacen = alm,
                                selected = seleccionadoId == alm.id,
                                onClick = { onSeleccionar(alm.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlmacenOrigenOpcion(
    almacen: Almacen,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val fondo by animateColorAsState(
        if (selected) C.accentSoft else C.surfaceSoft,
        label = "alm_fondo",
    )
    val borde by animateColorAsState(
        if (selected) C.accent else C.border.copy(alpha = 0.45f),
        label = "alm_borde",
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = fondo,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selected) C.accent.copy(alpha = 0.14f) else Color.White,
            ) {
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = null,
                    tint = if (selected) C.accent else C.textSecondary,
                    modifier = Modifier
                        .padding(7.dp)
                        .size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = almacen.nombre,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = C.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (almacen.codigo.isNotBlank()) {
                    Text(
                        text = almacen.codigo,
                        fontSize = 11.sp,
                        color = C.textSecondary,
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = if (selected) C.accent else Color.Transparent,
                border = if (selected) null else BorderStroke(1.5.dp, C.border),
                modifier = Modifier.size(22.dp),
            ) {
                if (selected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(3.dp),
                    )
                }
            }
        }
    }
}
