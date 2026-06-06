package com.factapp.jhonny.ui.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.MovimientoEstado
import com.factapp.jhonny.network.dto.model.MovimientoTipo
import com.factapp.jhonny.network.dto.model.etiquetaDestino
import com.factapp.jhonny.network.dto.model.detalleHistorial
import com.factapp.jhonny.network.dto.model.etiquetaTipoHistorial
import com.factapp.jhonny.network.dto.model.resumenProductos
import com.factapp.jhonny.network.dto.model.etiquetaGuiaRemision
import com.factapp.jhonny.network.dto.model.tituloHistorial
import com.factapp.jhonny.network.dto.demo.InventarioDemo
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.disponibleParaSalida
import com.factapp.jhonny.network.dto.disponibleParaIngreso
import com.factapp.jhonny.network.dto.formatCantidadConUnidad
import com.factapp.jhonny.network.dto.model.resumenSeries
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

@Composable
fun SalidasScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
    onNuevaSalida: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)
    val context = LocalContext.current
    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token

    var salidas by remember { mutableStateOf<List<Movimiento>>(emptyList()) }
    var catalogoLista by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var salidaAcciones by remember { mutableStateOf<Movimiento?>(null) }
    var salidaDetalle by remember { mutableStateOf<Movimiento?>(null) }

    val catalogoMap = remember(catalogoLista) { catalogoLista.associateBy { it.id } }
    val productosDisponibles = remember(catalogoLista) {
        catalogoLista.filter { it.disponibleParaSalida() }
    }

    var almacenesMap by remember { mutableStateOf<Map<String, Almacen>>(emptyMap()) }

    suspend fun recargar() {
        if (companyRuc.isBlank()) return
        CatalogRepository.listarParaGestion(companyRuc, token).onSuccess { catalogoLista = it }
        InventarioRepository.listarSalidas(companyRuc, token).onSuccess { salidas = it }
        InventarioRepository.listarAlmacenes(companyRuc, token).onSuccess { lista ->
            almacenesMap = lista.associateBy { it.id }
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
        withContext(Dispatchers.IO) { recargar() }
        cargando = false
    }

    val filtradas = remember(salidas, busqueda) {
        if (busqueda.isBlank()) salidas
        else {
            val q = busqueda.trim().lowercase()
            salidas.filter { mov ->
                mov.numeroDisplay.lowercase().contains(q) ||
                    mov.estado?.name?.lowercase()?.contains(q) == true ||
                    mov.cliente?.razonSocial?.lowercase()?.contains(q) == true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = "Salidas",
                subtitulo = "${salidas.size} salidas registradas",
                icono = Icons.Default.LocalShipping,
                onVolver = onVolver,
            )
        },
        floatingActionButton = {
            if (!cargando && error == null) {
                FloatingActionButton(
                    onClick = onNuevaSalida,
                    containerColor = C.accent,
                    contentColor = C.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva salida")
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
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = C.accent)
                }
                error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
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
                            SalidasAccionesRapidas(
                                productosDisponibles = productosDisponibles.size,
                                onNuevaSalida = onNuevaSalida,
                            )
                        }
                        item {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                BusquedaSeccion(busqueda, { busqueda = it }, salidas.size, filtradas.size)
                            }
                        }
                        if (salidas.isEmpty()) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Sin salidas aún. Registra la primera con +",
                                        color = C.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else if (filtradas.isEmpty()) {
                            item { MensajeSinResultados() }
                        } else {
                            items(filtradas, key = { it.id }) { salida ->
                                SalidaCard(
                                    salida = salida,
                                    almacenes = almacenesMap,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { salidaAcciones = salida },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }

    SalidaAccionesSheet(
        salida = salidaAcciones,
        onDismiss = { salidaAcciones = null },
        onMasOpciones = { salida ->
            salidaAcciones = null
            salidaDetalle = salida
        },
        onImprimir = { salida ->
            SalidaPrintHelper.imprimirSalida(context, salida, catalogoMap, almacenesMap)
            salidaAcciones = null
        },
        onCancelar = { salida ->
            salidas = salidas.map {
                if (it.id == salida.id) it.copy(estado = MovimientoEstado.ANULADA) else it
            }
            Toast.makeText(context, "Salida cancelada", Toast.LENGTH_SHORT).show()
            salidaAcciones = null
        },
        onEliminar = { salida ->
            salidas = salidas.filterNot { it.id == salida.id }
            Toast.makeText(context, "Salida eliminada", Toast.LENGTH_SHORT).show()
            salidaAcciones = null
        },
    )
    SalidaDetalleSheet(
        salida = salidaDetalle,
        catalogo = catalogoMap,
        almacenes = almacenesMap,
        onDismiss = { salidaDetalle = null },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalidaAccionesSheet(
    salida: Movimiento?,
    onDismiss: () -> Unit,
    onMasOpciones: (Movimiento) -> Unit,
    onImprimir: (Movimiento) -> Unit,
    onCancelar: (Movimiento) -> Unit,
    onEliminar: (Movimiento) -> Unit,
) {
    if (salida == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(C.border, RoundedCornerShape(50)),
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = C.accentSoft,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalShipping, null, tint = C.accent, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Opciones de salida",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = C.primary,
                    )
                    Text(
                        salida.numeroDisplay,
                        fontSize = 13.sp,
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = C.textSecondary)
                }
            }

            Spacer(Modifier.height(18.dp))

            SalidaAccionMenuItem(
                icono = Icons.Default.MoreHoriz,
                titulo = "Más opciones",
                detalle = "Ver todos los detalles de la salida",
                color = Color(0xFF6A1B9A),
                onClick = { onMasOpciones(salida) },
            )
            Spacer(Modifier.height(10.dp))
            SalidaAccionMenuItem(
                icono = Icons.Default.Print,
                titulo = "Imprimir",
                detalle = "Genera un PDF con detalle y firma de recepcion",
                color = C.accent,
                onClick = { onImprimir(salida) },
            )
            Spacer(Modifier.height(10.dp))
            SalidaAccionMenuItem(
                icono = Icons.Default.Cancel,
                titulo = "Cancelar salida",
                detalle = "Marca esta salida como anulada",
                color = Color(0xFFF57C00),
                onClick = { onCancelar(salida) },
            )
            Spacer(Modifier.height(10.dp))
            SalidaAccionMenuItem(
                icono = Icons.Default.Delete,
                titulo = "Eliminar",
                detalle = "Quita esta salida del listado",
                color = Color(0xFFC62828),
                onClick = { onEliminar(salida) },
            )
        }
    }
}

