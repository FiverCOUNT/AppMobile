package com.factapp.jhonny.ui.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.MovimientoTipo
import com.factapp.jhonny.network.dto.model.detalleHistorial
import com.factapp.jhonny.network.dto.model.etiquetaTipoHistorial
import com.factapp.jhonny.network.dto.model.fechaHoraCompacto
import com.factapp.jhonny.network.dto.model.resumenProductos
import com.factapp.jhonny.network.dto.model.tituloHistorial
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.inventario.HistorialMovimientoDetalleSheet
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

@Composable
fun ClienteDetalleScreen(
    cliente: Cliente,
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
    onNuevaEntrega: (Cliente) -> Unit = {},
    onAbrirComprobanteVenta: (String) -> Unit = {},
) {
    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token

    var movimientos by remember { mutableStateOf<List<Movimiento>>(emptyList()) }
    var catalogoLista by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var almacenesMap by remember { mutableStateOf<Map<String, Almacen>>(emptyMap()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var movimientoDetalle by remember { mutableStateOf<Movimiento?>(null) }
    var mostrarContacto by remember { mutableStateOf(false) }

    val catalogoMap = remember(catalogoLista) { catalogoLista.associateBy { it.id } }

    BackHandler {
        when {
            movimientoDetalle != null -> movimientoDetalle = null
            mostrarContacto -> mostrarContacto = false
            else -> onVolver()
        }
    }

    LaunchedEffect(companyRuc, token, cliente.id) {
        if (companyRuc.isBlank()) {
            cargando = false
            error = "Sin empresa vinculada"
            return@LaunchedEffect
        }
        cargando = true
        error = null
        withContext(Dispatchers.IO) {
            val movResult = InventarioRepository.listarMovimientosCliente(companyRuc, token, cliente.id)
            val catResult = CatalogRepository.listarParaGestion(companyRuc, token)
            val almResult = InventarioRepository.listarAlmacenes(companyRuc, token, todos = true)
            movResult.onSuccess { movimientos = it }
                .onFailure { error = it.message ?: "No se pudo cargar el historial" }
            catResult.onSuccess { catalogoLista = it }
            almResult.onSuccess { almacenesMap = it.associateBy { a -> a.id } }
        }
        cargando = false
    }

    val entregas = remember(movimientos) {
        movimientos.count { it.tipo == MovimientoTipo.SALIDA && it.almacenDestinoId == null }
    }
    val devoluciones = remember(movimientos) {
        movimientos.count { it.tipo == MovimientoTipo.ENTRADA && it.referenciaTipo == "DEVOLUCION_CLIENTE" }
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = cliente.razonSocial,
        subtitulo = cliente.etiquetaDocumento,
        icono = Icons.Default.People,
        onVolver = {
            when {
                movimientoDetalle != null -> movimientoDetalle = null
                mostrarContacto -> mostrarContacto = false
                else -> onVolver()
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                cargando -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = C.accent)
                }
                error != null -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error!!, color = C.textSecondary)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = "$entregas entregas · $devoluciones devoluciones",
                                    fontSize = 13.sp,
                                    color = C.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { onNuevaEntrega(cliente) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = C.accent,
                                        contentColor = C.onPrimary,
                                    ),
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.size(8.dp))
                                    Text(
                                        "Nueva entrega",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                if (!cliente.telefono.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { mostrarContacto = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = C.accent)
                                        Spacer(Modifier.size(8.dp))
                                        Text("Contactar", color = C.accent, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Entregas y devoluciones registradas para este cliente.",
                                    fontSize = 12.sp,
                                    color = C.textSecondary,
                                    lineHeight = 16.sp,
                                )
                            }
                        }
                        if (movimientos.isEmpty()) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Sin entregas ni devoluciones registradas",
                                        color = C.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else {
                            items(movimientos, key = { it.id }) { mov ->
                                ClienteMovimientoCard(
                                    movimiento = mov,
                                    catalogo = catalogoMap,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { movimientoDetalle = mov },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    HistorialMovimientoDetalleSheet(
        movimiento = movimientoDetalle,
        catalogo = catalogoMap,
        almacenes = almacenesMap,
        onDismiss = { movimientoDetalle = null },
        onAbrirComprobanteVenta = onAbrirComprobanteVenta,
    )

    ClienteContactoSheet(
        cliente = if (mostrarContacto) cliente else null,
        onDismiss = { mostrarContacto = false },
    )
}

@Composable
private fun ClienteMovimientoCard(
    movimiento: Movimiento,
    catalogo: Map<String, CatalogItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val esDevolucion = movimiento.tipo == MovimientoTipo.ENTRADA &&
        movimiento.referenciaTipo == "DEVOLUCION_CLIENTE"
    val (icono, color) = if (esDevolucion) {
        Icons.Default.Input to Color(0xFF2E7D32)
    } else {
        Icons.Default.LocalShipping to Color(0xFF1565C0)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(Modifier.size(40.dp), shape = CircleShape, color = color.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    movimiento.tituloHistorial(),
                    fontWeight = FontWeight.Bold,
                    color = C.textPrimary,
                    fontSize = 15.sp,
                )
                Text(
                    movimiento.resumenProductos(catalogo),
                    fontSize = 14.sp,
                    color = C.textPrimary,
                )
                Text(
                    movimiento.detalleHistorial(catalogo),
                    fontSize = 13.sp,
                    color = C.textSecondary,
                    lineHeight = 18.sp,
                    maxLines = 2,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${movimiento.etiquetaTipoHistorial()} · ${movimiento.fechaHoraCompacto()}",
                    fontSize = 11.sp,
                    color = C.accent,
                    fontWeight = FontWeight.Medium,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalle",
                tint = C.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
