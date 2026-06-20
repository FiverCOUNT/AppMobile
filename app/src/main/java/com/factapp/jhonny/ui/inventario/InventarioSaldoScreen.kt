package com.factapp.jhonny.ui.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.modelos.esAdmin
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.InventarioSaldo
import com.factapp.jhonny.network.dto.model.TipoInventarioSaldo
import com.factapp.jhonny.network.dto.model.etiquetaCantidad
import com.factapp.jhonny.network.dto.model.filtrarPorBusqueda
import com.factapp.jhonny.network.mensajeAuth
import com.factapp.jhonny.ui.catalogo.CatalogoAlmacenBar
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioSaldoScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val esAdmin = usuario?.esAdmin() == true

    var registros by remember { mutableStateOf<List<InventarioSaldo>>(emptyList()) }
    var almacenes by remember { mutableStateOf<List<Almacen>>(emptyList()) }
    var almacenSeleccionadoId by remember(usuario?.almacenId) {
        mutableStateOf(usuario?.almacenId?.takeIf { it.isNotBlank() })
    }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val almacenFiltroId = if (esAdmin) almacenSeleccionadoId else usuario?.almacenId
    val almacenEtiquetaNombre = remember(almacenes, almacenFiltroId, usuario?.almacenNombre) {
        almacenes.find { it.id == almacenFiltroId }?.nombre
            ?: usuario?.almacenNombre?.takeIf { it.isNotBlank() }
    }

    val registrosFiltrados by remember(registros, busqueda) {
        derivedStateOf { registros.filtrarPorBusqueda(busqueda) }
    }

    val subtituloHeader by remember(registros.size, busqueda, registrosFiltrados.size) {
        derivedStateOf {
            when {
                registros.isEmpty() -> "Stock por producto"
                busqueda.isNotBlank() ->
                    "${registrosFiltrados.size} de ${registros.size} registros"
                else -> "${registros.size} registros con stock"
            }
        }
    }

    suspend fun cargarInventario(almacenId: String?) {
        InventarioRepository.listarInventario(
            companyRuc = companyRuc,
            token = token,
            almacenId = almacenId,
            soloConStock = true,
        )
            .onSuccess { registros = it }
            .onFailure { error = it.mensajeAuth() }
    }

    LaunchedEffect(companyRuc, token, esAdmin) {
        if (!esAdmin || companyRuc.isBlank() || token.isNullOrBlank()) {
            almacenes = emptyList()
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            InventarioRepository.listarAlmacenes(companyRuc, token)
                .onSuccess { lista ->
                    almacenes = lista
                    if (almacenSeleccionadoId == null || lista.none { it.id == almacenSeleccionadoId }) {
                        almacenSeleccionadoId = usuario?.almacenId
                            ?.takeIf { id -> lista.any { it.id == id } }
                            ?: lista.firstOrNull()?.id
                    }
                }
                .onFailure {
                    almacenes = emptyList()
                }
        }
    }

    LaunchedEffect(companyRuc, token, almacenFiltroId) {
        when {
            token.isNullOrBlank() -> {
                cargando = false
                registros = emptyList()
                error = "Inicia sesión para consultar inventario"
            }
            companyRuc.isBlank() -> {
                cargando = false
                registros = emptyList()
                error = "Tu usuario no tiene empresa vinculada"
            }
            else -> {
                cargando = true
                error = null
                withContext(Dispatchers.IO) { cargarInventario(almacenFiltroId) }
                cargando = false
            }
        }
    }

    fun recargar() {
        scope.launch {
            cargando = true
            error = null
            withContext(Dispatchers.IO) { cargarInventario(almacenFiltroId) }
            cargando = false
        }
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Inventario",
        subtitulo = subtituloHeader,
        icono = Icons.Default.Inventory2,
        onVolver = onVolver,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (!token.isNullOrBlank() && companyRuc.isNotBlank()) {
                CatalogoAlmacenBar(
                    esAdmin = esAdmin,
                    almacenNombre = almacenEtiquetaNombre,
                    almacenes = almacenes,
                    almacenSeleccionadoId = almacenSeleccionadoId,
                    onSeleccionarAlmacen = { almacenSeleccionadoId = it },
                )
            }
            when {
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = C.accent)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error!!,
                                color = C.textSecondary,
                                fontSize = 14.sp,
                            )
                            if (!token.isNullOrBlank() && companyRuc.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { recargar() }, enabled = !cargando) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
                registros.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Sin stock registrado",
                                color = C.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay productos con stock en este almacén",
                                color = C.textSecondary,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 4.dp),
                            ) {
                                CatalogoBusquedaBar(
                                    value = busqueda,
                                    onValueChange = { busqueda = it },
                                    totalItems = registros.size,
                                    resultados = registrosFiltrados.size,
                                )
                            }
                        }
                        if (registrosFiltrados.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Sin resultados para la búsqueda",
                                        color = C.textSecondary,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        } else {
                            items(registrosFiltrados, key = { it.id }) { row ->
                                InventarioSaldoCard(
                                    row = row,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventarioSaldoCard(
    row: InventarioSaldo,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.nombreProducto,
                    fontWeight = FontWeight.SemiBold,
                    color = C.textPrimary,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${row.etiquetaCantidad()} · ${row.nombreAlmacen}",
                    fontSize = 13.sp,
                    color = C.textSecondary,
                )
            }
            TipoInventarioBadge(tipo = row.tipo)
        }
    }
}

@Composable
private fun TipoInventarioBadge(tipo: TipoInventarioSaldo) {
    val (label, bg, fg) = when (tipo) {
        TipoInventarioSaldo.SERIE -> Triple("Serie", Color(0xFFE3F2FD), Color(0xFF1565C0))
        TipoInventarioSaldo.SALDO -> Triple("Saldo", Color(0xFFE8F5E9), Color(0xFF2E7D32))
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
        )
    }
}
