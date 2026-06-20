package com.factapp.jhonny.ui.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.usaSeriesInventario
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.ProductoSerieEstado
import com.factapp.jhonny.network.dto.request.RegistrarEntradaRequest
import com.factapp.jhonny.network.dto.request.RegistrarMovimientoLineaRequest
import com.factapp.jhonny.network.dto.model.aLineaCatalogoItem
import com.factapp.jhonny.network.dto.model.requireItem
import com.factapp.jhonny.network.dto.model.aRegistrarMovimientoLinea
import com.factapp.jhonny.network.dto.disponibleParaIngreso
import com.factapp.jhonny.network.dto.etiquetaStock
import com.factapp.jhonny.network.dto.model.lineasListasParaIngreso
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.ui.components.EmitFormSheetHeader
import com.factapp.jhonny.ui.components.PartialOptionCard
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import androidx.compose.material.icons.outlined.QrCodeScanner
import com.factapp.jhonny.ui.emitir.CatalogoBuscarSheet
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.factapp.jhonny.network.ClienteRepository
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.MovimientoCliente
import com.factapp.jhonny.network.dto.model.ProductoDevolucionCliente
import com.factapp.jhonny.ui.clientes.AgregarClienteSheet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.background
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

private enum class TipoIngreso {
    PROVEEDOR,
    DEVOLUCION_CLIENTE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarIngresoSheet(
    visible: Boolean,
    catalogoCompleto: List<CatalogItem>,
    companyRuc: String,
    token: String? = null,
    almacenes: List<Almacen>,
    almacenIdDefault: String? = null,
    almacenUsuarioId: String? = almacenIdDefault,
    esAdmin: Boolean = false,
    esDevolucionInicial: Boolean = false,
    onDismiss: () -> Unit,
    onRegistrar: (RegistrarEntradaRequest) -> Unit,
) {
    if (!visible) return

    val scope = rememberCoroutineScope()
    val contentScrollState = rememberScrollState()
    val lineas = remember { mutableStateListOf<LineaCatalogoItem>() }
    var observaciones by remember { mutableStateOf("") }
    var mostrarBuscar by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }
    var lineaMenuSerieId by remember { mutableStateOf<String?>(null) }
    var lineaEscaneoSerieId by remember { mutableStateOf<String?>(null) }
    var almacenSeleccionadoId by remember { mutableStateOf<String?>(null) }
    var tipoIngreso by remember {
        mutableStateOf(
            if (esDevolucionInicial) TipoIngreso.DEVOLUCION_CLIENTE else TipoIngreso.PROVEEDOR,
        )
    }
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var clienteNombre by remember { mutableStateOf("") }
    var clienteDoc by remember { mutableStateOf("") }
    var mostrarBuscarCliente by remember { mutableStateOf(false) }
    var mostrarAgregarCliente by remember { mutableStateOf(false) }
    var busquedaCliente by remember { mutableStateOf("") }
    var guardandoCliente by remember { mutableStateOf(false) }
    var errorAgregarCliente by remember { mutableStateOf<String?>(null) }
    var productosEntregados by remember { mutableStateOf<List<ProductoDevolucionCliente>>(emptyList()) }
    var cargandoEntregados by remember { mutableStateOf(false) }
    var lineaSeriesDevolucionId by remember { mutableStateOf<String?>(null) }

    val esDevolucion = tipoIngreso == TipoIngreso.DEVOLUCION_CLIENTE
    val clienteIdDevolucion = clienteSeleccionado?.id

