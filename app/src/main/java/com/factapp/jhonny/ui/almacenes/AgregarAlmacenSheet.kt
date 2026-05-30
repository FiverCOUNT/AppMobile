package com.factapp.jhonny.ui.almacenes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.request.CrearAlmacenRequest
import com.factapp.jhonny.network.dto.codigoDesdeNombreAlmacen
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import kotlinx.coroutines.launch

private object NuevoAlmacenTheme {
    val cream = Color(0xFFFFF8F0)
    val creamSoft = Color(0xFFF5EBE0)
    val ink = Color(0xFF1E1812)
    val inkMuted = Color(0xFF6B5E52)
    val amber = Color(0xFFE8A54B)
    val amberDeep = Color(0xFFC45C26)
    val fieldBorder = Color(0xFFE8DDD0)
    val fieldFocus = Color(0xFFC45C26)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarAlmacenSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (CrearAlmacenRequest) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    val puedeGuardar = nombre.isNotBlank()

    LaunchedEffect(visible) {
        if (visible) {
            nombre = ""
            direccion = ""
        }
    }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NuevoAlmacenTheme.cream,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(NuevoAlmacenTheme.fieldBorder),
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Spacer(modifier = Modifier.size(40.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(NuevoAlmacenTheme.creamSoft, CircleShape),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NuevoAlmacenTheme.inkMuted,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(NuevoAlmacenTheme.amberDeep, NuevoAlmacenTheme.amber),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Nueva bodega",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = NuevoAlmacenTheme.ink,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Solo necesitas un nombre. El código interno se genera solo.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 14.sp,
                    color = NuevoAlmacenTheme.inkMuted,
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(28.dp))

                NuevoAlmacenCampo(
                    icon = Icons.Default.Edit,
                    label = "Nombre de la bodega",
                    placeholder = "Ej. Almacén central, Sede norte…",
                    value = nombre,
                    onValueChange = { nombre = it },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                NuevoAlmacenCampo(
                    icon = Icons.Default.LocationOn,
                    label = "Dirección",
                    placeholder = "Calle, distrito o referencia (opcional)",
                    value = direccion,
                    onValueChange = { direccion = it },
                    singleLine = false,
                    minLines = 2,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Surface(
                    onClick = {
                        if (!puedeGuardar) return@Surface
                        onGuardar(
                            CrearAlmacenRequest(
                                codigo = codigoDesdeNombreAlmacen(nombre),
                                nombre = nombre.trim(),
                                address = direccion.trim().takeIf { it.isNotEmpty() }?.let { Address.linea(it) },
                            ),
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                    enabled = puedeGuardar,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = if (puedeGuardar) {
                                    Brush.horizontalGradient(
                                        listOf(NuevoAlmacenTheme.amberDeep, NuevoAlmacenTheme.amber),
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            NuevoAlmacenTheme.fieldBorder,
                                            NuevoAlmacenTheme.fieldBorder,
                                        ),
                                    )
                                },
                                shape = RoundedCornerShape(18.dp),
                            )
                            .padding(vertical = 17.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Crear bodega",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (puedeGuardar) Color.White else NuevoAlmacenTheme.inkMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NuevoAlmacenCampo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    minLines: Int = 1,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NuevoAlmacenTheme.creamSoft)
            .border(1.dp, NuevoAlmacenTheme.fieldBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NuevoAlmacenTheme.amberDeep,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = NuevoAlmacenTheme.ink,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusEvent { focus ->
                    if (focus.isFocused) {
                        scope.launch { bringIntoViewRequester.bringIntoView() }
                    }
                },
            placeholder = {
                Text(
                    text = placeholder,
                    color = NuevoAlmacenTheme.inkMuted.copy(alpha = 0.65f),
                    fontSize = 15.sp,
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NuevoAlmacenTheme.fieldFocus,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = NuevoAlmacenTheme.amberDeep,
                focusedTextColor = NuevoAlmacenTheme.ink,
                unfocusedTextColor = NuevoAlmacenTheme.ink,
            ),
        )
    }
}
