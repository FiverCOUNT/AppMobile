package com.factapp.jhonny.ui.catalogo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.CatalogItemKind
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarCatalogItemSheet(
    visible: Boolean,
    companyRuc: String,
    onDismiss: () -> Unit,
    onGuardar: (CatalogItem) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var esProducto by remember { mutableStateOf(true) }
    var unidadSeleccionada by remember { mutableStateOf(CatalogItemKind.PRODUCT.unidadPorDefecto()) }

    val puedeGuardar = nombre.isNotBlank()

    LaunchedEffect(visible) {
        if (visible) {
            nombre = ""
            codigo = ""
            esProducto = true
            unidadSeleccionada = CatalogItemKind.PRODUCT.unidadPorDefecto()
        }
    }
    LaunchedEffect(esProducto) {
        val opciones = if (esProducto) UNIDADES_PRODUCTO else UNIDADES_SERVICIO
        if (unidadSeleccionada !in opciones.map { it.codigo }) {
            unidadSeleccionada = opciones.first().codigo
        }
    }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            ComprobanteEmitHeader(
                titulo = "Nuevo ítem",
                subtitulo = "Registra un producto o servicio en tu catálogo",
                icono = Icons.Default.Add,
                mostrarDragHandle = true,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                NuevoItemSeccionTitulo("Tipo de ítem")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TipoItemCard(
                        modifier = Modifier.weight(1f),
                        seleccionado = esProducto,
                        icono = Icons.Default.Inventory2,
                        titulo = "Producto",
                        detalle = "Inventario y salidas",
                        onClick = { esProducto = true },
                    )
                    TipoItemCard(
                        modifier = Modifier.weight(1f),
                        seleccionado = !esProducto,
                        icono = Icons.Default.RoomService,
                        titulo = "Servicio",
                        detalle = "Sin control de stock",
                        onClick = { esProducto = false },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = C.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, C.border.copy(alpha = 0.6f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                    ) {
                        NuevoItemSeccionTitulo("Información básica")
                        Spacer(modifier = Modifier.height(14.dp))
                        NuevoItemCampo(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = "Nombre del ítem",
                            placeholder = "Cemento Pacasmayo 40kg",
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NuevoItemCampo(
                            value = codigo,
                            onValueChange = { codigo = it },
                            label = "Código interno",
                            placeholder = "Opcional SN2025",
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NuevoItemSeccionTitulo("Unidad de medida")
                        Spacer(modifier = Modifier.height(8.dp))
                        UnidadItemSelector(
                            unidades = if (esProducto) UNIDADES_PRODUCTO else UNIDADES_SERVICIO,
                            seleccionada = unidadSeleccionada,
                            onSeleccionar = { unidadSeleccionada = it },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                NuevoItemNotaInformativa(
                    texto = if (esProducto) {
                        "El stock se actualizará al registrar salidas de mercadería."
                    } else {
                        "Los servicios no requieren inventario ni salidas de stock."
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, C.border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = C.textPrimary,
                        ),
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(
                        onClick = {
                            val kind = if (esProducto) CatalogItemKind.PRODUCT else CatalogItemKind.SERVICE
                            val nuevo = CatalogItem(
                                id = "local-${System.currentTimeMillis()}",
                                companyRuc = companyRuc,
                                kind = kind.name,
                                codigo = codigo.takeIf { it.isNotBlank() },
                                nombre = nombre.trim(),
                                unidad = unidadSeleccionada,
                                precioUnitario = 0.0,
                                activo = true,
                                manejaStock = esProducto,
                                stockActual = null,
                                duracionMinutos = if (!esProducto) 60 else null,
                            )
                            onGuardar(nuevo)
                            onDismiss()
                        },
                        enabled = puedeGuardar,
                        modifier = Modifier
                            .weight(1.35f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = C.accent,
                            contentColor = C.onPrimary,
                            disabledContainerColor = C.accent.copy(alpha = 0.4f),
                            disabledContentColor = C.onPrimary.copy(alpha = 0.7f),
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

private data class UnidadUi(
    val codigo: String,
    val etiqueta: String,
)

private val UNIDADES_PRODUCTO = listOf(
    UnidadUi("NIU", "Unidad"),
    UnidadUi("MTR", "Metro"),
    UnidadUi("KGM", "Kilogramo"),
    UnidadUi("LTR", "Litro"),
)

private val UNIDADES_SERVICIO = listOf(
    UnidadUi("ZZ", "Servicio"),
)

@Composable
private fun UnidadItemSelector(
    unidades: List<UnidadUi>,
    seleccionada: String,
    onSeleccionar: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        unidades.forEach { unidad ->
            FilterChip(
                selected = seleccionada == unidad.codigo,
                onClick = { onSeleccionar(unidad.codigo) },
                label = { Text(unidad.etiqueta, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = C.accentSoft,
                    selectedLabelColor = C.accent,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = seleccionada == unidad.codigo,
                    borderColor = C.border,
                    selectedBorderColor = C.accent.copy(alpha = 0.35f),
                ),
            )
        }
    }
}

@Composable
private fun NuevoItemSeccionTitulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = C.textPrimary,
            letterSpacing = 0.3.sp,
        ),
    )
}

@Composable
private fun TipoItemCard(
    seleccionado: Boolean,
    icono: ImageVector,
    titulo: String,
    detalle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borde = if (seleccionado) C.accent else C.border.copy(alpha = 0.7f)
    val fondo = if (seleccionado) C.accentSoft else C.surface

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = borde,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (seleccionado) 3.dp else 0.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (seleccionado) C.accent.copy(alpha = 0.15f) else C.surfaceSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = titulo,
                        tint = if (seleccionado) C.accent else C.textSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                color = if (seleccionado) C.primary else C.textPrimary,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detalle,
                fontSize = 11.sp,
                color = C.textSecondary,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun NuevoItemCampo(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

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
        label = { Text(label, color = C.textSecondary) },
        placeholder = {
            Text(
                text = placeholder,
                color = C.textSecondary.copy(alpha = 0.65f),
                fontSize = 14.sp,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = C.borderFocused,
            unfocusedBorderColor = C.border,
            focusedLabelColor = C.accent,
            unfocusedLabelColor = C.textSecondary,
            cursorColor = C.accent,
            focusedTextColor = C.textPrimary,
            unfocusedTextColor = C.textPrimary,
            focusedContainerColor = C.surfaceSoft,
            unfocusedContainerColor = C.surfaceSoft,
        ),
    )
}

@Composable
private fun NuevoItemNotaInformativa(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(C.surfaceSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = C.accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = texto,
            fontSize = 13.sp,
            color = C.textSecondary,
            lineHeight = 18.sp,
        )
    }
}