    val productosBusqueda = remember(catalogoCompleto, tipoIngreso, productosEntregados) {
        when (tipoIngreso) {
            TipoIngreso.PROVEEDOR -> catalogoCompleto.filter { it.disponibleParaIngreso() }
            TipoIngreso.DEVOLUCION_CLIENTE -> productosEntregados.map { it.aCatalogItem() }
        }
    }
    val seriesEntregadasMap = remember(productosEntregados) {
        productosEntregados.associate { it.catalogItemId to it.series }
    }
    val cantidadPendienteMap = remember(productosEntregados) {
        productosEntregados.associate { it.catalogItemId to it.cantidadPendiente }
    }
    val almacenUsuarioEnLista = remember(almacenes, almacenUsuarioId) {
        almacenUsuarioId?.takeIf { id -> almacenes.any { it.id == id } }
    }
    val fijaAlmacenDevolucion = esDevolucion && !esAdmin && almacenUsuarioEnLista != null
    val almacenDestinoEfectivo = when {
        fijaAlmacenDevolucion -> almacenUsuarioEnLista
        else -> almacenSeleccionadoId
    }
    val almacenId = almacenDestinoEfectivo.orEmpty()
    val almacenNombre = almacenes.firstOrNull { it.id == almacenId }?.nombre
    val clienteValido = !esDevolucion || clienteSeleccionado != null || clienteDoc.isNotBlank()
    val puedeAgregarProducto = when {
        almacenDestinoEfectivo == null || almacenId.isBlank() -> false
        !esDevolucion -> clienteValido
        !clienteValido || clienteIdDevolucion.isNullOrBlank() -> false
        cargandoEntregados -> false
        else -> productosEntregados.isNotEmpty()
    }
    val puedeRegistrar = almacenDestinoEfectivo != null &&
        almacenId.isNotBlank() &&
        clienteValido &&
        lineas.lineasListasParaIngreso()

    suspend fun recargarClientes() {
        val resultado = withContext(Dispatchers.IO) {
            ClienteRepository.listar(companyRuc, token)
        }
        withContext(Dispatchers.Main) {
            resultado.onSuccess { clientes = it }
        }
    }

    LaunchedEffect(visible, almacenes, almacenIdDefault, almacenUsuarioId, esDevolucionInicial) {
        if (visible) {
            lineas.clear()
            observaciones = ""
            mostrarBuscar = false
            busqueda = ""
            lineaMenuSerieId = null
            lineaEscaneoSerieId = null
            lineaSeriesDevolucionId = null
            productosEntregados = emptyList()
            cargandoEntregados = false
            tipoIngreso = if (esDevolucionInicial) {
                TipoIngreso.DEVOLUCION_CLIENTE
            } else {
                TipoIngreso.PROVEEDOR
            }
            clienteSeleccionado = null
            clienteNombre = ""
            clienteDoc = ""
            mostrarBuscarCliente = false
            mostrarAgregarCliente = false
            busquedaCliente = ""
            val almacenUsuario = almacenUsuarioId?.takeIf { id -> almacenes.any { it.id == id } }
            almacenSeleccionadoId = when {
                esDevolucionInicial && !esAdmin && almacenUsuario != null -> almacenUsuario
                else ->
                    almacenIdDefault
                        ?.takeIf { id -> almacenes.any { it.id == id } }
                        ?: almacenes.singleOrNull()?.id
            }
            if (companyRuc.isNotBlank()) {
                recargarClientes()
            }
        }
    }

    LaunchedEffect(esDevolucion, fijaAlmacenDevolucion, almacenUsuarioEnLista) {
        if (esDevolucion && fijaAlmacenDevolucion) {
            almacenSeleccionadoId = almacenUsuarioEnLista
        }
    }

    LaunchedEffect(esDevolucion, clienteIdDevolucion, companyRuc, token, visible) {
        if (!visible || !esDevolucion || clienteIdDevolucion.isNullOrBlank()) {
            productosEntregados = emptyList()
            cargandoEntregados = false
            return@LaunchedEffect
        }
        cargandoEntregados = true
        val resultado = withContext(Dispatchers.IO) {
            InventarioRepository.listarProductosDevolucion(companyRuc, token, clienteIdDevolucion)
        }
        productosEntregados = resultado.getOrElse { emptyList() }
        lineas.clear()
        cargandoEntregados = false
    }

