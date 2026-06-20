package com.factapp.jhonny.ui.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.modelos.esAdmin
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.ClienteRepository
import com.factapp.jhonny.network.InventarioRepository
import com.factapp.jhonny.network.mensajeAuth
import com.factapp.jhonny.network.dto.usaSeriesInventario
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.MovimientoCliente
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.request.RegistrarSalidaRequest
import com.factapp.jhonny.network.dto.model.aRegistrarSalidaLineas
import com.factapp.jhonny.network.dto.model.aLineaCatalogoItem
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.almacenIdParaOperaciones
import com.factapp.jhonny.network.dto.disponibleParaSalida
import com.factapp.jhonny.network.dto.model.lineasListasParaSalida
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.requireItem
import com.factapp.jhonny.network.dto.formatCantidadConUnidad
import com.factapp.jhonny.network.dto.hayStockPara
import com.factapp.jhonny.network.dto.stockDisponible
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.sheetContentWithoutTopInset
import com.factapp.jhonny.ui.emitir.CatalogoBuscarSheet
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

private enum class TipoDestinoSalida {
    CLIENTE,
    TRASLADO,
}

private enum class FiltroSeriesLista {
    TODAS,
    SELECCIONADAS,
}

@Composable
fun RegistrarSalidaScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    clienteInicial: Cliente? = null,
    onVolver: () -> Unit = {},
    onRegistrada: () -> Unit = {},
    onNuevoCliente: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val esAdmin = usuario?.esAdmin() == true
    val almacenUsuarioId = usuario.almacenIdParaOperaciones()

    var cargandoDatos by remember { mutableStateOf(true) }
    var cargandoCatalogoAlmacen by remember { mutableStateOf(false) }
    var catalogoLista by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var almacenes by remember { mutableStateOf<List<Almacen>>(emptyList()) }
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }

    val lineas = remember { mutableStateListOf<LineaCatalogoItem>() }
    var tipoDestino by remember { mutableStateOf<TipoDestinoSalida?>(null) }
    var almacenOrigenId by remember { mutableStateOf<String?>(null) }
    var almacenDestinoId by remember { mutableStateOf<String?>(null) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var clienteNombre by remember { mutableStateOf("") }
    var clienteDoc by remember { mutableStateOf("") }
    var comprobanteId by remember { mutableStateOf("") }
    var guiaRemisionId by remember { mutableStateOf("") }
    var mostrarBuscarProducto by remember { mutableStateOf(false) }
    var mostrarBuscarCliente by remember { mutableStateOf(false) }
    var busquedaProducto by remember { mutableStateOf("") }
    var busquedaCliente by remember { mutableStateOf("") }
    var lineaSeriesId by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var clienteInicialAplicado by remember(clienteInicial?.id) { mutableStateOf(false) }

    val productosDisponibles = remember(catalogoLista) {
        catalogoLista.filter { it.disponibleParaSalida() }
    }

    val almacenProcedenciaEfectivo = when (tipoDestino) {
        TipoDestinoSalida.TRASLADO, TipoDestinoSalida.CLIENTE -> when {
            !esAdmin -> almacenUsuarioId?.takeIf { id -> almacenes.any { it.id == id } }
            else ->
                almacenOrigenId
                    ?: almacenUsuarioId?.takeIf { id -> almacenes.any { it.id == id } }
                    ?: almacenes.firstOrNull()?.id
        }
        null -> null
    }
    val almacenesDestinoTraslado = remember(almacenes, almacenProcedenciaEfectivo) {
        almacenes.filter { it.id != almacenProcedenciaEfectivo }
    }
    val almacenProcedenciaUsuario = remember(almacenes, almacenUsuarioId) {
        almacenes.find { it.id == almacenUsuarioId }
    }
    val puedeAgregarProducto = !almacenProcedenciaEfectivo.isNullOrBlank() && !cargandoCatalogoAlmacen
    val tipoElegido = tipoDestino != null

    val destinoValido = when (tipoDestino) {
        TipoDestinoSalida.CLIENTE ->
            clienteSeleccionado != null || clienteDoc.isNotBlank()
        TipoDestinoSalida.TRASLADO ->
            !almacenDestinoId.isNullOrBlank() &&
                almacenDestinoId != almacenProcedenciaEfectivo
        null -> false
    }

    val puedeRegistrar = tipoElegido &&
        destinoValido &&
        !almacenProcedenciaEfectivo.isNullOrBlank() &&
        !guardando &&
        lineas.lineasListasParaSalida()

    LaunchedEffect(companyRuc, token) {
        if (companyRuc.isBlank()) {
            cargandoDatos = false
            return@LaunchedEffect
        }
        cargandoDatos = true
        withContext(Dispatchers.IO) {
            InventarioRepository.listarAlmacenes(companyRuc, token, todos = true).onSuccess { lista ->
                almacenes = lista
            }
            ClienteRepository.listar(companyRuc, token).onSuccess { clientes = it }
        }
        cargandoDatos = false
    }

    LaunchedEffect(cargandoDatos, clienteInicial, clientes) {
        if (cargandoDatos || clienteInicial == null || clienteInicialAplicado) return@LaunchedEffect
        tipoDestino = TipoDestinoSalida.CLIENTE
        clienteSeleccionado = clientes.find { it.id == clienteInicial.id } ?: clienteInicial
        clienteNombre = clienteInicial.razonSocial
        clienteDoc = clienteInicial.numeroDoc
        if (!esAdmin) {
            almacenOrigenId = almacenUsuarioId
        }
        clienteInicialAplicado = true
    }

    LaunchedEffect(companyRuc, token, tipoDestino, almacenProcedenciaEfectivo) {
        if (companyRuc.isBlank() || tipoDestino == null) {
            catalogoLista = emptyList()
            return@LaunchedEffect
        }
        val almacenId = almacenProcedenciaEfectivo
        if (almacenId.isNullOrBlank()) {
            catalogoLista = emptyList()
            return@LaunchedEffect
        }
        cargandoCatalogoAlmacen = true
        withContext(Dispatchers.IO) {
            CatalogRepository.listarPorAlmacen(companyRuc, token, almacenId)
                .onSuccess { catalogoLista = it }
        }
        cargandoCatalogoAlmacen = false
    }

    fun seleccionarTipo(tipo: TipoDestinoSalida) {
        if (tipoDestino == tipo) return
        tipoDestino = tipo
        almacenDestinoId = null
        clienteSeleccionado = null
        clienteNombre = ""
        clienteDoc = ""
        almacenOrigenId = if (esAdmin) null else almacenUsuarioId
        catalogoLista = emptyList()
        lineas.clear()
    }

    suspend fun recargarClientes() {
        ClienteRepository.listar(companyRuc, token).onSuccess { clientes = it }
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Nueva salida",
        subtitulo = "Despacho de mercadería",
        icono = Icons.Default.LocalShipping,
        onVolver = onVolver,
    ) { padding ->
        if (cargandoDatos) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = C.accent)
            }
        } else {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                    Text(
                        "Tipo de destino",
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
                        SalidaFilterChip(
                            label = "Cliente",
                            selected = tipoDestino == TipoDestinoSalida.CLIENTE,
                            onClick = { seleccionarTipo(TipoDestinoSalida.CLIENTE) },
                        )
                        SalidaFilterChip(
                            label = "Traslado interno",
                            selected = tipoDestino == TipoDestinoSalida.TRASLADO,
                            onClick = { seleccionarTipo(TipoDestinoSalida.TRASLADO) },
                        )
                    }

                    when (val destino = tipoDestino) {
                        null -> {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Elige si despachas a un cliente o trasladas entre almacenes de tu empresa.",
                                fontSize = 13.sp,
                                color = C.textSecondary,
                                lineHeight = 18.sp,
                            )
                        }
                        TipoDestinoSalida.TRASLADO -> {
                            Spacer(Modifier.height(20.dp))
                            if (esAdmin) {
                                val procedencias = almacenes.filter { it.id != almacenDestinoId }
                                SeccionAlmacenes(
                                    titulo = "Almacén de procedencia",
                                    subtitulo = "Opcional — si no eliges, se usa tu almacén asignado",
                                    almacenes = procedencias,
                                    seleccionadoId = almacenOrigenId,
                                    onSeleccionar = { id ->
                                        almacenOrigenId = id
                                        if (almacenDestinoId == id) almacenDestinoId = null
                                        lineas.clear()
                                    },
                                    vacioMensaje = "Configura al menos un almacén de origen",
                                    opcional = true,
                                )
                            } else {
                                AlmacenAsignadoFijo(
                                    titulo = "Almacén de procedencia",
                                    subtitulo = "Tu almacén asignado",
                                    almacen = almacenProcedenciaUsuario,
                                    sinAlmacenMensaje = "Tu usuario no tiene almacén asignado. Contacta al administrador.",
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            SeccionAlmacenes(
                                titulo = "Almacén de destino",
                                subtitulo = "Obligatorio — hacia dónde va el stock",
                                almacenes = almacenesDestinoTraslado,
                                seleccionadoId = almacenDestinoId,
                                onSeleccionar = { id ->
                                    almacenDestinoId = id
                                    if (esAdmin && almacenOrigenId == id) almacenOrigenId = null
                                    lineas.clear()
                                },
                            )
                        }
                        TipoDestinoSalida.CLIENTE -> {
                            Spacer(Modifier.height(20.dp))
                            if (esAdmin) {
                                SeccionAlmacenes(
                                    titulo = "Almacén de procedencia",
                                    subtitulo = "Opcional — bodega de donde sale el stock",
                                    almacenes = almacenes,
                                    seleccionadoId = almacenOrigenId,
                                    onSeleccionar = { id ->
                                        almacenOrigenId = id
                                        lineas.clear()
                                    },
                                    opcional = true,
                                )
                            } else {
                                AlmacenAsignadoFijo(
                                    titulo = "Almacén de procedencia",
                                    subtitulo = "Tu almacén asignado",
                                    almacen = almacenProcedenciaUsuario,
                                    sinAlmacenMensaje = "Tu usuario no tiene almacén asignado. Contacta al administrador.",
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Cliente destino",
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
                                    ClienteSeleccionadoCard(
                                        nombre = cli.razonSocial,
                                        documento = cli.etiquetaDocumento,
                                        onQuitar = {
                                            clienteSeleccionado = null
                                            clienteNombre = ""
                                            clienteDoc = ""
                                        },
                                    )
                                }
                                clienteDoc.isNotBlank() -> {
                                    Spacer(Modifier.height(8.dp))
                                    ClienteSeleccionadoCard(
                                        nombre = clienteNombre.ifBlank { "Cliente manual" },
                                        documento = clienteDoc,
                                        onQuitar = {
                                            clienteNombre = ""
                                            clienteDoc = ""
                                        },
                                    )
                                }
                            }
                        }
                    }

                    if (tipoDestino != null) {
                        Spacer(Modifier.height(16.dp))
                        SalidaCampoTexto(
                            value = comprobanteId,
                            onValueChange = { comprobanteId = it },
                            label = "Factura / boleta (opcional)",
                            placeholder = "ID del comprobante de venta",
                        )
                        Spacer(Modifier.height(12.dp))
                        SalidaCampoTexto(
                            value = guiaRemisionId,
                            onValueChange = { guiaRemisionId = it },
                            label = "Guía de remisión (opcional)",
                            placeholder = "ID de la guía emitida",
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Productos",
                            fontWeight = FontWeight.SemiBold,
                            color = C.primary,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { mostrarBuscarProducto = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = puedeAgregarProducto,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, C.accent),
                        ) {
                            if (cargandoCatalogoAlmacen) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = C.accent,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null, tint = C.accent)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar producto", color = C.accent, fontWeight = FontWeight.SemiBold)
                        }

                        if (!puedeAgregarProducto && tipoDestino == TipoDestinoSalida.TRASLADO && almacenDestinoId.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Elige primero el almacén de destino.",
                                fontSize = 13.sp,
                                color = C.textSecondary,
                            )
                        } else if (!puedeAgregarProducto) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Configura un almacén de procedencia o asigna uno al usuario.",
                                fontSize = 13.sp,
                                color = C.textSecondary,
                            )
                        } else if (puedeAgregarProducto && productosDisponibles.isEmpty() && !cargandoCatalogoAlmacen) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No hay productos con stock en el almacén seleccionado.",
                                fontSize = 13.sp,
                                color = C.textSecondary,
                            )
                        } else if (lineas.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (tipoDestino == TipoDestinoSalida.TRASLADO) {
                                    "Agrega al menos un producto con stock en el almacén de origen."
                                } else {
                                    "Agrega al menos un producto con stock disponible."
                                },
                                fontSize = 13.sp,
                                color = C.textSecondary,
                            )
                        } else {
                            Spacer(Modifier.height(10.dp))
                            lineas.forEach { linea ->
                                LineaSalidaEditor(
                                    linea = linea,
                                    onCantidadChange = { nueva ->
                                        val idx = lineas.indexOfFirst { it.requireItem().id == linea.requireItem().id }
                                        if (idx >= 0 && !linea.requireItem().usaSeriesInventario) {
                                            val max = linea.requireItem().stockDisponible
                                            lineas[idx] = lineas[idx].copy(cantidad = nueva.coerceIn(0.0, max))
                                        }
                                    },
                                    onAbrirSeries = { lineaSeriesId = linea.requireItem().id },
                                    onEliminar = {
                                        lineas.removeAll { it.requireItem().id == linea.requireItem().id }
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                if (tipoElegido) {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val tipo = tipoDestino ?: return@Button
                            val clienteId = when (tipo) {
                                TipoDestinoSalida.CLIENTE -> clienteSeleccionado?.id
                                TipoDestinoSalida.TRASLADO -> null
                            }
                            val clienteManual = when (tipo) {
                                TipoDestinoSalida.CLIENTE -> {
                                    if (clienteId != null) {
                                        null
                                    } else if (clienteDoc.isNotBlank()) {
                                        MovimientoCliente(
                                            tipoDoc = if (clienteDoc.length == 11) "6" else "1",
                                            numeroDoc = clienteDoc,
                                            razonSocial = clienteNombre.takeIf { it.isNotBlank() },
                                        )
                                    } else {
                                        null
                                    }
                                }
                                TipoDestinoSalida.TRASLADO -> null
                            }
                            val request = RegistrarSalidaRequest(
                                companyRuc = companyRuc,
                                almacenId = requireNotNull(almacenProcedenciaEfectivo),
                                almacenDestinoId = if (tipo == TipoDestinoSalida.TRASLADO) {
                                    almacenDestinoId
                                } else {
                                    null
                                },
                                comprobanteId = comprobanteId.takeIf { it.isNotBlank() },
                                guiaRemisionId = guiaRemisionId.takeIf { it.isNotBlank() },
                                lineas = lineas.aRegistrarSalidaLineas(),
                                clienteId = clienteId,
                                cliente = clienteManual,
                            )
                            guardando = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    InventarioRepository.registrarSalida(companyRuc, token, request)
                                }
                                guardando = false
                                result.fold(
                                    onSuccess = {
                                        onRegistrada()
                                        onVolver()
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(
                                            context,
                                            error.mensajeAuth(),
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    },
                                )
                            }
                        },
                        enabled = puedeRegistrar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = C.accent),
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = C.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Registrar despacho", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    CatalogoBuscarSheet(
        visible = mostrarBuscarProducto,
        items = productosDisponibles,
        busqueda = busquedaProducto,
        onBusquedaChange = { busquedaProducto = it },
        onDismiss = {
            mostrarBuscarProducto = false
            busquedaProducto = ""
        },
        onItemSeleccionado = { item ->
            if (lineas.none { it.requireItem().id == item.id }) {
                lineas += item.aLineaCatalogoItem(
                    cantidad = if (item.usaSeriesInventario) 0.0 else 1.0,
                    almacenId = almacenProcedenciaEfectivo,
                )
            }
            mostrarBuscarProducto = false
            busquedaProducto = ""
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
            mostrarBuscarCliente = false
            busquedaCliente = ""
        },
        onNuevoCliente = {
            mostrarBuscarCliente = false
            busquedaCliente = ""
            onNuevoCliente()
        },
    )

    val lineaSeries = lineas.firstOrNull { it.requireItem().id == lineaSeriesId }
    SalidaSeleccionSeriesSheet(
        visible = lineaSeries != null,
        companyRuc = companyRuc,
        almacenId = almacenProcedenciaEfectivo.orEmpty(),
        token = token,
        catalogItem = lineaSeries?.catalogItem,
        seriesIniciales = lineaSeries?.series.orEmpty(),
        onDismiss = { lineaSeriesId = null },
        onConfirmar = { seleccionadas ->
            val catalogId = lineaSeries?.catalogItem?.id ?: return@SalidaSeleccionSeriesSheet
            val idx = lineas.indexOfFirst { it.requireItem().id == catalogId }
            if (idx >= 0) {
                lineas[idx] = lineas[idx].copy(
                    series = seleccionadas,
                    numerosSerie = emptyList(),
                    almacenId = almacenProcedenciaEfectivo ?: seleccionadas.firstOrNull()?.almacenId,
                    cantidad = seleccionadas.size.toDouble(),
                )
            }
            lineaSeriesId = null
        },
    )
}

