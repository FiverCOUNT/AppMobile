package com.factapp.jhonny

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.extras.LoadingOverlay
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.modelos.esAdmin
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.toActualizarRequest
import com.factapp.jhonny.network.dto.model.toCrearRequest
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.mensajeAuth
import com.factapp.jhonny.network.dto.etiquetaStock
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.ui.catalogo.AgregarCatalogItemSheet
import com.factapp.jhonny.ui.catalogo.CatalogItemActionSheet
import com.factapp.jhonny.ui.catalogo.CatalogoAlmacenBar
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.catalogo.filtrarPorBusqueda
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val puedeGestionar = usuario?.esAdmin() == true

    var items by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var almacenes by remember { mutableStateOf<List<Almacen>>(emptyList()) }
    var almacenSeleccionadoId by remember(usuario?.almacenId) {
        mutableStateOf(usuario?.almacenId?.takeIf { it.isNotBlank() })
    }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var itemOpciones by remember { mutableStateOf<CatalogItem?>(null) }
    var mostrarAgregar by remember { mutableStateOf(false) }
    var itemAEditar by remember { mutableStateOf<CatalogItem?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val almacenFiltroId = if (puedeGestionar) almacenSeleccionadoId else usuario?.almacenId
    val almacenEtiquetaNombre = remember(almacenes, almacenFiltroId, usuario?.almacenNombre) {
        almacenes.find { it.id == almacenFiltroId }?.nombre
            ?: usuario?.almacenNombre?.takeIf { it.isNotBlank() }
    }

    val itemsFiltrados by remember(items, busqueda) {
        derivedStateOf { items.filtrarPorBusqueda(busqueda) }
    }

    val subtituloHeader by remember(items.size, busqueda, itemsFiltrados.size) {
        derivedStateOf {
            when {
                items.isEmpty() -> "Productos y servicios"
                busqueda.isNotBlank() ->
                    "${itemsFiltrados.size} de ${items.size} ítems"
                else -> "${items.size} ítems"
            }
        }
    }

    suspend fun cargarCatalogo(almacenId: String?) {
        val resultado = if (!almacenId.isNullOrBlank()) {
            CatalogRepository.listarPorAlmacen(companyRuc, token, almacenId)
        } else {
            CatalogRepository.listarParaGestion(companyRuc, token)
        }
        resultado
            .onSuccess { items = it }
            .onFailure { error = it.mensajeAuth() }
    }

    LaunchedEffect(companyRuc, token, puedeGestionar) {
        if (!puedeGestionar || companyRuc.isBlank() || token.isNullOrBlank()) {
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
                items = emptyList()
                error = "Inicia sesión para cargar el catálogo del servidor"
            }
            companyRuc.isBlank() -> {
                cargando = false
                items = emptyList()
                error = "Tu usuario no tiene empresa vinculada"
            }
            else -> {
                cargando = true
                error = null
                withContext(Dispatchers.IO) { cargarCatalogo(almacenFiltroId) }
                cargando = false
            }
        }
    }

    fun actualizarItem(actualizado: CatalogItem) {
        items = items.map { if (it.id == actualizado.id) actualizado else it }
    }

    fun recargar() {
        scope.launch {
            cargando = true
            error = null
            withContext(Dispatchers.IO) { cargarCatalogo(almacenFiltroId) }
            cargando = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = "Catálogo",
                subtitulo = subtituloHeader,
                icono = Icons.Default.Description,
                onVolver = onVolver,
            )
        },
        floatingActionButton = {
            if (puedeGestionar && !token.isNullOrBlank() && companyRuc.isNotBlank()) {
                FloatingActionButton(
                    onClick = { if (!guardando) mostrarAgregar = true },
                    containerColor = C.accent,
                    contentColor = C.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir ítem")
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (!token.isNullOrBlank() && companyRuc.isNotBlank()) {
                CatalogoAlmacenBar(
                    esAdmin = puedeGestionar,
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
                                Button(
                                    onClick = { recargar() },
                                    enabled = !cargando,
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
                items.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No hay ítems en el catálogo",
                                color = C.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (puedeGestionar) {
                                    "Toca + para añadir el primero"
                                } else {
                                    "No hay productos disponibles en tu almacén"
                                },
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
                                    totalItems = items.size,
                                    resultados = itemsFiltrados.size,
                                )
                            }
                        }
                        if (itemsFiltrados.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Sin resultados",
                                            color = C.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Prueba otro nombre",
                                            color = C.textSecondary,
                                            fontSize = 14.sp,
                                        )
                                    }
                                }
                            }
                        } else {
                            items(itemsFiltrados, key = { it.id }) { item ->
                                CatalogoItemCard(
                                    item = item,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    mostrarOpciones = puedeGestionar,
                                    onOpciones = { itemOpciones = item },
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }

    if (puedeGestionar) CatalogItemActionSheet(
        item = itemOpciones,
        onDismiss = { itemOpciones = null },
        onEditar = { seleccionado ->
            itemAEditar = seleccionado
            itemOpciones = null
        },
        onToggleActivo = { seleccionado ->
            scope.launch {
                guardando = true
                val nuevoActivo = !seleccionado.activo
                withContext(Dispatchers.IO) {
                    CatalogRepository.cambiarActivo(companyRuc, token, seleccionado.id, nuevoActivo)
                        .onSuccess { actualizado ->
                            actualizarItem(actualizado)
                            itemOpciones = null
                        }
                        .onFailure { error = it.mensajeAuth() }
                }
                guardando = false
            }
        },
        onEliminar = { eliminado ->
            scope.launch {
                guardando = true
                withContext(Dispatchers.IO) {
                    CatalogRepository.eliminar(companyRuc, token, eliminado.id)
                        .onSuccess {
                            items = items.filter { it.id != eliminado.id }
                            itemOpciones = null
                        }
                        .onFailure { error = it.mensajeAuth() }
                }
                guardando = false
            }
        },
    )

    if (puedeGestionar) AgregarCatalogItemSheet(
        visible = mostrarAgregar,
        companyRuc = companyRuc,
        onDismiss = { mostrarAgregar = false },
        onGuardar = { nuevo ->
            scope.launch {
                guardando = true
                withContext(Dispatchers.IO) {
                    CatalogRepository.crear(companyRuc, token, nuevo.toCrearRequest())
                        .onSuccess { creado ->
                            items = items + creado
                            mostrarAgregar = false
                        }
                        .onFailure { error = it.mensajeAuth() }
                }
                guardando = false
            }
        },
    )

    if (puedeGestionar) AgregarCatalogItemSheet(
        visible = itemAEditar != null,
        companyRuc = companyRuc,
        itemExistente = itemAEditar,
        onDismiss = { itemAEditar = null },
        onGuardar = { actualizado ->
            scope.launch {
                val id = itemAEditar?.id ?: return@launch
                guardando = true
                withContext(Dispatchers.IO) {
                    CatalogRepository.actualizar(
                        companyRuc,
                        token,
                        id,
                        actualizado.toActualizarRequest(),
                    )
                        .onSuccess { respuesta ->
                            actualizarItem(respuesta)
                            itemAEditar = null
                        }
                        .onFailure { error = it.mensajeAuth() }
                }
                guardando = false
            }
        },
    )

    LoadingOverlay(
        visible = guardando,
        message = "Guardando en el servidor...",
    )
    }
}

@Composable
private fun CatalogoItemCard(
    item: CatalogItem,
    mostrarOpciones: Boolean,
    onOpciones: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = if (item.activo) 1f else 0.55f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (mostrarOpciones) Modifier.clickable(onClick = onOpciones)
                    else Modifier
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.nombre,
                        fontWeight = FontWeight.SemiBold,
                        color = C.textPrimary,
                        fontSize = 16.sp,
                    )
                    if (!item.activo) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFE5E5),
                        ) {
                            Text(
                                text = "Inactivo",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildString {
                        append(item.tipo.etiqueta)
                        item.etiquetaStock()?.let { append(" · $it") }
                    },
                    fontSize = 13.sp,
                    color = C.textSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatearSoles(item.precioUnitario),
                    fontWeight = FontWeight.Bold,
                    color = C.accent,
                    fontSize = 15.sp,
                )
                if (mostrarOpciones) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable(onClick = onOpciones),
                        shape = CircleShape,
                        color = C.surfaceSoft,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Opciones",
                                tint = C.textSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
