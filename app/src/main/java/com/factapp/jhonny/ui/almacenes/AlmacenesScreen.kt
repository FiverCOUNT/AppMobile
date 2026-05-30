package com.factapp.jhonny.ui.almacenes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.lineaPrincipal
import com.factapp.jhonny.network.dto.request.CrearAlmacenRequest
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.ui.components.ApplySystemBarsColor
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private object AlmacenesTheme {
    val bg = Color(0xFF0E1218)
    val heroTop = Color(0xFF1A2332)
    val heroBottom = Color(0xFF0E1218)
    val glowAmber = Color(0x33E8A54B)
    val glowBlue = Color(0x225B8DEF)
    val card = Color(0xFF1A222C)
    val cardBorder = Color(0xFF2A3544)
    val textPrimary = Color(0xFFF2EDE6)
    val textMuted = Color(0xFF8E9BAA)
    val amber = Color(0xFFE8A54B)
    val amberDeep = Color(0xFFC45C26)
    val pinActive = Color(0xFF6BCB9A)

    val acentos = listOf(
        Color(0xFFE8A54B),
        Color(0xFF5B8DEF),
        Color(0xFF6BCB9A),
        Color(0xFFB07CCF),
        Color(0xFFE8786B),
        Color(0xFF4ECDC4),
    )
}

