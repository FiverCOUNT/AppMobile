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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.factapp.jhonny.extras.LoadingOverlay
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.modelos.esAdmin
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.mensajeAuth
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
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.almacenIdParaOperaciones
import com.factapp.jhonny.network.dto.disponibleParaSalida
import com.factapp.jhonny.network.dto.disponibleParaIngreso
import com.factapp.jhonny.network.dto.formatCantidadConUnidad
import com.factapp.jhonny.network.dto.model.UbicacionProducto
import com.factapp.jhonny.network.dto.model.HistorialItemTipo
import com.factapp.jhonny.network.dto.model.etiquetaCantidad
import com.factapp.jhonny.network.dto.model.etiquetaRecorrido
import com.factapp.jhonny.network.dto.model.etiquetaTipo
import com.factapp.jhonny.network.dto.model.fechaHoraCompacto
import com.factapp.jhonny.network.dto.model.fechaLegible
import com.factapp.jhonny.network.dto.model.horaLegible
import com.factapp.jhonny.network.dto.model.resumenSeries
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        val cat = withContext(Dispatchers.IO) {
            CatalogRepository.listarParaGestion(companyRuc, token)
        }
        val sal = withContext(Dispatchers.IO) {
            InventarioRepository.listarSalidas(companyRuc, token)
        }
        val alm = withContext(Dispatchers.IO) {
            InventarioRepository.listarAlmacenes(companyRuc, token, todos = true)
        }
        withContext(Dispatchers.Main) {
            cat.onSuccess { catalogoLista = it }
            sal.onSuccess { salidas = it }
            alm.onSuccess { lista -> almacenesMap = lista.associateBy { it.id } }
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
        recargar()
        cargando = false
    }

    val filtradas = remember(salidas, busqueda, almacenesMap) {
        if (busqueda.isBlank()) salidas
        else {
            val q = busqueda.trim().lowercase()
            salidas.filter { mov ->
                mov.numeroDisplay.lowercase().contains(q) ||
                    mov.estado?.name?.lowercase()?.contains(q) == true ||
                    mov.cliente?.razonSocial?.lowercase()?.contains(q) == true ||
                    mov.cliente?.numeroDoc?.lowercase()?.contains(q) == true ||
                    almacenesMap[mov.almacenId]?.nombre?.lowercase()?.contains(q) == true ||
                    almacenesMap[mov.almacenId]?.codigo?.lowercase()?.contains(q) == true ||
                    almacenesMap[mov.almacenDestinoId]?.nombre?.lowercase()?.contains(q) == true ||
                    almacenesMap[mov.almacenDestinoId]?.codigo?.lowercase()?.contains(q) == true ||
                    mov.etiquetaDestino(almacenesMap)?.lowercase()?.contains(q) == true
            }
        }
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Salidas",
        subtitulo = "${salidas.size} salidas registradas",
        icono = Icons.Default.LocalShipping,
        onVolver = onVolver,
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
                                BusquedaSeccion(
                                    busqueda = busqueda,
                                    onBusquedaChange = { busqueda = it },
                                    total = salidas.size,
                                    resultados = filtradas.size,
                                    placeholder = "Número, cliente, almacén…",
                                )
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
    val origen = almacenes[salida.almacenId]?.nombre ?: salida.almacenId
    val destino = salida.etiquetaDestino(almacenes) ?: "Sin destino"
    val detalle = MovimientoDetalleUi(
        etiqueta = salida.estado?.name ?: "Salida",
        titulo = salida.numeroDisplay,
        subtitulo = destino,
        fecha = salida.fechaLegible(),
        hora = salida.horaLegible(),
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
    var esDevolucionInicial by remember { mutableStateOf(false) }
    var registrando by remember { mutableStateOf(false) }
    var ingresoAcciones by remember { mutableStateOf<Movimiento?>(null) }
    var ingresoDetalle by remember { mutableStateOf<Movimiento?>(null) }

    val catalogoMap = remember(catalogoLista) {
        catalogoLista.distinctBy { it.id }.associateBy { it.id }
    }
    val almacenesMap = remember(almacenes) {
        almacenes.distinctBy { it.id }.associateBy { it.id }
    }
    val productosDisponibles = remember(catalogoLista) {
        catalogoLista.filter { it.disponibleParaIngreso() }
    }

    suspend fun recargar() {
        if (companyRuc.isBlank()) return
        val cat = withContext(Dispatchers.IO) {
            CatalogRepository.listarParaGestion(companyRuc, token)
        }
        val ing = withContext(Dispatchers.IO) {
            InventarioRepository.listarIngresos(companyRuc, token)
        }
        val alm = withContext(Dispatchers.IO) {
            InventarioRepository.listarAlmacenes(companyRuc, token, todos = usuario?.esAdmin() == true)
        }
        withContext(Dispatchers.Main) {
            cat.onSuccess { catalogoLista = it }.onFailure { error = it.mensajeAuth() }
            ing.onSuccess { ingresos = it }.onFailure { error = it.mensajeAuth() }
            alm.onSuccess { lista -> almacenes = lista }
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
        try {
            recargar()
        } catch (e: Exception) {
            error = e.mensajeAuth()
        } finally {
            cargando = false
        }
    }

    val filtrados = remember(ingresos, busqueda, catalogoMap) {
        if (busqueda.isBlank()) ingresos
        else {
            val q = busqueda.trim().lowercase()
            ingresos.filter { mov ->
                mov.observaciones?.lowercase()?.contains(q) == true ||
                    mov.cliente?.razonSocial?.lowercase()?.contains(q) == true ||
                    mov.cliente?.numeroDoc?.lowercase()?.contains(q) == true ||
                    mov.lineasSeguras.any { linea ->
                        catalogoMap[linea.catalogItemId]?.nombre?.lowercase()?.contains(q) == true ||
                            linea.nombreEfectivo.lowercase().contains(q) ||
                            linea.catalogItemId.lowercase().contains(q) ||
                            linea.productoSerie?.numeroSerie?.lowercase()?.contains(q) == true ||
                            linea.numerosSerieUi.any { sn -> sn.lowercase().contains(q) }
                    }
            }
        }
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Ingresos",
        subtitulo = "Mercadería que entra a tu almacén",
        icono = Icons.Default.Input,
        onVolver = onVolver,
        floatingActionButton = {
            if (!cargando && error == null) {
                FloatingActionButton(
                    onClick = {
                        esDevolucionInicial = false
                        mostrarRegistrar = true
                    },
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
                                onNuevoIngreso = {
                                    esDevolucionInicial = false
                                    mostrarRegistrar = true
                                },
                                onDevolucionCliente = {
                                    esDevolucionInicial = true
                                    mostrarRegistrar = true
                                },
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
                            items(
                                filtrados,
                                key = { mov -> mov.id.ifBlank { "ingreso-${mov.hashCode()}" } },
                            ) { mov ->
                                IngresoCard(
                                    mov = mov,
                                    catalogo = catalogoMap,
                                    almacenes = almacenesMap,
                                    modifier = Modifier.padding(horizontal = 7.dp),
                                    onClick = { ingresoAcciones = mov },
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
        catalogoCompleto = catalogoLista,
        companyRuc = companyRuc,
        token = token,
        almacenes = almacenes,
        almacenIdDefault = usuario.almacenIdParaOperaciones(),
        almacenUsuarioId = usuario.almacenIdParaOperaciones(),
        esAdmin = usuario?.esAdmin() == true,
        esDevolucionInicial = esDevolucionInicial,
        onDismiss = { mostrarRegistrar = false },
        onRegistrar = { body ->
            scope.launch {
                registrando = true
                try {
                    val resultado = withContext(Dispatchers.IO) {
                        InventarioRepository.registrarEntrada(companyRuc, token, body)
                    }
                    resultado
                        .onSuccess { mov ->
                            recargar()
                            mostrarRegistrar = false
                            Toast.makeText(
                                context,
                                "Ingreso ${mov.numero ?: mov.id.take(8)} registrado",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        .onFailure {
                            Toast.makeText(
                                context,
                                it.mensajeAuth(),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        e.mensajeAuth(),
                        Toast.LENGTH_LONG,
                    ).show()
                } finally {
                    registrando = false
                }
            }
        },
    )

    LoadingOverlay(visible = registrando, message = "Registrando ingreso...")

    IngresoAccionesSheet(
        ingreso = ingresoAcciones,
        onDismiss = { ingresoAcciones = null },
        onMasOpciones = { ingreso ->
            ingresoAcciones = null
            ingresoDetalle = ingreso
        },
    )
    HistorialMovimientoDetalleSheet(
        movimiento = ingresoDetalle,
        catalogo = catalogoMap,
        almacenes = almacenesMap,
        onDismiss = { ingresoDetalle = null },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngresoAccionesSheet(
    ingreso: Movimiento?,
    onDismiss: () -> Unit,
    onMasOpciones: (Movimiento) -> Unit,
) {
    if (ingreso == null) return

    val esDevolucion = ingreso.referenciaTipo == "DEVOLUCION_CLIENTE"
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
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Input,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (esDevolucion) "Opciones de devolución" else "Opciones de ingreso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = C.primary,
                    )
                    Text(
                        ingreso.numeroDisplay,
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
                detalle = if (esDevolucion) {
                    "Ver todos los detalles de la devolución"
                } else {
                    "Ver todos los detalles del ingreso"
                },
                color = Color(0xFF6A1B9A),
                onClick = { onMasOpciones(ingreso) },
            )
        }
    }
}

@Composable
private fun IngresosAccionesRapidas(
    productosDisponibles: Int,
    onNuevoIngreso: () -> Unit,
    onDevolucionCliente: () -> Unit,
    onIrACatalogo: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                icono = Icons.Default.Replay,
                titulo = "Devolución",
                detalle = "Cliente devuelve al almacén",
                onClick = onDevolucionCliente,
            )
        }
        InventarioAccionCard(
            icono = Icons.Default.Inventory2,
            titulo = "Catálogo",
            detalle = "Gestionar productos",
            onClick = onIrACatalogo,
        )
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

private enum class ModoBusquedaUbicacion(val apiValue: String) {
    SERIE("serie"),
    NOMBRE("nombre"),
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

    var ubicaciones by remember { mutableStateOf<List<UbicacionProducto>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var modoBusqueda by remember { mutableStateOf(ModoBusquedaUbicacion.SERIE) }

    LaunchedEffect(companyRuc, token, busqueda, modoBusqueda) {
        error = null
        val q = busqueda.trim()
        val modo = modoBusqueda
        if (companyRuc.isBlank()) {
            error = "Sin empresa vinculada"
            ubicaciones = emptyList()
            return@LaunchedEffect
        }
        if (q.length < 2) {
            cargando = false
            ubicaciones = emptyList()
            return@LaunchedEffect
        }
        cargando = true
        delay(350)
        if (busqueda.trim() != q || modoBusqueda != modo) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            InventarioRepository.buscarUbicaciones(
                companyRuc = companyRuc,
                token = token,
                query = q,
                modo = modo.apiValue,
            )
                .onSuccess { lista -> ubicaciones = lista }
                .onFailure {
                    ubicaciones = emptyList()
                    error = it.mensajeAuth()
                }
        }
        cargando = false
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Historial",
        subtitulo = "Trazabilidad por ítem · entrada o salida",
        icono = Icons.Default.History,
        onVolver = onVolver,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                HistorialModoBusquedaCard(
                    modo = modoBusqueda,
                    onModo = { modoBusqueda = it },
                )
                Spacer(Modifier.height(10.dp))
                CatalogoBusquedaBar(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    totalItems = ubicaciones.size,
                    resultados = ubicaciones.size,
                    placeholder = when (modoBusqueda) {
                        ModoBusquedaUbicacion.SERIE -> "Buscar por número de serie…"
                        ModoBusquedaUbicacion.NOMBRE -> "Buscar por nombre de producto…"
                    },
                )
            }

            when {
                error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error!!, color = C.textSecondary)
                }
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = C.accent)
                }
                busqueda.trim().length < 2 -> Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Escribe al menos 2 caracteres para ver el historial del ítem",
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
                ubicaciones.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Sin movimientos para tu búsqueda",
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(ubicaciones, key = { it.id }) { item ->
                        HistorialItemCard(item = item)
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistorialModoBusquedaCard(
    modo: ModoBusquedaUbicacion,
    onModo: (ModoBusquedaUbicacion) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Buscar por",
                fontSize = 12.sp,
                color = C.textSecondary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HistorialModoChip(
                    label = "Número de serie",
                    selected = modo == ModoBusquedaUbicacion.SERIE,
                    onClick = { onModo(ModoBusquedaUbicacion.SERIE) },
                )
                HistorialModoChip(
                    label = "Nombre",
                    selected = modo == ModoBusquedaUbicacion.NOMBRE,
                    onClick = { onModo(ModoBusquedaUbicacion.NOMBRE) },
                )
            }
        }
    }
}

@Composable
private fun HistorialModoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = C.accent,
            selectedLabelColor = Color.White,
            containerColor = C.surfaceSoft,
            labelColor = C.textPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = C.border.copy(alpha = 0.5f),
            selectedBorderColor = C.accent,
        ),
    )
}

@Composable
private fun HistorialItemCard(
    item: UbicacionProducto,
    modifier: Modifier = Modifier,
) {
    val (icono, color) = when {
        item.tipoMovimiento == HistorialItemTipo.ENTRADA ->
            Icons.Default.Input to Color(0xFF2E7D32)
        item.esTraslado ->
            Icons.Default.Warehouse to Color(0xFF6A1B9A)
        else ->
            Icons.Default.LocalShipping to Color(0xFF1565C0)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(Modifier.size(40.dp), shape = CircleShape, color = color.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.catalogItemNombre,
                        fontWeight = FontWeight.Bold,
                        color = C.textPrimary,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.12f),
                    ) {
                        Text(
                            item.etiquetaTipo(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (!item.numeroSerie.isNullOrBlank()) {
                    Text(
                        "Serie ${item.numeroSerie}",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                    )
                } else {
                    Text(
                        item.etiquetaCantidad(),
                        fontSize = 13.sp,
                        color = C.textSecondary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    item.etiquetaRecorrido(),
                    fontSize = 14.sp,
                    color = C.textPrimary,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append(item.fechaHoraCompacto())
                        item.movimientoNumero?.takeIf { it.isNotBlank() }?.let { num ->
                            append(" · ")
                            append(num)
                        }
                    },
                    fontSize = 11.sp,
                    color = C.accent,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
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
    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = titulo,
        subtitulo = subtitulo,
        icono = icono,
        onVolver = onVolver,
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
    placeholder: String = "Buscar por nombre o tipo…",
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        CatalogoBusquedaBar(
            value = busqueda,
            onValueChange = onBusquedaChange,
            totalItems = total,
            resultados = resultados,
            placeholder = placeholder,
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
    val fecha = salida.fechaLegible()
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
    onClick: () -> Unit = {},
) {
    val almacen = almacenes[mov.almacenId]?.nombre ?: mov.almacenId
    val esDevolucion = mov.referenciaTipo == "DEVOLUCION_CLIENTE"
    val fecha = mov.fechaLegible()
    val lineasMov = mov.lineasSeguras
    val lineasPreview = lineasMov.take(2)
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
                            if (esDevolucion) "Devolución cliente" else "Ingreso",
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
                                if (esDevolucion) "DEVOLUCIÓN" else "ENTRADA",
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
                        text = buildString {
                            if (esDevolucion) {
                                val clienteNombre = mov.cliente?.razonSocial?.takeIf { it.isNotBlank() }
                                    ?: mov.cliente?.numeroDoc?.let { "Doc. $it" }
                                    ?: "Cliente"
                                append("Origen: $clienteNombre")
                                append(" · Destino: $almacen")
                            } else {
                                append("Destino: $almacen")
                            }
                        },
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
                    if (lineasMov.size > lineasPreview.size) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "+${lineasMov.size - lineasPreview.size} producto(s) más",
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
                        "${lineasMov.size} línea(s)",
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