@Composable
private fun SalidaAccionMenuItem(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = C.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = color.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, color = C.textPrimary, fontSize = 15.sp)
                Text(detalle, color = C.textSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = C.textSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalidaDetalleSheet(
    salida: Movimiento?,
    catalogo: Map<String, CatalogItem>,
    almacenes: Map<String, Almacen>,
    onDismiss: () -> Unit,
) {
    if (salida == null) return

    val estadoColor = when (salida.estado) {
        MovimientoEstado.DESPACHADA -> Color(0xFF2E7D32)
        MovimientoEstado.BORRADOR -> Color(0xFFF57C00)
        MovimientoEstado.ANULADA -> Color(0xFFC62828)
        null -> Color(0xFF516B82)
    }
    val fecha = salida.fechaDespacho ?: salida.fecha
    val origen = almacenes[salida.almacenId]?.nombre ?: salida.almacenId
    val destino = salida.etiquetaDestino(almacenes) ?: "Sin destino"

    val detalle = MovimientoDetalleUi(
        etiqueta = salida.estado?.name ?: "Salida",
        titulo = salida.numeroDisplay,
        subtitulo = destino,
        fecha = fecha.take(10),
        hora = fecha.takeIf { it.length >= 16 && it[10] == 'T' }?.substring(11, 16),
        origen = origen,
        destino = destino,
        lineas = salida.lineas.map { linea ->
            val item = catalogo[linea.catalogItemId]
            val nombre = item?.nombre ?: linea.nombreEfectivo
            val unidad = item?.unidad ?: "NIU"
            MovimientoDetalleLineaUi(
                nombre = nombre,
                cantidad = formatCantidadConUnidad(linea.cantidad, unidad),
                series = linea.resumenSeries()
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    .orEmpty(),
                manejaSerie = linea.tieneSeries,
            )
        },
        campos = buildList {
            add(MovimientoDetalleCampoUi("N° movimiento", salida.numeroDisplay, Icons.Default.Tag))
            add(MovimientoDetalleCampoUi("Estado", salida.estado?.name ?: "Sin estado", Icons.Default.Tag))
            salida.cliente?.let { cliente ->
                add(
                    MovimientoDetalleCampoUi(
                        "Cliente / receptor",
                        cliente.razonSocial ?: "Doc. ${cliente.numeroDoc}",
                        Icons.Default.Person,
                    ),
                )
                add(MovimientoDetalleCampoUi("Documento", "${cliente.tipoDoc} - ${cliente.numeroDoc}", Icons.Default.Tag))
            }
            salida.comprobanteId?.let {
                add(MovimientoDetalleCampoUi("Comprobante venta", it, Icons.Default.Tag))
            }
            salida.etiquetaGuiaRemision()?.let { guia ->
                add(MovimientoDetalleCampoUi("Guía de remisión", guia, Icons.Default.LocalShipping))
            }
            add(MovimientoDetalleCampoUi("ID interno", salida.id, Icons.Default.Tag, valorSecundario = true))
        },
        notas = listOfNotNull(salida.observaciones?.takeIf { it.isNotBlank() }),
        icono = Icons.Default.LocalShipping,
        accentColor = estadoColor,
        headerColors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2)),
    )

    MovimientoDetalleComunSheet(detalle = detalle, onDismiss = onDismiss)
}