    BackHandler(onBack = onDismiss)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            EmitFormSheetHeader(
                titulo = if (esDevolucion) "Devolución de cliente" else "Nuevo ingreso",
                subtitulo = when {
                    esDevolucion && clienteSeleccionado != null ->
                        "Entrada a ${almacenNombre ?: "almacén"} · ${clienteSeleccionado!!.razonSocial}"
                    esDevolucion -> "Mercadería que el cliente devuelve al almacén"
                    almacenNombre != null -> "Entrada a $almacenNombre"
                    else -> "Mercadería que entra al almacén"
                },
                icono = Icons.Default.Warehouse,
                onVolver = onDismiss,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(contentScrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Tipo de ingreso",
                    fontWeight = FontWeight.SemiBold,
                    color = C.primary,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IngresoFilterChip(
                        label = "Proveedor / compra",
                        selected = tipoIngreso == TipoIngreso.PROVEEDOR,
                        onClick = {
                            if (tipoIngreso != TipoIngreso.PROVEEDOR) {
                                tipoIngreso = TipoIngreso.PROVEEDOR
                                lineas.clear()
                                clienteSeleccionado = null
                                clienteDoc = ""
                                clienteNombre = ""
                            }
                        },
                    )
                    IngresoFilterChip(
                        label = "Devolución cliente",
                        selected = esDevolucion,
                        onClick = {
                            if (!esDevolucion) {
                                tipoIngreso = TipoIngreso.DEVOLUCION_CLIENTE
                                lineas.clear()
                            }
                        },
                    )
                }

                if (esDevolucion) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Cliente que devuelve",
                        fontWeight = FontWeight.SemiBold,
                        color = C.primary,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            busquedaCliente = ""
                            mostrarBuscarCliente = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, C.accent),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = C.accent)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Buscar, filtrar o agregar cliente",
                            color = C.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    when {
                        clienteSeleccionado != null -> {
                            val cli = clienteSeleccionado!!
                            Spacer(Modifier.height(8.dp))
                            IngresoClienteCard(
                                nombre = cli.razonSocial,
                                documento = cli.etiquetaDocumento,
                                onQuitar = {
                                    clienteSeleccionado = null
                                    clienteDoc = ""
                                    clienteNombre = ""
                                    lineas.clear()
                                },
                            )
                        }
                        clienteDoc.isNotBlank() -> {
                            Spacer(Modifier.height(8.dp))
                            IngresoClienteCard(
                                nombre = clienteNombre.ifBlank { "Cliente manual" },
                                documento = clienteDoc,
                                onQuitar = {
                                    clienteDoc = ""
                                    clienteNombre = ""
                                    lineas.clear()
                                },
                            )
                        }
                        else -> {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Indica qué cliente devuelve la mercadería.",
                                fontSize = 13.sp,
                                color = C.textSecondary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (fijaAlmacenDevolucion) {
                    AlmacenAsignadoSection(
                        titulo = "Almacén de destino",
                        subtitulo = "La mercadería devuelta ingresa a tu almacén asignado",
                        almacen = almacenes.find { it.id == almacenUsuarioEnLista },
                        sinAlmacenMensaje = "Tu usuario no tiene almacén asignado. Contacta al administrador.",
                    )
                } else {
                    AlmacenSelectorSection(
                        titulo = "Almacén de destino",
                        subtitulo = if (esDevolucion) {
                            "Obligatorio — ¿a qué bodega entra la mercadería devuelta?"
                        } else {
                            "Obligatorio — ¿a qué bodega entra la mercadería?"
                        },
                        almacenes = almacenes,
                        seleccionadoId = almacenSeleccionadoId,
                        onSeleccionar = { id ->
                            if (id != almacenSeleccionadoId) {
                                almacenSeleccionadoId = id
                                lineas.clear()
                            }
                        },
                        vacioMensaje = "Crea un almacén en Más opciones → Almacenes",
                    )
                }

                if (!fijaAlmacenDevolucion && almacenes.size > 1 && almacenSeleccionadoId == null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Selecciona el almacén de destino para continuar.",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { mostrarBuscar = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = puedeAgregarProducto,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, C.accent),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = C.accent)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar producto del catálogo", color = C.accent, fontWeight = FontWeight.SemiBold)
                }

                if (esDevolucion && cargandoEntregados) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = C.accent,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Cargando productos entregados al cliente…", fontSize = 13.sp, color = C.textSecondary)
                    }
                } else if (!puedeAgregarProducto) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            esDevolucion && clienteIdDevolucion.isNullOrBlank() ->
                                "Elige un cliente registrado para ver qué productos puede devolver."
                            esDevolucion && productosEntregados.isEmpty() ->
                                "Este cliente no tiene productos entregados pendientes de devolución."
                            esDevolucion && !clienteValido ->
                                "Selecciona el cliente que devuelve antes de agregar productos."
                            almacenDestinoEfectivo == null ->
                                "Selecciona el almacén de destino antes de agregar productos."
                            else -> "Completa los datos requeridos para agregar productos."
                        },
                        fontSize = 13.sp,
                        color = C.textSecondary,
                    )
                } else if (esDevolucion) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${productosEntregados.size} producto(s) entregado(s) a este cliente. En serializados, elige las series a devolver.",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                        lineHeight = 18.sp,
                    )
                }

                if (lineas.isEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (esDevolucion) {
                            "Agrega los productos que el cliente está devolviendo."
                        } else {
                            "Elige productos activos con inventario. Si no hay ninguno, revisa el catálogo."
                        },
                        fontSize = 13.sp,
                        color = C.textSecondary,
                        lineHeight = 18.sp,
                    )
                } else {
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(220.dp),
                    ) {
                        items(lineas, key = { it.requireItem().id }) { linea ->
                            LineaIngresoEditor(
                                linea = linea,
                                esDevolucion = esDevolucion,
                                maxCantidadDevolucion = cantidadPendienteMap[linea.requireItem().id],
                                onCantidadChange = { nueva ->
                                    val idx = lineas.indexOfFirst { it.requireItem().id == linea.requireItem().id }
                                    if (idx >= 0) {
                                        lineas[idx] = if (linea.requireItem().usaSeriesInventario) {
                                            lineas[idx]
                                        } else {
                                            val max = cantidadPendienteMap[linea.requireItem().id]
                                            val cantidad = if (esDevolucion && max != null) {
                                                nueva.coerceIn(0.0, max)
                                            } else {
                                                nueva
                                            }
                                            lineas[idx].copy(cantidad = cantidad)
                                        }
                                    }
                                },
                                onAbrirMenuSerial = { lineaMenuSerieId = linea.requireItem().id },
                                onSeleccionarSeriesDevolucion = {
                                    lineaSeriesDevolucionId = linea.requireItem().id
                                },
                                onEliminar = {
                                    lineas.removeAll { it.requireItem().id == linea.requireItem().id }
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Observaciones (opcional)") },
                    placeholder = {
                        Text(
                            if (esDevolucion) "Ej. Devolución por cambio, garantía…"
                            else "Ej. Compra proveedor, nota de ingreso",
                        )
                    },
                    singleLine = false,
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors(),
                )

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val clienteId = if (esDevolucion) clienteSeleccionado?.id else null
                        val clienteManual = if (esDevolucion && clienteId == null && clienteDoc.isNotBlank()) {
                            MovimientoCliente(
                                tipoDoc = if (clienteDoc.length == 11) "6" else "1",
                                numeroDoc = clienteDoc,
                                razonSocial = clienteNombre.takeIf { it.isNotBlank() },
                            )
                        } else {
                            null
                        }
                        val request = RegistrarEntradaRequest(
                            companyRuc = companyRuc,
                            almacenId = almacenId,
                            lineas = lineas.map { it.aRegistrarMovimientoLinea() },
                            observaciones = observaciones.takeIf { it.isNotBlank() },
                            clienteId = clienteId,
                            cliente = clienteManual,
                        )
                        onRegistrar(request)
                    },
                    enabled = puedeRegistrar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.accent),
                ) {
                    Text(
                        if (esDevolucion) "Registrar devolución" else "Registrar ingreso",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    CatalogoBuscarSheet(
        visible = mostrarBuscar,
        items = productosBusqueda,
        busqueda = busqueda,
        onBusquedaChange = { busqueda = it },
        onDismiss = {
            mostrarBuscar = false
            busqueda = ""
        },
        onItemSeleccionado = { item ->
            if (lineas.none { it.requireItem().id == item.id }) {
                lineas += item.aLineaCatalogoItem(
                    cantidad = if (item.usaSeriesInventario) 0.0 else 1.0,
                    almacenId = almacenId,
                )
            }
            mostrarBuscar = false
            busqueda = ""
        },
    )

    if (!esDevolucion) {
        val lineaMenuSerie = lineas.firstOrNull { it.requireItem().id == lineaMenuSerieId }
        IngresoSeriesActionSheet(
            item = lineaMenuSerie?.catalogItem,
            visible = lineaMenuSerie != null,
            onDismiss = { lineaMenuSerieId = null },
            onIngresoMasivo = {
                lineaMenuSerieId = null
                lineaEscaneoSerieId = it.id
            },
        )

        val lineaEscaneoSerie = lineas.firstOrNull { it.requireItem().id == lineaEscaneoSerieId }
        EscaneoMasivoSeriesSheet(
            visible = lineaEscaneoSerie != null,
            companyRuc = companyRuc,
            catalogItem = lineaEscaneoSerie?.catalogItem,
            seriesIniciales = lineaEscaneoSerie?.series.orEmpty(),
            onDismiss = { lineaEscaneoSerieId = null },
            onConfirmar = { series ->
                val idx = lineas.indexOfFirst { it.requireItem().id == lineaEscaneoSerie?.requireItem()?.id }
                if (idx >= 0) {
                    lineas[idx] = lineas[idx].copy(
                        series = series,
                        numerosSerie = series.map { it.numeroSerie },
                        almacenId = almacenId,
                        cantidad = series.size.toDouble(),
                    )
                }
                lineaEscaneoSerieId = null
            },
        )
    }

    val lineaSeriesDevolucion = lineas.firstOrNull { it.requireItem().id == lineaSeriesDevolucionId }
    DevolucionSeriesSheet(
        visible = lineaSeriesDevolucion != null,
        catalogItem = lineaSeriesDevolucion?.catalogItem,
        seriesEntregadas = seriesEntregadasMap[lineaSeriesDevolucion?.requireItem()?.id].orEmpty(),
        seriesIniciales = lineaSeriesDevolucion?.series.orEmpty(),
        onDismiss = { lineaSeriesDevolucionId = null },
        onConfirmar = { series ->
            val catalogId = lineaSeriesDevolucion?.requireItem()?.id ?: return@DevolucionSeriesSheet
            val idx = lineas.indexOfFirst { it.requireItem().id == catalogId }
            if (idx >= 0) {
                lineas[idx] = lineas[idx].copy(
                    series = series,
                    numerosSerie = series.map { it.numeroSerie },
                    almacenId = almacenId,
                    cantidad = series.size.toDouble(),
                )
            }
            lineaSeriesDevolucionId = null
        },
    )

    SalidaClienteBuscarSheet(
        visible = mostrarBuscarCliente,
        clientes = clientes,
        busqueda = busquedaCliente,
        onBusquedaChange = { busquedaCliente = it },
        onDismiss = {
            mostrarBuscarCliente = false
            busquedaCliente = ""
        },
        onClienteSeleccionado = { cli ->
            clienteSeleccionado = cli
            clienteNombre = cli.razonSocial
            clienteDoc = cli.numeroDoc
            lineas.clear()
            mostrarBuscarCliente = false
            busquedaCliente = ""
        },
        onNuevoCliente = {
            mostrarBuscarCliente = false
            errorAgregarCliente = null
            mostrarAgregarCliente = true
        },
    )

    AgregarClienteSheet(
        visible = mostrarAgregarCliente,
        guardando = guardandoCliente,
        error = errorAgregarCliente,
        onDismiss = {
            if (!guardandoCliente) {
                mostrarAgregarCliente = false
                errorAgregarCliente = null
            }
        },
        onGuardar = { body ->
            guardandoCliente = true
            errorAgregarCliente = null
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    ClienteRepository.crear(companyRuc, token, body)
                }
                guardandoCliente = false
                result.onSuccess { cli ->
                    mostrarAgregarCliente = false
                    withContext(Dispatchers.IO) { recargarClientes() }
                    clienteSeleccionado = cli
                    clienteNombre = cli.razonSocial
                    clienteDoc = cli.numeroDoc
                    lineas.clear()
                }.onFailure {
                    errorAgregarCliente = it.message ?: "No se pudo registrar el cliente"
                }
            }
        },
    )
}