private fun List<Almacen>.filtrarPorBusqueda(query: String): List<Almacen> {
    val q = query.trim().lowercase()
    if (q.isBlank()) return this
    return filter { almacen ->
        almacen.nombre.lowercase().contains(q) ||
            almacen.codigo.lowercase().contains(q) ||
            almacen.address.lineaPrincipal?.lowercase()?.contains(q) == true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlmacenesScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    ApplySystemBarsColor(
        statusBarColor = AlmacenesTheme.heroTop,
        navigationBarColor = AlmacenesTheme.bg,
        lightStatusBarIcons = false,
        lightNavigationBarIcons = false,
    )

    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val scope = rememberCoroutineScope()

    var almacenes by remember { mutableStateOf<List<Almacen>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarAgregar by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    val almacenesFiltrados by remember(almacenes, busqueda) {
        derivedStateOf { almacenes.filtrarPorBusqueda(busqueda) }
    }

    suspend fun recargar() {
        InventarioRepository.listarAlmacenes(companyRuc, token)
            .onSuccess { almacenes = it }
            .onFailure { error = it.message ?: "No se pudieron cargar los almacenes" }
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

    fun guardarAlmacen(body: CrearAlmacenRequest) {
        scope.launch {
            withContext(Dispatchers.IO) {
                InventarioRepository.crearAlmacen(companyRuc, token, body)
                    .onSuccess { nuevo -> almacenes = almacenes + nuevo }
                    .onFailure { error = it.message ?: "No se pudo crear el almacén" }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AlmacenesTheme.bg,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AlmacenesTheme.bg,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Surface(
                        onClick = { mostrarAgregar = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(AlmacenesTheme.amberDeep, AlmacenesTheme.amber),
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                )
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF1A1208),
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(modifier = Modifier.size(10.dp))
                                Text(
                                    text = "Registrar nueva bodega",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1208),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
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
                        CircularProgressIndicator(color = AlmacenesTheme.amber)
                    }
                }
                error != null && almacenes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = error!!, color = AlmacenesTheme.textMuted)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        item {
                            AlmacenesHeroHeader(
                                total = almacenes.size,
                                onVolver = onVolver,
                            )
                        }
                        item {
                            AlmacenesBusquedaPill(
                                value = busqueda,
                                onValueChange = { busqueda = it },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            )
                        }
                        if (almacenes.isEmpty()) {
                            item {
                                AlmacenesEstadoVacio(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
                                )
                            }
                        } else if (almacenesFiltrados.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Ninguna bodega coincide con tu búsqueda",
                                        color = AlmacenesTheme.textMuted,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(
                                items = almacenesFiltrados,
                                key = { _, almacen -> almacen.id },
                            ) { index, almacen ->
                                BodegaTarjetaMapa(
                                    almacen = almacen,
                                    indice = index,
                                    modifier = Modifier.padding(
                                        start = if (index % 2 == 0) 20.dp else 36.dp,
                                        end = if (index % 2 == 0) 36.dp else 20.dp,
                                        top = 10.dp,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    AgregarAlmacenSheet(
        visible = mostrarAgregar,
        onDismiss = { mostrarAgregar = false },
        onGuardar = { body -> guardarAlmacen(body) },
    )
}

@Composable
private fun AlmacenesHeroHeader(
    total: Int,
    onVolver: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(AlmacenesTheme.heroTop, AlmacenesTheme.heroBottom),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-40).dp, y = (-30).dp)
                .background(AlmacenesTheme.glowAmber, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 20.dp)
                .background(AlmacenesTheme.glowBlue, CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp)
                .padding(bottom = 20.dp),
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = AlmacenesTheme.textPrimary,
                )
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2A3544),
                    ) {
                        Icon(
                            Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = AlmacenesTheme.amber,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(26.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(14.dp))
                    Column {
                        Text(
                            text = "Red de bodegas",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlmacenesTheme.textPrimary,
                            lineHeight = 30.sp,
                        )
                        Text(
                            text = if (total == 0) "Sin ubicaciones aún" else "$total punto(s) de stock",
                            fontSize = 14.sp,
                            color = AlmacenesTheme.textMuted,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Cada almacén es un nodo donde vive tu inventario. Despachos e ingresos parten de aquí.",
                    fontSize = 13.sp,
                    color = AlmacenesTheme.textMuted,
                    lineHeight = 19.sp,
                )
            }
        }
    }
}

@Composable
private fun AlmacenesBusquedaPill(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(AlmacenesTheme.card)
            .border(1.dp, AlmacenesTheme.cardBorder, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = AlmacenesTheme.textMuted,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.size(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = AlmacenesTheme.textPrimary,
                fontSize = 15.sp,
            ),
            cursorBrush = SolidColor(AlmacenesTheme.amber),
            singleLine = true,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "Buscar bodega o dirección…",
                            color = AlmacenesTheme.textMuted,
                            fontSize = 15.sp,
                        )
                    }
                    inner()
                }
            },
        )
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Limpiar",
                    tint = AlmacenesTheme.textMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun AlmacenesEstadoVacio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AlmacenesTheme.card)
            .border(1.dp, AlmacenesTheme.cardBorder, RoundedCornerShape(24.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(AlmacenesTheme.amberDeep.copy(alpha = 0.35f), AlmacenesTheme.glowBlue),
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                tint = AlmacenesTheme.amber,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu mapa está vacío",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AlmacenesTheme.textPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Registra la primera bodega para organizar stock y traslados.",
            fontSize = 14.sp,
            color = AlmacenesTheme.textMuted,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun BodegaTarjetaMapa(
    almacen: Almacen,
    indice: Int,
    modifier: Modifier = Modifier,
) {
    val acento = AlmacenesTheme.acentos[indice % AlmacenesTheme.acentos.size]
    val inicial = almacen.nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "B"
    val alpha = if (almacen.activo) 1f else 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 8.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(acento.copy(alpha = 0.18f)),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = AlmacenesTheme.card,
            border = androidx.compose.foundation.BorderStroke(1.dp, AlmacenesTheme.cardBorder),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(acento.copy(alpha = 0.85f), acento.copy(alpha = 0.45f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = inicial,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color(0xFF0E1218),
                    )
                }
                Spacer(modifier = Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = almacen.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AlmacenesTheme.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (almacen.activo) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(AlmacenesTheme.pinActive, CircleShape),
                                )
                                Spacer(modifier = Modifier.size(5.dp))
                                Text(
                                    text = "Activo",
                                    fontSize = 11.sp,
                                    color = AlmacenesTheme.pinActive,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val direccionAlmacen = almacen.address.lineaPrincipal
                    if (!direccionAlmacen.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = acento,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = direccionAlmacen,
                                fontSize = 13.sp,
                                color = AlmacenesTheme.textMuted,
                                lineHeight = 18.sp,
                            )
                        }
                    } else {
                        Text(
                            text = "Sin dirección registrada",
                            fontSize = 13.sp,
                            color = AlmacenesTheme.textMuted.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )
                    }
                }
            }
        }
    }
}
