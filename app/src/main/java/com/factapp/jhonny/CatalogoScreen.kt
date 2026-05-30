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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.etiquetaStock
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.ui.catalogo.AgregarCatalogItemSheet
import com.factapp.jhonny.ui.catalogo.CatalogItemActionSheet
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.catalogo.filtrarPorBusqueda
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
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

    var items by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var itemOpciones by remember { mutableStateOf<CatalogItem?>(null) }
    var mostrarAgregar by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    val itemsFiltrados by remember(items, busqueda) {
        derivedStateOf { items.filtrarPorBusqueda(busqueda) }
    }

    val subtituloHeader by remember(items.size, busqueda, itemsFiltrados.size) {
        derivedStateOf {
            when {
                items.isEmpty() -> "Productos y servicios"
                busqueda.isNotBlank() ->
                    "${itemsFiltrados.size} de ${items.size} ítems"
                else -> "${items.size} ítems · productos y servicios"
            }
        }
    }

    LaunchedEffect(companyRuc, token) {
        if (companyRuc.isBlank()) {
            cargando = false
            error = "Sin empresa vinculada"
            return@LaunchedEffect
        }
        cargando = true
        error = null
        withContext(Dispatchers.IO) {
            CatalogRepository.listarParaGestion(companyRuc, token)
                .onSuccess { items = it }
                .onFailure { error = it.message ?: "No se pudo cargar el catálogo" }
        }
        cargando = false
    }

    fun actualizarItem(actualizado: CatalogItem) {
        items = items.map { if (it.id == actualizado.id) actualizado else it }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            FloatingActionButton(
                onClick = { mostrarAgregar = true },
                containerColor = C.accent,
                contentColor = C.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir ítem")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
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
                        Text(text = error!!, color = C.textSecondary)
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
                                text = "Toca + para añadir el primero",
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
                                    .padding(top = 8.dp, bottom = 4.dp),
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
                                            text = "Prueba otro nombre o código",
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

    CatalogItemActionSheet(
        item = itemOpciones,
        onDismiss = { itemOpciones = null },
        onToggleActivo = { seleccionado ->
            actualizarItem(seleccionado.copy(activo = !seleccionado.activo))
        },
        onEliminar = { eliminado ->
            items = items.filter { it.id != eliminado.id }
            itemOpciones = null
        },
    )

    AgregarCatalogItemSheet(
        visible = mostrarAgregar,
        companyRuc = companyRuc,
        onDismiss = { mostrarAgregar = false },
        onGuardar = { nuevo -> items = items + nuevo },
    )
}

@Composable
private fun CatalogoItemCard(
    item: CatalogItem,
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
                .clickable(onClick = onOpciones)
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
                        item.codigo?.let { append(" · $it") }
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