@Composable
private fun AlmacenAsignadoFijo(
    titulo: String,
    subtitulo: String,
    almacen: Almacen?,
    sinAlmacenMensaje: String,
) {
    Text(titulo, fontWeight = FontWeight.SemiBold, color = C.primary, fontSize = 14.sp)
    Text(subtitulo, fontSize = 12.sp, color = C.textSecondary)
    Spacer(Modifier.height(8.dp))
    if (almacen == null) {
        Text(sinAlmacenMensaje, fontSize = 13.sp, color = Color(0xFFC62828))
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = C.accentSoft,
        ) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = C.accent,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(almacen.nombre, fontWeight = FontWeight.Bold, color = C.primary)
                    if (almacen.codigo.isNotBlank()) {
                        Text(almacen.codigo, fontSize = 12.sp, color = C.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionAlmacenes(
    titulo: String,
    subtitulo: String,
    almacenes: List<Almacen>,
    seleccionadoId: String?,
    onSeleccionar: (String) -> Unit,
    vacioMensaje: String = "No hay almacenes configurados",
    opcional: Boolean = false,
) {
    Text(titulo, fontWeight = FontWeight.SemiBold, color = C.primary, fontSize = 14.sp)
    Text(
        if (opcional) "$subtitulo (opcional)" else subtitulo,
        fontSize = 12.sp,
        color = C.textSecondary,
    )
    Spacer(Modifier.height(8.dp))
    if (almacenes.isEmpty()) {
        Text(vacioMensaje, fontSize = 13.sp, color = C.textSecondary)
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            almacenes.forEach { alm ->
                SalidaFilterChip(
                    label = alm.nombre,
                    selected = seleccionadoId == alm.id,
                    onClick = { onSeleccionar(alm.id) },
                )
            }
        }
    }
}

@Composable
private fun ClienteSeleccionadoCard(
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
            Column(Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold, color = C.primary)
                Text(documento, fontSize = 13.sp, color = C.accent)
            }
            IconButton(onClick = onQuitar) {
                Icon(Icons.Default.Close, contentDescription = "Quitar", tint = C.textSecondary)
            }
        }
    }
}

