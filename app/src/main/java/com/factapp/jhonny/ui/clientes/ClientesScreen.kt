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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.ClienteRepository
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.lineaPrincipal
import com.factapp.jhonny.network.dto.request.CrearClienteRequest
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

private fun List<Cliente>.filtrarPorBusqueda(query: String): List<Cliente> {
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
fun ClientesScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val scope = rememberCoroutineScope()
    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token

    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var clienteContacto by remember { mutableStateOf<Cliente?>(null) }
    var mostrarAgregar by remember { mutableStateOf(false) }
    var guardandoCliente by remember { mutableStateOf(false) }
    var errorAgregar by remember { mutableStateOf<String?>(null) }

    suspend fun recargar() {
        if (companyRuc.isBlank()) return
        ClienteRepository.listar(companyRuc, token)
            .onSuccess { clientes = it }
            .onFailure { error = it.message ?: "No se pudieron cargar los clientes" }
    }

    LaunchedEffect(companyRuc, token) {
        if (companyRuc.isBlank()) {
            cargando = false
            error = "Sin empresa vinculada"
            return@LaunchedEffect
        }
        cargando = true
        error = null
        withContext(Dispatchers.IO) { recargar() }
        cargando = false
    }

    val filtrados by remember(clientes, busqueda) {
        derivedStateOf { clientes.filtrarPorBusqueda(busqueda) }
    }

    val subtituloHeader by remember(clientes.size, busqueda, filtrados.size) {
        derivedStateOf {
            when {
                clientes.isEmpty() -> "Registra personas con DNI"
                busqueda.isNotBlank() -> "${filtrados.size} de ${clientes.size} clientes"
                else -> "${clientes.size} clientes registrados"
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = "Clientes",
                subtitulo = subtituloHeader,
                icono = Icons.Default.People,
                onVolver = onVolver,
            )
        },
        floatingActionButton = {
            if (!cargando && error == null) {
                FloatingActionButton(
                    onClick = {
                        errorAgregar = null
                        mostrarAgregar = true
                    },
                    containerColor = C.accent,
                    contentColor = C.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo cliente")
                }
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
                                CatalogoBusquedaBar(
                                    value = busqueda,
                                    onValueChange = { busqueda = it },
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Puedes registrar clientes con DNI. Los RUC se crean al facturar.",
                                    fontSize = 12.sp,
                                    color = C.textSecondary,
                                    lineHeight = 16.sp,
                                )
                            }
                        }
                        if (clientes.isEmpty()) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Sin clientes. Toca + para registrar uno con DNI",
                                        color = C.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else if (filtrados.isEmpty()) {
                            item { ClientesSinResultadosBusqueda() }
                        } else {
                            items(filtrados, key = { it.id }) { cliente ->
                                ClienteCard(
                                    cliente = cliente,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { clienteContacto = cliente },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }

    ClienteContactoSheet(
        cliente = clienteContacto,
        onDismiss = { clienteContacto = null },
    )

    AgregarClienteSheet(
        visible = mostrarAgregar,
        guardando = guardandoCliente,
        error = errorAgregar,
        onDismiss = {
            if (!guardandoCliente) {
                mostrarAgregar = false
                errorAgregar = null
            }
        },
        onGuardar = { body ->
            guardandoCliente = true
            errorAgregar = null
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    ClienteRepository.crear(companyRuc, token, body)
                }
                guardandoCliente = false
                result.onSuccess {
                    mostrarAgregar = false
                    errorAgregar = null
                    withContext(Dispatchers.IO) { recargar() }
                }.onFailure {
                    errorAgregar = it.message ?: "No se pudo registrar el cliente"
                }
            }
        },
    )
}

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = C.accentSoft,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (cliente.esPersonaNatural) {
                                Icons.Default.Person
                            } else {
                                Icons.Default.Business
                            },
                            contentDescription = null,
                            tint = C.accent,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cliente.razonSocial,
                        fontWeight = FontWeight.Bold,
                        color = C.textPrimary,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = cliente.etiquetaDocumento,
                        fontSize = 13.sp,
                        color = C.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!cliente.esPersonaNatural) {
                        Text(
                            text = "Desde venta / facturación",
                            fontSize = 11.sp,
                            color = C.textSecondary,
                        )
                    }
                }
            }
            cliente.address.lineaPrincipal?.takeIf { it.isNotBlank() }?.let { dir ->
                Spacer(Modifier.height(10.dp))
                ClienteDetalleLinea(etiqueta = "Dirección", valor = dir)
            }
            cliente.telefono?.takeIf { it.isNotBlank() }?.let { tel ->
                Spacer(Modifier.height(6.dp))
                ClienteDetalleLinea(etiqueta = "Teléfono", valor = tel)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Contactar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = C.accent,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = C.accent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ClientesSinResultadosBusqueda() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Sin resultados para tu búsqueda",
            color = C.textSecondary,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ClienteDetalleLinea(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$etiqueta: ",
            fontSize = 13.sp,
            color = C.textSecondary,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            color = C.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}
