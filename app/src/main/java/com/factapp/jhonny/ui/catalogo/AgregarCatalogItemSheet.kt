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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.unidadPermiteSerie
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
    itemExistente: CatalogItem? = null,
    onDismiss: () -> Unit,
    onGuardar: (CatalogItem) -> Unit,
) {
    if (!visible) return

    val esEdicion = itemExistente != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nombre by remember { mutableStateOf("") }
    var precioTexto by remember { mutableStateOf("") }
    var esProducto by remember { mutableStateOf(true) }
    var manejaSerie by remember { mutableStateOf(false) }
    var unidadSeleccionada by remember { mutableStateOf(CatalogItemKind.PRODUCT.unidadPorDefecto()) }

    val precioUnitario = parsePrecioTexto(precioTexto)
    val puedeGuardar = nombre.isNotBlank() && precioTexto.isNotBlank() && precioUnitario > 0.0

    LaunchedEffect(visible, itemExistente?.id) {
        if (!visible) return@LaunchedEffect
        if (itemExistente != null) {
            nombre = itemExistente.nombre
            precioTexto = formatPrecioParaCampo(itemExistente.precioUnitario)
            esProducto = itemExistente.esProducto
            manejaSerie = itemExistente.esProducto && itemExistente.manejaSerie
            unidadSeleccionada = itemExistente.unidad
        } else {
            nombre = ""
            precioTexto = ""
            esProducto = true
            manejaSerie = false
            unidadSeleccionada = CatalogItemKind.PRODUCT.unidadPorDefecto()
        }
    }
    LaunchedEffect(esProducto) {
        if (!esProducto) manejaSerie = false
        val opciones = if (esProducto) UNIDADES_PRODUCTO else UNIDADES_SERVICIO
        if (unidadSeleccionada !in opciones.map { it.codigo }) {
            unidadSeleccionada = opciones.first().codigo
        }
    }
    LaunchedEffect(unidadSeleccionada) {
        if (!unidadPermiteSerie(unidadSeleccionada)) {
            manejaSerie = false
        }
    }

    val puedeUsarSerie = esProducto && unidadPermiteSerie(unidadSeleccionada)

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
                titulo = if (esEdicion) "Editar ítem" else "Nuevo ítem",
                subtitulo = if (esEdicion) {
                    "Actualiza los datos del producto o servicio"
                } else {
                    "Registra un producto o servicio en tu catálogo"
                },
                icono = if (esEdicion) Icons.Default.Edit else Icons.Default.Add,
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
                            value = precioTexto,
                            onValueChange = { precioTexto = it.filterPrecioDecimal() },
                            label = "Precio unitario",
                            placeholder = "0.00",
                            prefix = { Text("S/ ", color = C.accent, fontWeight = FontWeight.SemiBold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NuevoItemSeccionTitulo("Unidad de medida")
                        Spacer(modifier = Modifier.height(8.dp))
                        UnidadItemSelector(
                            unidades = if (esProducto) UNIDADES_PRODUCTO else UNIDADES_SERVICIO,
                            seleccionada = unidadSeleccionada,
                            onSeleccionar = { unidadSeleccionada = it },
                        )
                        if (puedeUsarSerie) {
                            Spacer(modifier = Modifier.height(14.dp))
                            NuevoItemSeccionTitulo("Número de serie")
                            Spacer(modifier = Modifier.height(8.dp))
                            ManejaSerieSelector(
                                conSerie = manejaSerie,
                                onSeleccionar = { manejaSerie = it },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                NuevoItemNotaInformativa(
                    texto = when {
                        !esProducto -> "Los servicios no requieren inventario ni salidas de stock."
                        manejaSerie -> "Cada unidad tendrá un número de serie. El stock será la cantidad de series disponibles."
                        !puedeUsarSerie -> "Metro, kilo y litro se controlan por cantidad; no usan número de serie."
                        else -> "El stock se actualizará al registrar salidas de mercadería."
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
                            val precio = parsePrecioTexto(precioTexto)
                            val usaSerie = esProducto && manejaSerie && unidadPermiteSerie(unidadSeleccionada)
                            val item = if (itemExistente != null) {
                                itemExistente.copy(
                                    kind = kind.name,
                                    nombre = nombre.trim(),
                                    precioUnitario = precio,
                                    unidad = unidadSeleccionada,
                                    manejaStock = esProducto,
                                    manejaSerie = usaSerie,
                                    stockActual = if (esProducto) itemExistente.stockActual else null,
                                    duracionMinutos = if (!esProducto) {
                                        itemExistente.duracionMinutos ?: 60
                                    } else {
                                        null
                                    },
                                )
                            } else {
                                CatalogItem(
                                    id = "local-${System.currentTimeMillis()}",
                                    companyRuc = companyRuc,
                                    kind = kind.name,
                                    nombre = nombre.trim(),
                                    unidad = unidadSeleccionada,
                                    precioUnitario = precio,
                                    activo = true,
                                    manejaStock = esProducto,
                                    manejaSerie = usaSerie,
                                    stockActual = null,
                                    duracionMinutos = if (!esProducto) 60 else null,
                                )
                            }
                            onGuardar(item)
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
                            imageVector = if (esEdicion) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (esEdicion) "Actualizar" else "Guardar",
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
    UnidadUi("KGM", "Kg"),
    UnidadUi("LTR", "Litro"),
)

private val UNIDADES_SERVICIO = listOf(
    UnidadUi("ZZ", "Servicio"),
)

@Composable
private fun ManejaSerieSelector(
    conSerie: Boolean,
    onSeleccionar: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = !conSerie,
            onClick = { onSeleccionar(false) },
            label = {
                Text(
                    text = "Sin serie",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = C.accentSoft,
                selectedLabelColor = C.accent,
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = !conSerie,
                borderColor = C.border,
                selectedBorderColor = C.accent.copy(alpha = 0.35f),
            ),
        )
        FilterChip(
            selected = conSerie,
            onClick = { onSeleccionar(true) },
            label = {
                Text(
                    text = "Con serie",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = C.accentSoft,
                selectedLabelColor = C.accent,
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = conSerie,
                borderColor = C.border,
                selectedBorderColor = C.accent.copy(alpha = 0.35f),
            ),
        )
    }
}

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

private fun String.filterPrecioDecimal(): String {
    val cleaned = replace(',', '.')
    val filtered = buildString {
        var dotUsed = false
        for (ch in cleaned) {
            when {
                ch.isDigit() -> append(ch)
                ch == '.' && !dotUsed -> {
                    append(ch)
                    dotUsed = true
                }
            }
        }
    }
    val parts = filtered.split('.')
    if (parts.size == 2 && parts[1].length > 2) {
        return "${parts[0]}.${parts[1].take(2)}"
    }
    return filtered.take(12)
}

private fun parsePrecioTexto(texto: String): Double =
    texto.trim().replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

private fun formatPrecioParaCampo(precio: Double): String = when {
    precio <= 0.0 -> ""
    precio == precio.toLong().toDouble() -> precio.toLong().toString()
    else -> "%.2f".format(precio).trimEnd('0').trimEnd('.')
}

@Composable
private fun NuevoItemCampo(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    prefix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
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
        prefix = prefix,
        keyboardOptions = keyboardOptions,
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