@Composable
private fun SalidaFilterChip(
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
private fun LineaSalidaEditor(
    linea: LineaCatalogoItem,
    onCantidadChange: (Double) -> Unit,
    onAbrirSeries: () -> Unit,
    onEliminar: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (linea.requireItem().usaSeriesInventario) Modifier.clickable(onClick = onAbrirSeries)
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
                Text(
                    linea.requireItem().nombre,
                    fontWeight = FontWeight.SemiBold,
                    color = C.textPrimary,
                    fontSize = 14.sp,
                )
                Text(
                    buildString {
                        linea.requireItem().codigo?.let { append("$it · ") }
                        if (linea.requireItem().usaSeriesInventario) {
                            append("${linea.series.size} series · ")
                        } else {
                            append(
                                "Stock: ${
                                    formatCantidadConUnidad(
                                        linea.requireItem().stockDisponible,
                                        linea.requireItem().unidad,
                                    )
                                } · ",
                            )
                        }
                        append(if (linea.requireItem().usaSeriesInventario) "Elegir series" else "Cantidad")
                    },
                    fontSize = 12.sp,
                    color = C.textSecondary,
                )
            }
            if (linea.requireItem().usaSeriesInventario) {
                IconButton(onClick = onAbrirSeries, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Elegir series", tint = C.accent)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = salidaFieldColors(),
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = C.textSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalidaSeleccionSeriesSheet(
    visible: Boolean,
    companyRuc: String,
    almacenId: String,
    token: String?,
    catalogItem: CatalogItem?,
    seriesIniciales: List<ProductoSerie>,
    onDismiss: () -> Unit,
    onConfirmar: (List<ProductoSerie>) -> Unit,
) {
    if (!visible || catalogItem == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var cargando by remember(catalogItem.id) { mutableStateOf(true) }
    var disponibles by remember(catalogItem.id) { mutableStateOf<List<ProductoSerie>>(emptyList()) }
    val seleccionadas = remember(catalogItem.id) { mutableStateListOf<String>() }
    var busquedaSeries by remember(catalogItem.id) { mutableStateOf("") }
    var filtroLista by remember(catalogItem.id) { mutableStateOf(FiltroSeriesLista.TODAS) }

    val idsSeleccionados = seleccionadas.toList()
    val seriesFiltradas = remember(disponibles, busquedaSeries, filtroLista, idsSeleccionados) {
        disponibles.filter { serie ->
            val coincideBusqueda = busquedaSeries.isBlank() ||
                serie.numeroSerie.contains(busquedaSeries.trim(), ignoreCase = true)
            val coincideFiltro = when (filtroLista) {
                FiltroSeriesLista.TODAS -> true
                FiltroSeriesLista.SELECCIONADAS -> seleccionadas.contains(serie.id)
            }
            coincideBusqueda && coincideFiltro
        }
    }

    LaunchedEffect(catalogItem.id, visible) {
        if (!visible) return@LaunchedEffect
        cargando = true
        busquedaSeries = ""
        filtroLista = FiltroSeriesLista.TODAS
        seleccionadas.clear()
        seleccionadas.addAll(seriesIniciales.map { it.id })
        disponibles = withContext(Dispatchers.IO) {
            InventarioRepository.listarSeriesDisponibles(
                companyRuc = companyRuc,
                catalogItemId = catalogItem.id,
                token = token,
                almacenId = almacenId,
            ).getOrElse { emptyList() }
        }
        cargando = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = C.background,
        contentWindowInsets = { sheetContentWithoutTopInset() },
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ComprobanteEmitHeader(
                titulo = "Series disponibles",
                subtitulo = catalogItem.nombre,
                onVolver = onDismiss,
                mostrarDragHandle = true,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(C.background)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = busquedaSeries,
                    onValueChange = { busquedaSeries = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filtrar por número de serie") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = C.accent)
                    },
                    trailingIcon = {
                        if (busquedaSeries.isNotEmpty()) {
                            IconButton(onClick = { busquedaSeries = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = C.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = salidaFieldColors(),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SalidaFilterChip(
                        label = "Todas (${disponibles.size})",
                        selected = filtroLista == FiltroSeriesLista.TODAS,
                        onClick = { filtroLista = FiltroSeriesLista.TODAS },
                    )
                    SalidaFilterChip(
                        label = "Seleccionadas (${seleccionadas.size})",
                        selected = filtroLista == FiltroSeriesLista.SELECCIONADAS,
                        onClick = { filtroLista = FiltroSeriesLista.SELECCIONADAS },
                    )
                }
                if (busquedaSeries.isNotBlank() || filtroLista == FiltroSeriesLista.SELECCIONADAS) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (seriesFiltradas.isEmpty()) {
                            "Sin coincidencias"
                        } else {
                            "${seriesFiltradas.size} de ${disponibles.size} series"
                        },
                        fontSize = 12.sp,
                        color = if (seriesFiltradas.isEmpty()) C.textSecondary else C.accent,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(Modifier.height(10.dp))
                when {
                    cargando -> Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = C.accent)
                    }
                    disponibles.isEmpty() -> Text(
                        "No hay series en este almacén.",
                        color = C.textSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    seriesFiltradas.isEmpty() -> Text(
                        "Ninguna serie coincide con el filtro.",
                        color = C.textSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.height(220.dp),
                    ) {
                        items(seriesFiltradas, key = { it.id }) { serie ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (seleccionadas.contains(serie.id)) {
                                            seleccionadas.remove(serie.id)
                                        } else {
                                            seleccionadas.add(serie.id)
                                        }
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
                        onConfirmar(disponibles.filter { seleccionadas.contains(it.id) })
                    },
                    enabled = seleccionadas.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.accent),
                ) {
                    Text("Confirmar ${seleccionadas.size} serie(s)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SalidaCampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focus ->
                if (focus.isFocused) {
                    scope.launch {
                        delay(150)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        label = { Text(label) },
        placeholder = placeholder?.let { hint -> { Text(hint) } },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = salidaFieldColors(),
    )
}

@Composable
private fun salidaFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = C.borderFocused,
    unfocusedBorderColor = C.border,
    focusedContainerColor = C.surface,
    unfocusedContainerColor = C.surface,
)
