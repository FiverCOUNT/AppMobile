package com.factapp.jhonny.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.lineaPrincipal
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.components.EmitFormSheetHeader
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

private fun List<Cliente>.filtrarClientes(query: String): List<Cliente> {
    if (query.isBlank()) return this
    val q = query.trim().lowercase()
    return filter { c ->
        c.razonSocial.lowercase().contains(q) ||
            c.numeroDoc.lowercase().contains(q) ||
            c.address.lineaPrincipal?.lowercase()?.contains(q) == true ||
            c.telefono?.lowercase()?.contains(q) == true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalidaClienteBuscarSheet(
    visible: Boolean,
    clientes: List<Cliente>,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onClienteSeleccionado: (Cliente) -> Unit,
    onNuevoCliente: (() -> Unit)? = null,
    soloPersonasNaturales: Boolean = false,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filtrados = clientes.filtrarClientes(busqueda)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EmitFormSheetHeader(
                titulo = "Cliente destino",
                subtitulo = when {
                    busqueda.isNotBlank() -> "${filtrados.size} de ${clientes.size} clientes"
                    clientes.isEmpty() -> "Sin clientes registrados"
                    else -> "${clientes.size} clientes · filtra o registra uno"
                },
                icono = Icons.Default.Business,
                mostrarDragHandle = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(C.background)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                Spacer(Modifier.height(10.dp))
                CatalogoBusquedaBar(
                    value = busqueda,
                    onValueChange = onBusquedaChange,
                    placeholder = if (soloPersonasNaturales) {
                        "Nombre, DNI, teléfono…"
                    } else {
                        "Nombre, DNI, RUC, teléfono…"
                    },
                )

                if (onNuevoCliente != null) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onNuevoCliente,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = C.accent)
                        Spacer(Modifier.width(8.dp))
                        Text("Nuevo cliente", fontSize = 14.sp, color = C.accent, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Lista de clientes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = C.primary,
                )
                Spacer(Modifier.height(8.dp))

                if (filtrados.isEmpty()) {
                    Text(
                        if (busqueda.isNotBlank()) "Sin resultados para tu búsqueda"
                        else "No hay clientes registrados. Usa «Nuevo cliente».",
                        color = C.textSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filtrados, key = { it.id }) { cliente ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onClienteSeleccionado(cliente)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = C.surface),
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Icon(
                                        imageVector = if (cliente.esPersonaNatural) {
                                            Icons.Default.Person
                                        } else {
                                            Icons.Default.Business
                                        },
                                        contentDescription = null,
                                        tint = C.accent,
                                    )
                                    Text(
                                        cliente.razonSocial,
                                        fontWeight = FontWeight.SemiBold,
                                        color = C.textPrimary,
                                        fontSize = 15.sp,
                                    )
                                    Text(
                                        cliente.etiquetaDocumento,
                                        fontSize = 13.sp,
                                        color = C.accent,
                                    )
                                    cliente.address.lineaPrincipal?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, fontSize = 12.sp, color = C.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
