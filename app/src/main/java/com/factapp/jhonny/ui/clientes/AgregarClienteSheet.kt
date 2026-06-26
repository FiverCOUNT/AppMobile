package com.factapp.jhonny.ui.clientes

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.request.CrearClienteRequest
import com.factapp.jhonny.network.dto.model.dniValido
import com.factapp.jhonny.ui.components.AddressFormFields
import com.factapp.jhonny.ui.components.AddressFormState
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object NuevoClienteTheme {
    val fondo = Color(0xFFEEF4FB)
    val surface = Color(0xFFF7FAFE)
    val ink = Color(0xFF1A3654)
    val inkMuted = Color(0xFF516B82)
    val azul = Color(0xFF3D7EC4)
    val azulDeep = Color(0xFF1A4570)
    val azulSoft = Color(0xFFD4E5F5)
    val borde = Color(0xFFACC4DB)
    val error = Color(0xFFC62828)
    val errorSoft = Color(0xFFFFEBEE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarClienteSheet(
    visible: Boolean,
    guardando: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onGuardar: (CrearClienteRequest) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var dni by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccionState by remember { mutableStateOf(AddressFormState()) }

    val focusDni = remember { FocusRequester() }
    val focusNombre = remember { FocusRequester() }
    val focusTelefono = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(visible) {
        if (visible) {
            dni = ""
            nombre = ""
            telefono = ""
            direccionState = AddressFormState()
            delay(350)
            focusDni.requestFocus()
        }
    }

    val dniLimpio = dni.filter { it.isDigit() }
    val dniCompleto = dniValido(dniLimpio)
    val puedeGuardar = !guardando && dniCompleto && nombre.isNotBlank()
    val scrollState = rememberScrollState()

    fun guardarCliente() {
        if (!puedeGuardar) return
        onGuardar(
            CrearClienteRequest(
                numeroDoc = dniLimpio,
                razonSocial = nombre.trim(),
                telefono = telefono.trim().takeIf { it.isNotBlank() },
                address = direccionState.toAddress(),
            ),
        )
    }

    LaunchedEffect(dniCompleto) {
        if (dniCompleto) focusNombre.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NuevoClienteTheme.fondo,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(NuevoClienteTheme.borde),
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
                        enabled = !guardando,
                        modifier = Modifier
                            .size(40.dp)
                            .background(NuevoClienteTheme.surface, CircleShape),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NuevoClienteTheme.inkMuted,
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
                                listOf(NuevoClienteTheme.azulDeep, NuevoClienteTheme.azul),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Nuevo cliente",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = NuevoClienteTheme.ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Persona natural con DNI · para boletas y salidas",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 14.sp,
                    color = NuevoClienteTheme.inkMuted,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(22.dp))

                InfoRucBanner()

                Spacer(Modifier.height(22.dp))

                NuevoClienteCampo(
                    icon = Icons.Default.Badge,
                    label = "DNI",
                    placeholder = "8 dígitos",
                    value = dni,
                    onValueChange = { dni = it.filter { c -> c.isDigit() }.take(8) },
                    focusRequester = focusDni,
                    imeAction = ImeAction.Next,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(
                        onNext = { focusNombre.requestFocus() },
                    ),
                    trailing = {
                        Text(
                            text = "${dniLimpio.length}/8",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dniCompleto) NuevoClienteTheme.azul else NuevoClienteTheme.inkMuted,
                        )
                    },
                )
                if (dniLimpio.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { dniLimpio.length / 8f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (dniCompleto) NuevoClienteTheme.azul else NuevoClienteTheme.borde,
                        trackColor = NuevoClienteTheme.azulSoft,
                    )
                }

                Spacer(Modifier.height(16.dp))

                NuevoClienteCampo(
                    icon = Icons.Default.Person,
                    label = "Nombres y apellidos",
                    placeholder = "Como figura en el documento",
                    value = nombre,
                    onValueChange = { nombre = it },
                    focusRequester = focusNombre,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusTelefono.requestFocus() },
                    ),
                )

                Spacer(Modifier.height(16.dp))

                NuevoClienteCampo(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    placeholder = "Ej. 987 654 321 (opcional)",
                    value = telefono,
                    onValueChange = { telefono = it },
                    focusRequester = focusTelefono,
                    imeAction = ImeAction.Done,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                    ),
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NuevoClienteTheme.surface)
                        .border(1.dp, NuevoClienteTheme.borde.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = NuevoClienteTheme.azulDeep,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Dirección",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = NuevoClienteTheme.ink,
                            )
                            Text(
                                text = "Opcional · ubigeo, departamento, provincia, distrito y calle",
                                fontSize = 11.sp,
                                color = NuevoClienteTheme.inkMuted,
                                lineHeight = 15.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    AddressFormFields(
                        state = direccionState,
                        onStateChange = { direccionState = it },
                        labelColor = NuevoClienteTheme.ink,
                        borderColor = NuevoClienteTheme.borde,
                        accentColor = NuevoClienteTheme.azul,
                        surfaceColor = Color.White,
                    )
                }

                error?.let { msg ->
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = NuevoClienteTheme.errorSoft,
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = NuevoClienteTheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(msg, color = NuevoClienteTheme.error, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Surface(
                    onClick = { guardarCliente() },
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
                                        listOf(NuevoClienteTheme.azulDeep, NuevoClienteTheme.azul),
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(NuevoClienteTheme.borde, NuevoClienteTheme.borde),
                                    )
                                },
                                shape = RoundedCornerShape(18.dp),
                            )
                            .padding(vertical = 17.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Registrar cliente",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = if (puedeGuardar) Color.White else NuevoClienteTheme.inkMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRucBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NuevoClienteTheme.azulSoft,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = NuevoClienteTheme.azulDeep,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "¿Cliente con RUC?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NuevoClienteTheme.ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "No hace falta registrarlo aquí. Al emitir una factura, el sistema lo crea automáticamente.",
                    fontSize = 12.sp,
                    color = NuevoClienteTheme.inkMuted,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun NuevoClienteCampo(
    icon: ImageVector,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: @Composable (() -> Unit)? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NuevoClienteTheme.surface)
            .border(1.dp, NuevoClienteTheme.borde.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NuevoClienteTheme.azulDeep,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = NuevoClienteTheme.ink,
                )
            }
            trailing?.invoke()
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusEvent { focus ->
                    if (focus.isFocused) {
                        scope.launch { bringIntoViewRequester.bringIntoView() }
                    }
                },
            placeholder = {
                Text(
                    text = placeholder,
                    color = NuevoClienteTheme.inkMuted.copy(alpha = 0.65f),
                    fontSize = 15.sp,
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions.copy(imeAction = imeAction),
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NuevoClienteTheme.azul,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = NuevoClienteTheme.azulDeep,
                focusedTextColor = NuevoClienteTheme.ink,
                unfocusedTextColor = NuevoClienteTheme.ink,
            ),
        )
    }
}