@Composable
private fun SalidaPuntoRuta(
    etiqueta: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(etiqueta, color = C.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        Text(valor, color = C.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 17.sp)
    }
}

@Composable
private fun SalidaDetalleFila(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.1f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = color, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(etiqueta, fontSize = 11.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
            Text(valor, fontSize = 14.sp, color = C.textPrimary, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun SalidaProductoDetalle(
    nombre: String,
    cantidad: String,
    series: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(10.dp),
                color = C.accent.copy(alpha = 0.1f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Inventory2, null, tint = C.accent, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(nombre, fontSize = 14.sp, color = C.textPrimary, fontWeight = FontWeight.SemiBold)
                Text(cantidad, fontSize = 12.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
            }
        }
        if (!series.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("Serie(s): $series", fontSize = 12.sp, color = C.textSecondary, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun SalidaDetalleDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp, end = 14.dp),
        thickness = 0.5.dp,
        color = C.border.copy(alpha = 0.35f),
    )
}

@Composable
private fun SalidasAccionesRapidas(
    productosDisponibles: Int,
    onNuevaSalida: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        InventarioAccionCard(
            icono = Icons.Default.LocalShipping,
            titulo = "Nueva salida",
            detalle = "$productosDisponibles productos con stock",
            onClick = onNuevaSalida,
        )
    }
}

@Composable
fun IngresosScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
    onIrACatalogo: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token

    var ingresos by remember { mutableStateOf<List<Movimiento>>(emptyList()) }
    var catalogoLista by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var almacenes by remember { mutableStateOf<List<Almacen>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var mostrarRegistrar by remember { mutableStateOf(false) }

    val catalogoMap = remember(catalogoLista) { catalogoLista.associateBy { it.id } }
    val almacenesMap = remember(almacenes) { almacenes.associateBy { it.id } }
    val productosDisponibles = remember(catalogoLista) {
        catalogoLista.filter { it.disponibleParaIngreso() }
    }

    suspend fun recargar() {
        if (companyRuc.isBlank()) return
        CatalogRepository.listarParaGestion(companyRuc, token).onSuccess { catalogoLista = it }
        InventarioRepository.listarIngresos(companyRuc, token).onSuccess { ingresos = it }
        InventarioRepository.listarAlmacenes(companyRuc, token).onSuccess { lista ->
            almacenes = lista
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
        withContext(Dispatchers.IO) { recargar() }
        cargando = false
    }

    val filtrados = remember(ingresos, busqueda, catalogoMap) {
        if (busqueda.isBlank()) ingresos
        else {
            val q = busqueda.trim().lowercase()
            ingresos.filter { mov ->
                mov.observaciones?.lowercase()?.contains(q) == true ||
                    mov.lineas.any { linea ->
                        catalogoMap[linea.catalogItemId]?.nombre?.lowercase()?.contains(q) == true ||
                            linea.nombreEfectivo.lowercase().contains(q) ||
                            linea.catalogItemId.lowercase().contains(q) ||
                            linea.productoSerie?.numeroSerie?.lowercase()?.contains(q) == true ||
                            linea.numerosSerieUi.any { sn -> sn.lowercase().contains(q) }
                    }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = "Ingresos",
                subtitulo = "Mercadería que entra a tu almacén",
                icono = Icons.Default.Input,
                onVolver = onVolver,
            )
        },
        floatingActionButton = {
            if (!cargando && error == null) {
                FloatingActionButton(
                    onClick = { mostrarRegistrar = true },
                    containerColor = C.accent,
                    contentColor = C.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo ingreso")
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
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = C.accent)
                }
                error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
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
                            IngresosAccionesRapidas(
                                productosDisponibles = productosDisponibles.size,
                                onNuevoIngreso = { mostrarRegistrar = true },
                                onIrACatalogo = onIrACatalogo,
                            )
                        }
                        item {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                BusquedaSeccion(busqueda, { busqueda = it }, ingresos.size, filtrados.size)
                            }
                        }
                        if (ingresos.isEmpty()) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Sin ingresos aún. Registra el primero con +",
                                        color = C.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else if (filtrados.isEmpty()) {
                            item { MensajeSinResultados() }
                        } else {
                            items(filtrados, key = { it.id }) { mov ->
                                IngresoCard(
                                    mov = mov,
                                    catalogo = catalogoMap,
                                    almacenes = almacenesMap,
                                    modifier = Modifier.padding(horizontal = 7.dp),
                                )
                            }
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }

    RegistrarIngresoSheet(
        visible = mostrarRegistrar,
        productosDisponibles = productosDisponibles,
        companyRuc = companyRuc,
        almacenes = almacenes,
        onDismiss = { mostrarRegistrar = false },
        onRegistrar = { body ->
            scope.launch {
                val resultado = withContext(Dispatchers.IO) {
                    InventarioRepository.registrarEntrada(companyRuc, token, body)
                }
                resultado
                    .onSuccess { mov ->
                        ingresos = listOf(mov) + ingresos
                        Toast.makeText(context, "Ingreso registrado", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure {
                        Toast.makeText(
                            context,
                            it.message ?: "No se pudo registrar",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
            }
        },
    )
}

@Composable
private fun IngresosAccionesRapidas(
    productosDisponibles: Int,
    onNuevoIngreso: () -> Unit,
    onIrACatalogo: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InventarioAccionCard(
                modifier = Modifier.weight(1f),
                icono = Icons.Default.Add,
                titulo = "Nuevo ingreso",
                detalle = "$productosDisponibles productos disponibles",
                onClick = onNuevoIngreso,
            )
            InventarioAccionCard(
                modifier = Modifier.weight(1f),
                icono = Icons.Default.Inventory2,
                titulo = "Catálogo",
                detalle = "Gestionar productos",
                onClick = onIrACatalogo,
            )
        }
    }
}

@Composable
private fun InventarioAccionCard(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, C.accent.copy(alpha = 0.35f)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Surface(Modifier.size(40.dp), shape = CircleShape, color = C.accentSoft) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, null, tint = C.accent, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(titulo, fontWeight = FontWeight.Bold, color = C.textPrimary, fontSize = 14.sp)
            Text(detalle, fontSize = 11.sp, color = C.textSecondary, lineHeight = 14.sp)
        }
    }
}

@Composable
fun HistorialInventarioScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)
    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token

    var movimientos by remember { mutableStateOf<List<Movimiento>>(emptyList()) }
    var almacenes by remember { mutableStateOf<List<Almacen>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var movimientoDetalle by remember { mutableStateOf<Movimiento?>(null) }
    var catalogoMap by remember { mutableStateOf<Map<String, CatalogItem>>(emptyMap()) }

    val almacenesMap = remember(almacenes) { almacenes.associateBy { it.id } }

    LaunchedEffect(companyRuc, token) {
        if (companyRuc.isBlank()) {
            cargando = false
            error = "Sin empresa vinculada"
            return@LaunchedEffect
        }
        cargando = true
        withContext(Dispatchers.IO) {
            val catalogo = CatalogRepository.listarParaGestion(companyRuc, token)
                .getOrNull()
                ?.associateBy { it.id }
                .orEmpty()
            catalogoMap = catalogo
            InventarioRepository.listarHistorial(companyRuc, token)
                .onSuccess { lista -> movimientos = lista }
                .onFailure { error = it.message }
            InventarioRepository.listarAlmacenes(companyRuc, token).onSuccess { lista ->
                almacenes = lista
            }
        }
        cargando = false
    }

    val filtrados = remember(movimientos, busqueda, catalogoMap) {
        movimientos.filtrarHistorial(busqueda, catalogoMap)
    }

    InventarioListaScaffold(
        modifier = modifier,
        titulo = "Historial",
        subtitulo = "Vida del producto · ingresos y salidas",
        icono = Icons.Default.History,
        onVolver = onVolver,
        cargando = cargando,
        error = error,
        vacio = movimientos.isEmpty(),
        mensajeVacio = "Sin movimientos en el historial",
    ) {
        item {
            BusquedaSeccion(busqueda, { busqueda = it }, movimientos.size, filtrados.size)
        }
        if (filtrados.isEmpty() && movimientos.isNotEmpty()) {
            item { MensajeSinResultados() }
        } else {
            items(filtrados, key = { it.id }) { mov ->
                HistorialMovimientoCard(
                    movimiento = mov,
                    catalogo = catalogoMap,
                    modifier = Modifier.padding(horizontal = 0.dp),
                    onClick = { movimientoDetalle = mov },
                )
            }
        }
    }

    HistorialMovimientoDetalleSheet(
        movimiento = movimientoDetalle,
        catalogo = catalogoMap,
        almacenes = almacenesMap,
        onDismiss = { movimientoDetalle = null },
    )
}

private fun List<Movimiento>.filtrarHistorial(
    query: String,
    catalogo: Map<String, CatalogItem>,
): List<Movimiento> {
    if (query.isBlank()) return this
    val q = query.trim().lowercase()
    return filter { mov ->
        mov.numeroDisplay.lowercase().contains(q) ||
            mov.tituloHistorial().lowercase().contains(q) ||
            mov.detalleHistorial(catalogo).lowercase().contains(q) ||
            mov.etiquetaTipoHistorial().lowercase().contains(q) ||
            mov.observaciones?.lowercase()?.contains(q) == true
    }
}

@Composable
private fun InventarioListaScaffold(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onVolver: () -> Unit,
    cargando: Boolean,
    error: String?,
    vacio: Boolean,
    mensajeVacio: String,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = titulo,
                subtitulo = subtitulo,
                icono = icono,
                onVolver = onVolver,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = C.accent)
                }
                error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error, color = C.textSecondary)
                }
                vacio -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(mensajeVacio, color = C.textSecondary, fontWeight = FontWeight.Medium)
                }
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    content()
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BusquedaSeccion(
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    total: Int,
    resultados: Int,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        CatalogoBusquedaBar(
            value = busqueda,
            onValueChange = onBusquedaChange,
            totalItems = total,
            resultados = resultados,
        )
    }
}

