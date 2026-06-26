package com.factapp.jhonny.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.textoUnaLinea
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

data class AddressFormState(
    val ubigeo: String = "",
    val departamento: String = "",
    val provincia: String = "",
    val distrito: String = "",
    val urbanizacion: String = "",
    val direccion: String = "",
    val codLocal: String = Address.COD_LOCAL_PRINCIPAL,
) {
    fun toAddress(): Address? {
        val addr = Address(
            ubigeo = ubigeo.trim().takeIf { it.isNotBlank() },
            departamento = departamento.trim().takeIf { it.isNotBlank() },
            provincia = provincia.trim().takeIf { it.isNotBlank() },
            distrito = distrito.trim().takeIf { it.isNotBlank() },
            urbanizacion = urbanizacion.trim().takeIf { it.isNotBlank() },
            direccion = direccion.trim().takeIf { it.isNotBlank() },
            codLocal = codLocal.trim().ifBlank { Address.COD_LOCAL_PRINCIPAL },
        )
        return addr.takeIf { it.tieneDatos() }
    }

    companion object {
        fun fromAddress(address: Address?): AddressFormState = AddressFormState(
            ubigeo = address?.ubigeo.orEmpty(),
            departamento = address?.departamento.orEmpty(),
            provincia = address?.provincia.orEmpty(),
            distrito = address?.distrito.orEmpty(),
            urbanizacion = address?.urbanizacion.orEmpty(),
            direccion = address?.direccion.orEmpty(),
            codLocal = address?.codLocal ?: Address.COD_LOCAL_PRINCIPAL,
        )
    }
}

@Composable
fun AddressFormFields(
    state: AddressFormState,
    onStateChange: (AddressFormState) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelColor: Color = ComprobanteEmitColors.textPrimary,
    borderColor: Color = ComprobanteEmitColors.border,
    accentColor: Color = ComprobanteEmitColors.accent,
    surfaceColor: Color = Color.White,
    mostrarCodLocal: Boolean = false,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentColor,
        unfocusedBorderColor = borderColor.copy(alpha = 0.6f),
        focusedLabelColor = accentColor,
        cursorColor = accentColor,
        focusedTextColor = labelColor,
        unfocusedTextColor = labelColor,
        disabledTextColor = labelColor.copy(alpha = 0.6f),
        focusedContainerColor = surfaceColor,
        unfocusedContainerColor = surfaceColor,
        disabledContainerColor = surfaceColor.copy(alpha = 0.7f),
    )
    val shape = RoundedCornerShape(12.dp)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = state.ubigeo,
            onValueChange = { onStateChange(state.copy(ubigeo = it.filter { c -> c.isDigit() }.take(6))) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text("Ubigeo") },
            placeholder = { Text("6 dígitos · ej. 150101") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = shape,
            colors = fieldColors,
        )
        OutlinedTextField(
            value = state.departamento,
            onValueChange = { onStateChange(state.copy(departamento = it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text("Departamento") },
            placeholder = { Text("Ej. LIMA") },
            singleLine = true,
            shape = shape,
            colors = fieldColors,
        )
        OutlinedTextField(
            value = state.provincia,
            onValueChange = { onStateChange(state.copy(provincia = it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text("Provincia") },
            placeholder = { Text("Ej. LIMA") },
            singleLine = true,
            shape = shape,
            colors = fieldColors,
        )
        OutlinedTextField(
            value = state.distrito,
            onValueChange = { onStateChange(state.copy(distrito = it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text("Distrito") },
            placeholder = { Text("Ej. MIRAFLORES") },
            singleLine = true,
            shape = shape,
            colors = fieldColors,
        )
        OutlinedTextField(
            value = state.urbanizacion,
            onValueChange = { onStateChange(state.copy(urbanizacion = it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text("Urbanización") },
            placeholder = { Text("Opcional") },
            singleLine = true,
            shape = shape,
            colors = fieldColors,
        )
        OutlinedTextField(
            value = state.direccion,
            onValueChange = { onStateChange(state.copy(direccion = it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text("Dirección") },
            placeholder = { Text("Calle, número, referencia") },
            minLines = 2,
            shape = shape,
            colors = fieldColors,
        )
        if (mostrarCodLocal) {
            OutlinedTextField(
                value = state.codLocal,
                onValueChange = { onStateChange(state.copy(codLocal = it.filter { c -> c.isDigit() }.take(4))) },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                label = { Text("Cód. local") },
                placeholder = { Text("0000 = principal") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = shape,
                colors = fieldColors,
            )
        }
    }
}

@Composable
fun AddressResumenCard(
    address: Address?,
    modifier: Modifier = Modifier,
    accentColor: Color = ComprobanteEmitColors.accent,
    textColor: Color = ComprobanteEmitColors.textSecondary,
) {
    if (address == null || !address.tieneDatos()) {
        Text(
            text = "Sin dirección registrada.",
            fontSize = 13.sp,
            color = textColor,
            modifier = modifier,
        )
        return
    }
    Column(modifier = modifier) {
        address.textoUnaLinea()?.let { ubicacion ->
            Text(
                text = ubicacion,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ComprobanteEmitColors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
        }
        address.direccion?.takeIf { it.isNotBlank() }?.let { dir ->
            Text(text = dir, fontSize = 13.sp, color = textColor, lineHeight = 18.sp)
        }
        address.ubigeo?.takeIf { it.isNotBlank() }?.let { ubi ->
            Spacer(Modifier.height(4.dp))
            Text(text = "Ubigeo: $ubi", fontSize = 12.sp, color = accentColor)
        }
    }
}