@Composable
private fun IngresoFilterChip(
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
private fun IngresoClienteCard(
    nombre: String,
    documento: String,
    onQuitar: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = C.accentSoft,
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = C.accent)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold, color = C.primary)
                Text(documento, fontSize = 13.sp, color = C.accent)
            }
            TextButton(onClick = onQuitar) {
                Text("Quitar", color = C.textSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevolucionSeriesSheet(
    visible: Boolean,
    catalogItem: CatalogItem?,
    seriesEntregadas: List<ProductoSerie>,
    seriesIniciales: List<ProductoSerie>,
    onDismiss: () -> Unit,
    onConfirmar: (List<ProductoSerie>) -> Unit,
) {
    if (!visible || catalogItem == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val seleccionadas = remember(catalogItem.id) { mutableStateListOf<String>() }
    var busquedaSeries by remember(catalogItem.id) { mutableStateOf("") }

    val seriesFiltradas = remember(seriesEntregadas, busquedaSeries) {
        if (busquedaSeries.isBlank()) seriesEntregadas
        else {
            val q = busquedaSeries.trim()
            seriesEntregadas.filter { it.numeroSerie.contains(q, ignoreCase = true) }
        }
    }

    LaunchedEffect(catalogItem.id, visible) {
        if (!visible) return@LaunchedEffect
        seleccionadas.clear()
        seleccionadas.addAll(seriesIniciales.map { it.id })
        busquedaSeries = ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EmitFormSheetHeader(
                titulo = "Series entregadas",
                subtitulo = catalogItem.nombre,
                mostrarDragHandle = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(C.background)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = busquedaSeries,
                    onValueChange = { busquedaSeries = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filtrar por número de serie") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = C.accent) },
                    trailingIcon = {
                        if (busquedaSeries.isNotEmpty()) {
                            IconButton(onClick = { busquedaSeries = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = C.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors(),
                )
                Spacer(Modifier.height(10.dp))
                if (seriesEntregadas.isEmpty()) {
                    Text(
                        "No hay series pendientes de devolución para este producto.",
                        color = C.textSecondary,
                        fontSize = 13.sp,
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.height(240.dp),
                    ) {
                        items(seriesFiltradas, key = { it.id }) { serie ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (seleccionadas.contains(serie.id)) seleccionadas.remove(serie.id)
                                        else seleccionadas.add(serie.id)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (seleccionadas.contains(serie.id)) C.accentSoft else C.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (seleccionadas.contains(serie.id)) C.accent else C.border.copy(alpha = 0.35f),
                                ),
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = seleccionadas.contains(serie.id),
                                        onCheckedChange = { checked ->
                                            if (checked) seleccionadas.add(serie.id)
                                            else seleccionadas.remove(serie.id)
                                        },
                                    )
                                    Text(serie.numeroSerie, color = C.textPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val elegidas = seriesEntregadas.filter { seleccionadas.contains(it.id) }
                        onConfirmar(elegidas)
                    },
                    enabled = seleccionadas.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.accent),
                ) {
                    Text("Confirmar (${seleccionadas.size})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LineaIngresoEditor(
    linea: LineaCatalogoItem,
    esDevolucion: Boolean = false,
    maxCantidadDevolucion: Double? = null,
    onCantidadChange: (Double) -> Unit,
    onAbrirMenuSerial: () -> Unit,
    onSeleccionarSeriesDevolucion: () -> Unit = {},
    onEliminar: () -> Unit,
) {
    val item = linea.requireItem()
    val onSerieClick = if (esDevolucion) onSeleccionarSeriesDevolucion else onAbrirMenuSerial

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.usaSeriesInventario) Modifier.clickable(onClick = onSerieClick)
                else Modifier,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.5f)),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.nombre, fontWeight = FontWeight.SemiBold, color = C.textPrimary, fontSize = 14.sp)

                Text(
                    text = buildString {
                        item.codigo?.let { append("$it · ") }
                        if (item.usaSeriesInventario) {
                            append("${linea.series.size} series · ")
                            append(if (esDevolucion) "Elegir series entregadas" else "Producto serializado")
                        } else {
                            if (esDevolucion && maxCantidadDevolucion != null) {
                                append("Pendiente: ${maxCantidadDevolucion.toInt()} · ")
                            } else {
                                item.etiquetaStock()?.let { append("$it · ") }
                            }
                            append(if (esDevolucion) "Cantidad a devolver" else "Cantidad a ingresar")
                        }
                    },
                    fontSize = 12.sp,
                    color = C.textSecondary,
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            if (item.usaSeriesInventario) {
                if (!esDevolucion) {
                    IconButton(onClick = onAbrirMenuSerial, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "Ingreso masivo de series", tint = C.accent)
                    }
                }
            } else {
                OutlinedTextField(

                    value = linea.cantidad.takeIf { it > 0 }?.let {
                        if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                    }.orEmpty(),
                    onValueChange = { txt ->
                        val v = txt.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onCantidadChange(v)
                    },
                    modifier = Modifier.width(60.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp),
                    colors = outlinedFieldColors(),
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = C.textSecondary)
            }
        }


    }
}

@Composable
private fun IngresoSeriesActionSheet(
    item: CatalogItem?,
    visible: Boolean,
    onDismiss: () -> Unit,
    onIngresoMasivo: (CatalogItem) -> Unit,
) {
    if (!visible || item == null) return

    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = "Series del producto",
        subtitle = item.nombre,
        theme = PartialSheetTheme.Emit,
    ) {
        PartialOptionCard(
            icon = Icons.Outlined.QrCodeScanner,
            titulo = "Ingreso masivo de series",
            detalle = "Escanea o pega números de serie en lote",
            onClick = {
                onIngresoMasivo(item)
                onDismiss()
            },
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EscaneoMasivoSeriesSheet(
    visible: Boolean,
    companyRuc: String,
    catalogItem: CatalogItem?,
    seriesIniciales: List<ProductoSerie>,
    onDismiss: () -> Unit,
    onConfirmar: (List<ProductoSerie>) -> Unit,
) {
    if (!visible || catalogItem == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var input by remember(catalogItem.id) { mutableStateOf("") }
    val series = remember(catalogItem.id) { mutableStateListOf<ProductoSerie>() }

    LaunchedEffect(catalogItem.id, visible) {
        if (visible) {
            series.clear()
            series.addAll(seriesIniciales)
            input = ""
        }
    }

    fun agregarDesdeTexto(raw: String) {
        val existentes = series.map { it.numeroSerie.lowercase() }.toMutableSet()
        val nuevas = raw.split("\n", "\r", ",", ";", "\t", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { existentes.add(it.lowercase()) }
            .mapIndexed { index, numero ->
                ProductoSerie(
                    id = "scan-${System.currentTimeMillis()}-$index",
                    companyRuc = companyRuc,
                    catalogItemId = catalogItem.id,
                    numeroSerie = numero,
                    estado = ProductoSerieEstado.DISPONIBLE,
                )
            }
        if (nuevas.isNotEmpty()) series.addAll(nuevas)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
        modifier = Modifier.imePadding(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EmitFormSheetHeader(
                titulo = "Escaneo masivo",
                subtitulo = catalogItem.nombre,
                mostrarDragHandle = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
            Text(
                text = "Escanea con pistola (Enter) o pega lote de series.",
                color = C.textSecondary,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { nuevo ->
                    if ('\n' in nuevo || '\r' in nuevo) {
                        agregarDesdeTexto(nuevo)
                        input = ""
                    } else {
                        input = nuevo
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Serie / lote de series") },
                placeholder = { Text("Ej. DL-SN-2026-00489") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = outlinedFieldColors(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        agregarDesdeTexto(input)
                        input = ""
                    },
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar")
                }
                TextButton(onClick = { series.clear() }) {
                    Text("Limpiar", color = C.accent)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Series capturadas: ${series.size}",
                color = C.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = C.surface.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, C.border.copy(alpha = 0.35f)),
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .height(160.dp)
                        .padding(8.dp),
                ) {
                    items(series, key = { it.id }) { serie ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = C.surface.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, C.border.copy(alpha = 0.3f)),
                        ) {
                            Text(
                                text = serie.numeroSerie,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                color = C.textPrimary,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { onConfirmar(series.toList()) },
                enabled = series.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = C.accent),
            ) {
                Text("Confirmar series", fontWeight = FontWeight.Bold)
            }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = C.borderFocused,
    unfocusedBorderColor = C.border,
    focusedContainerColor = C.surface,
    unfocusedContainerColor = C.surface,
)