@Composable
private fun MensajeSinResultados() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Sin resultados para tu búsqueda", color = C.textSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun SalidaCard(
    salida: Movimiento,
    almacenes: Map<String, Almacen> = emptyMap(),
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val estadoColor = when (salida.estado) {
        MovimientoEstado.DESPACHADA -> Color(0xFF2E7D32)
        MovimientoEstado.BORRADOR -> Color(0xFFF57C00)
        MovimientoEstado.ANULADA -> Color(0xFFC62828)
        null -> Color(0xFF516B82)
    }
    val destino = salida.etiquetaDestino(almacenes) ?: "Destino no especificado"
    val origen = almacenes[salida.almacenId]?.nombre ?: salida.almacenId
    val fecha = (salida.fechaDespacho ?: salida.fecha).take(10)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = estadoColor.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = estadoColor,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            salida.numeroDisplay,
                            fontWeight = FontWeight.Bold,
                            color = C.textPrimary,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = estadoColor.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = salida.estado?.name ?: "SIN ESTADO",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = estadoColor,
                                letterSpacing = 0.4.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = destino,
                        fontSize = 14.sp,
                        color = C.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = C.surfaceSoft,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Warehouse, null, tint = C.accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Desde $origen",
                        fontSize = 12.sp,
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = C.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = C.accentSoft,
                ) {
                    Text(
                        "${salida.lineas.size} línea(s)",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = C.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    fecha,
                    fontSize = 12.sp,
                    color = C.textSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Ver opciones",
                    fontSize = 12.sp,
                    color = C.accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun IngresoCard(
    mov: Movimiento,
    catalogo: Map<String, CatalogItem>,
    almacenes: Map<String, Almacen> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    val almacen = almacenes[mov.almacenId]?.nombre ?: mov.almacenId
    val fecha = mov.fecha.take(10)
    val lineasPreview = mov.lineas.take(2)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Input,
                            null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Ingreso",
                            fontWeight = FontWeight.Bold,
                            color = C.textPrimary,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                        ) {
                            Text(
                                "ENTRADA",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                letterSpacing = 0.4.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "Destino: $almacen",
                        fontSize = 14.sp,
                        color = C.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                    )
                    mov.observaciones?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            it,
                            fontSize = 12.sp,
                            color = C.textSecondary,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = C.surfaceSoft,
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warehouse, null, tint = C.accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Productos ingresados",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    lineasPreview.forEach { linea ->
                        val item = catalogo[linea.catalogItemId]
                        val nombre = item?.nombre ?: linea.nombreEfectivo
                        val unidad = item?.unidad ?: "NIU"
                        val seriesTexto = linea.resumenSeries()
                        Text(
                            text = buildString {
                                append("• $nombre — ${formatCantidadConUnidad(linea.cantidad, unidad)}")
                                if (seriesTexto != null) append(" · SN: $seriesTexto")
                            },
                            fontSize = 12.sp,
                            color = C.textPrimary,
                            lineHeight = 17.sp,
                        )
                    }
                    if (mov.lineas.size > lineasPreview.size) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "+${mov.lineas.size - lineasPreview.size} producto(s) más",
                            fontSize = 12.sp,
                            color = C.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = C.accentSoft,
                ) {
                    Text(
                        "${mov.lineas.size} línea(s)",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = C.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    fecha,
                    fontSize = 12.sp,
                    color = C.textSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HistorialMovimientoCard(
    movimiento: Movimiento,
    catalogo: Map<String, CatalogItem>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val (icono, color) = when (movimiento.tipo) {
        MovimientoTipo.ENTRADA -> Icons.Default.Input to Color(0xFF2E7D32)
        MovimientoTipo.SALIDA -> Icons.Default.LocalShipping to Color(0xFF1565C0)
        MovimientoTipo.AJUSTE -> Icons.Default.Tune to Color(0xFFF57C00)
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
                .padding(10.dp),
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
                    "${movimiento.etiquetaTipoHistorial()} · ${movimiento.fechaEfectiva.take(16).replace('T', ' ')}",
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
