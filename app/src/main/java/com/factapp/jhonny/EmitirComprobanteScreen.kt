package com.factapp.jhonny

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedButton
import com.factapp.jhonny.demo.ComprobanteAfectadoDemo
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.ClienteRepository
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.aEmitirLineas
import com.factapp.jhonny.network.dto.model.actualizarCantidad
import com.factapp.jhonny.network.dto.model.actualizarSerieEnIndice
import com.factapp.jhonny.network.dto.model.agregarDesdeCatalogo
import com.factapp.jhonny.network.dto.model.calcularTotales
import com.factapp.jhonny.network.dto.model.descripcionEnComprobante
import com.factapp.jhonny.network.dto.model.eliminarLinea
import com.factapp.jhonny.network.dto.model.receptorParaEmitir
import com.factapp.jhonny.network.dto.hayStockPara
import com.factapp.jhonny.network.dto.model.lineasListasParaEmitir
import com.factapp.jhonny.network.dto.manejaInventario
import com.factapp.jhonny.network.dto.model.requireItem
import com.factapp.jhonny.network.dto.model.requiereSeries
import com.factapp.jhonny.network.dto.model.seriesValidas
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.request.EmitirComprobanteRequest
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.emitir.CatalogoBuscarSheet
import com.factapp.jhonny.ui.inventario.SalidaClienteBuscarSheet
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import com.factapp.jhonny.ui.theme.EasyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmitirComprobanteScreen(
    tipo: TipoComprobante,
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
    onEmitir: (TipoComprobante) -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val scope = rememberCoroutineScope()
    val companyRuc = usuario.companyRucParaCatalogo()
    val token = usuario?.token

    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var docReceptor by remember { mutableStateOf("") }
    var nombreReceptor by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var mostrarBuscarCliente by remember { mutableStateOf(false) }
    var busquedaCliente by remember { mutableStateOf("") }
    var docReferencia by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var facturaEtiqueta by remember { mutableStateOf("") }
    var facturasVinculadas by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var guardando by remember { mutableStateOf(false) }

    var catalogo by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var errorCatalogo by remember { mutableStateOf<String?>(null) }
    var cargandoCatalogo by remember { mutableStateOf(false) }
    var lineas by remember { mutableStateOf<List<LineaCatalogoItem>>(emptyList()) }
    var mostrarBuscarCatalogo by remember { mutableStateOf(false) }
    var busquedaCatalogo by remember { mutableStateOf("") }

    LaunchedEffect(companyRuc, token, tipo) {
        if (companyRuc.isNullOrBlank()) return@LaunchedEffect
        if (tipo == TipoComprobante.BOLETA) {
            withContext(Dispatchers.IO) {
                ClienteRepository.listar(companyRuc, token).onSuccess { clientes = it }
            }
        }
    }

    LaunchedEffect(companyRuc, token) {
        if (companyRuc.isNullOrBlank()) return@LaunchedEffect
        cargandoCatalogo = true
        errorCatalogo = null
        withContext(Dispatchers.IO) {
            CatalogRepository.listarPorEmpresa(companyRuc, token)
                .onSuccess { catalogo = it }
                .onFailure { errorCatalogo = it.message ?: "No se pudo cargar el catálogo" }
        }
        cargandoCatalogo = false
    }

    val esBoleta = tipo == TipoComprobante.BOLETA
    val esGuiaEmision = tipo.esGuiaEmision
    val esNotaCredito = tipo == TipoComprobante.NOTA_CREDITO
    val documentoAfectado = remember(docReferencia) { Invoice.fromEtiqueta(docReferencia.trim()) }
    val docReferenciaValida = documentoAfectado != null ||
        ComprobanteAfectadoDemo.referenciaEsValida(docReferencia)
    val puedeAgregarDesdeCatalogo =
        !esGuiaEmision && usuario != null && (!esNotaCredito || docReferenciaValida)

    LaunchedEffect(tipo, docReferencia, catalogo) {
        if (!esNotaCredito) return@LaunchedEffect
        if (!docReferenciaValida || catalogo.isEmpty()) {
            lineas = emptyList()
            return@LaunchedEffect
        }
        lineas = ComprobanteAfectadoDemo.lineasParaNotaCredito(docReferencia, catalogo)
    }

    val totales = lineas.calcularTotales()

    val receptor = remember(esBoleta, clienteSeleccionado, docReceptor, nombreReceptor) {
        receptorParaEmitir(
            esBoleta = esBoleta,
            cliente = clienteSeleccionado,
            docManual = docReceptor,
            nombreManual = nombreReceptor,
        )
    }
    val receptorValido = receptor != null

    val labelDocCliente = when (tipo) {
        TipoComprobante.FACTURA -> "RUC del cliente"
        TipoComprobante.BOLETA -> "DNI del cliente"
        TipoComprobante.GUIA_EMISION -> "RUC / DNI del destinatario"
        TipoComprobante.NOTA_CREDITO, TipoComprobante.NOTA_DEBITO -> "RUC / DNI del cliente"
    }
    val labelNombre = when (tipo) {
        TipoComprobante.BOLETA -> "Nombre completo"
        TipoComprobante.GUIA_EMISION -> "Razón social o nombre del destinatario"
        else -> "Razón social o nombre"
    }

    val puedeEmitir = !guardando && when {
        esGuiaEmision ->
            facturasVinculadas.isNotEmpty() && receptorValido
        esNotaCredito ->
            lineas.isNotEmpty() &&
                lineas.lineasListasParaEmitir() &&
                docReferenciaValida &&
                motivo.isNotBlank() &&
                receptorValido
        else -> lineas.isNotEmpty() && lineas.lineasListasParaEmitir() && receptorValido
    }

    fun emitirComprobante() {
        val ruc = companyRuc.orEmpty()
        val receptorEmitir = receptor ?: return
        val request = EmitirComprobanteRequest(
            companyRuc = ruc,
            tipo = tipo.tipoApi,
            receptor = receptorEmitir,
            lineas = if (esGuiaEmision) emptyList() else lineas.aEmitirLineas(),
            documentoAfectado = if (tipo.esNota) documentoAfectado else null,
            facturas = facturasVinculadas.takeIf { esGuiaEmision },
            motivoNota = motivo.takeIf { it.isNotBlank() },
            observaciones = observaciones.takeIf { it.isNotBlank() },
        )
        guardando = true
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                ComprobanteRepository.emitir(ruc, token, request).isSuccess
            }
            guardando = false
            if (ok) onEmitir(tipo)
        }
    }

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
            docReceptor = ""
            nombreReceptor = ""
            mostrarBuscarCliente = false
            busquedaCliente = ""
        },
    )

    CatalogoBuscarSheet(
        visible = mostrarBuscarCatalogo,
        items = catalogo,
        busqueda = busquedaCatalogo,
        onBusquedaChange = { busquedaCatalogo = it },
        onDismiss = {
            mostrarBuscarCatalogo = false
            busquedaCatalogo = ""
        },
        onItemSeleccionado = { item ->
            lineas = lineas.agregarDesdeCatalogo(item)
        },
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = tipo.titulo,
                subtitulo = "Serie ${tipo.serie} · SUNAT",
                detalle = buildString {
                    append(tipo.detalle)
                    usuario?.company?.nombre?.let { nombre ->
                        append("\n")
                        append(nombre)
                    }
                },
                icono = tipo.icono(),
                onVolver = onVolver,
            )
        },
        bottomBar = {
            EmitDockInferior(
                puedeEmitir = puedeEmitir,
                tipo = tipo,
                subtotal = totales.subtotal,
                igv = totales.igv,
                total = totales.total,
                mostrarTotales = !esGuiaEmision,
                onEmitir = { emitirComprobante() },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EmitTipoMetaCard(tipo = tipo, cantidadLineas = lineas.size)

            EmitSeccionCard(
                icono = Icons.Default.Person,
                titulo = "Datos del receptor",
                subtitulo = if (esBoleta) {
                    "Cliente registrado o consumidor final"
                } else {
                    "Cliente o destinatario del comprobante"
                },
            ) {
                if (esBoleta) {
                    OutlinedButton(
                        onClick = { mostrarBuscarCliente = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, C.accent),
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = C.accent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Buscar cliente registrado",
                            color = C.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    clienteSeleccionado?.let { cli ->
                        Spacer(modifier = Modifier.height(10.dp))
                        EmitClienteSeleccionadoCard(
                            nombre = cli.razonSocial,
                            documento = cli.etiquetaDocumento,
                            onQuitar = {
                                clienteSeleccionado = null
                                docReceptor = ""
                                nombreReceptor = ""
                            },
                        )
                    } ?: run {
                        Spacer(modifier = Modifier.height(10.dp))
                        EmitCampo(
                            value = docReceptor,
                            onValueChange = { docReceptor = it.filter { c -> c.isDigit() } },
                            label = labelDocCliente,
                            placeholder = "Ej. 45678901",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        EmitCampo(
                            value = nombreReceptor,
                            onValueChange = { nombreReceptor = it },
                            label = labelNombre,
                            placeholder = "Nombre o razón social",
                        )
                    }
                } else {
                    EmitCampo(
                        value = docReceptor,
                        onValueChange = { docReceptor = it.filter { c -> c.isDigit() } },
                        label = labelDocCliente,
                        placeholder = "Ej. 20123456789",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    EmitCampo(
                        value = nombreReceptor,
                        onValueChange = { nombreReceptor = it },
                        label = labelNombre,
                        placeholder = "Nombre o razón social",
                    )
                }
            }

            if (esGuiaEmision) {
                EmitSeccionCard(
                    icono = Icons.Default.ReceiptLong,
                    titulo = "Facturas vinculadas",
                    subtitulo = "Al menos una factura amparada por la guía",
                ) {
                    EmitCampo(
                        value = facturaEtiqueta,
                        onValueChange = { facturaEtiqueta = it },
                        label = "Serie y número",
                        placeholder = "F001-00001234",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        onClick = {
                            val ref = Invoice.fromEtiqueta(facturaEtiqueta) ?: return@Surface
                            if (facturasVinculadas.none { it.etiqueta == ref.etiqueta }) {
                                facturasVinculadas = facturasVinculadas + ref
                            }
                            facturaEtiqueta = ""
                        },
                        enabled = Invoice.fromEtiqueta(facturaEtiqueta) != null,
                        shape = RoundedCornerShape(12.dp),
                        color = C.accentSoft,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Add, null, tint = C.accent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Agregar factura", color = C.accent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (facturasVinculadas.isEmpty()) {
                        Text(
                            text = "La guía debe incluir al menos una factura electrónica.",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        facturasVinculadas.forEach { ref ->
                            FacturaVinculadaCard(
                                referencia = ref,
                                onEliminar = {
                                    facturasVinculadas = facturasVinculadas.filter { it.etiqueta != ref.etiqueta }
                                },
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            if (tipo.esNota) {
                EmitSeccionCard(
                    icono = Icons.AutoMirrored.Filled.Undo,
                    titulo = "Documento afectado",
                    subtitulo = "Comprobante original y motivo",
                ) {
                    EmitCampo(
                        value = docReferencia,
                        onValueChange = { docReferencia = it },
                        label = "Serie y número",
                        placeholder = "F001-00001234",
                    )
                    if (esNotaCredito) {
                        Text(
                            text = if (docReferenciaValida) {
                                "Ítems cargados de $docReferencia. Ajusta cantidades a acreditar."
                            } else {
                                "Ingresa el comprobante afectado para cargar sus ítems."
                            },
                            fontSize = 12.sp,
                            color = if (docReferenciaValida) C.accent else C.textSecondary,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EmitCampo(
                        value = motivo,
                        onValueChange = { motivo = it },
                        label = if (tipo == TipoComprobante.NOTA_CREDITO) {
                            "Motivo de la nota de crédito"
                        } else {
                            "Motivo de la nota de débito"
                        },
                        placeholder = "Describe el motivo del ajuste",
                        singleLine = false,
                        minLines = 2,
                    )
                }
            }

            if (!esGuiaEmision) {
                EmitSeccionCard(
                    icono = Icons.Default.ShoppingCart,
                    titulo = if (esNotaCredito) "Ítems a acreditar" else "Detalle de venta",
                    subtitulo = "${lineas.size} línea(s) · catálogo",
                    trailing = {
                        if (lineas.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = C.accentSoft,
                            ) {
                                Text(
                                    text = formatearSoles(totales.subtotal),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = C.accent,
                                )
                            }
                        }
                    },
                ) {
                    if (lineas.isEmpty()) {
                        ItemsVacioCard(
                            titulo = when {
                                esNotaCredito && !docReferenciaValida ->
                                    "Sin ítems a acreditar"
                                else -> "Tu comprobante está vacío"
                            },
                            subtitulo = when {
                                esNotaCredito && !docReferenciaValida ->
                                    "Primero indica el documento afectado"
                                esNotaCredito ->
                                    "Esperando líneas del comprobante o catálogo"
                                else ->
                                    "Agrega productos o servicios del catálogo"
                            },
                        )
                    } else {
                        lineas.forEach { linea ->
                            LineaItemCard(
                                linea = linea,
                                onCantidadChange = { nueva ->
                                    lineas = lineas.actualizarCantidad(linea.lineaId, nueva)
                                },
                                onSerieChange = { indice, valor ->
                                    lineas = lineas.actualizarSerieEnIndice(linea.lineaId, indice, valor)
                                },
                                onEliminar = {
                                    lineas = lineas.eliminarLinea(linea.lineaId)
                                },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (puedeAgregarDesdeCatalogo) {
                        Spacer(modifier = Modifier.height(4.dp))
                        EmitBotonAgregarCatalogo(
                            texto = if (esNotaCredito) {
                                "Agregar ítem extra del catálogo"
                            } else {
                                "Buscar en catálogo y agregar"
                            },
                            onClick = { mostrarBuscarCatalogo = true },
                        )
                    }

                    when {
                        usuario == null -> EmitAvisoCatalogo("Inicia sesión para usar el catálogo")
                        cargandoCatalogo -> EmitAvisoCatalogo("Cargando catálogo del servidor…")
                        errorCatalogo != null -> EmitAvisoCatalogo(errorCatalogo!!)
                    }
                }
            }

            EmitSeccionCard(
                icono = Icons.AutoMirrored.Filled.Notes,
                titulo = "Observaciones",
                subtitulo = "Opcional · visible en el PDF",
            ) {
                EmitCampo(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = "Notas adicionales",
                    placeholder = "Condiciones, referencias internas…",
                    singleLine = false,
                    minLines = 2,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun EmitTipoMetaCard(tipo: TipoComprobante, cantidadLineas: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(C.primaryDeep, C.primary, C.accent),
                    ),
                    shape = RoundedCornerShape(18.dp),
                )
                .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.15f),
                ) {
                    Icon(
                        imageVector = tipo.icono(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Serie ${tipo.serie}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = tipo.titulo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                    if (cantidadLineas > 0) {
                        Text(
                            text = "$cantidadLineas ítem(s) en el detalle",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.6f)),
                ) {
                    Text(
                        text = "SUNAT",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color(0xFFE8F5E9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmitSeccionCard(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(C.surfaceSoft)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = C.accentSoft,
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = C.accent,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = C.textPrimary,
                    )
                    Text(
                        text = subtitulo,
                        fontSize = 12.sp,
                        color = C.textSecondary,
                    )
                }
                trailing?.invoke()
            }
            HorizontalDivider(color = C.border.copy(alpha = 0.35f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun EmitClienteSeleccionadoCard(
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
private fun EmitCampo(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = C.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder, color = C.textSecondary.copy(alpha = 0.55f), fontSize = 14.sp)
                }
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = C.borderFocused,
                unfocusedBorderColor = C.border.copy(alpha = 0.5f),
                focusedContainerColor = C.surfaceSoft,
                unfocusedContainerColor = C.surfaceSoft,
                cursorColor = C.accent,
                focusedTextColor = C.textPrimary,
                unfocusedTextColor = C.textPrimary,
            ),
        )
    }
}

@Composable
private fun EmitBotonAgregarCatalogo(
    texto: String,
    onClick: () -> Unit,
) {
    val dashColor = C.accent.copy(alpha = 0.55f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
                    ),
                )
            }
            .clickable(onClick = onClick)
            .background(C.accentSoft.copy(alpha = 0.35f))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = C.accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(10.dp))
            Text(texto, color = C.accent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun EmitAvisoCatalogo(texto: String) {
    Text(
        text = texto,
        fontSize = 12.sp,
        color = C.textSecondary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun EmitDockInferior(
    puedeEmitir: Boolean,
    tipo: TipoComprobante,
    subtotal: Double,
    igv: Double,
    total: Double,
    mostrarTotales: Boolean,
    onEmitir: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = C.surface,
        shadowElevation = 12.dp,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            if (mostrarTotales && total > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Subtotal", fontSize = 11.sp, color = C.textSecondary)
                        Text(
                            formatearSoles(subtotal),
                            fontSize = 13.sp,
                            color = C.textPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("IGV 18%", fontSize = 11.sp, color = C.textSecondary)
                        Text(
                            formatearSoles(igv),
                            fontSize = 13.sp,
                            color = C.textPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total", fontSize = 11.sp, color = C.textSecondary)
                        Text(
                            formatearSoles(total),
                            fontSize = 20.sp,
                            color = C.accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Button(
                onClick = onEmitir,
                enabled = puedeEmitir,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = C.primary,
                    contentColor = C.onPrimary,
                    disabledContainerColor = C.border.copy(alpha = 0.5f),
                    disabledContentColor = C.textSecondary,
                ),
            ) {
                Icon(
                    imageVector = tipo.icono(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Emitir ${tipo.titulo.lowercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun LineaItemCard(
    linea: LineaCatalogoItem,
    onCantidadChange: (Double) -> Unit,
    onSerieChange: (indice: Int, valor: String) -> Unit,
    onEliminar: () -> Unit,
) {
    val item = linea.requireItem()
    val conSerie = linea.requiereSeries
    var cantidadTexto by remember(linea.lineaId, linea.cantidad) {
        mutableStateOf(formatCantidad(linea.cantidad))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(C.surfaceSoft)
            .border(1.dp, C.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(5.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(C.accent, C.accentBright)),
                ),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.descripcionEnComprobante(),
                        fontWeight = FontWeight.Bold,
                        color = C.textPrimary,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = C.accentSoft,
                        ) {
                            Text(
                                text = item.tipo.etiqueta,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = C.accent,
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "${formatearSoles(item.precioUnitario)} c/u",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                        )
                    }
                }
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Default.Delete, "Quitar", tint = C.textSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (conSerie) {
                Text(
                    text = "Números de serie",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = C.textSecondary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                linea.numerosSerieUi.forEachIndexed { indice, sn ->
                    OutlinedTextField(
                        value = sn,
                        onValueChange = { onSerieChange(indice, it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Serie ${indice + 1}", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = C.borderFocused,
                            unfocusedBorderColor = C.border.copy(alpha = 0.5f),
                            focusedContainerColor = C.surface,
                            unfocusedContainerColor = C.surface,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                EmitStepperCantidad(
                    cantidad = linea.cantidad,
                    puedeIncrementar = item.hayStockPara(linea.cantidad + 1.0),
                    onMenos = {
                        val nueva = (linea.cantidad - 1.0).coerceAtLeast(0.0)
                        if (nueva <= 0) onEliminar() else onCantidadChange(nueva)
                    },
                    onMas = {
                        if (item.hayStockPara(linea.cantidad + 1.0)) {
                            onCantidadChange(linea.cantidad + 1.0)
                        }
                    },
                )
                if (!linea.seriesValidas()) {
                    Text(
                        text = "Completa todas las series",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Cantidad", fontSize = 13.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
                    EmitStepperCantidad(
                        cantidad = linea.cantidad,
                        cantidadTexto = cantidadTexto,
                        onCantidadTextoChange = { texto ->
                            val filtrado = texto.filter { c -> c.isDigit() || c == '.' }
                            cantidadTexto = filtrado
                            filtrado.toDoubleOrNull()?.let { onCantidadChange(it) }
                        },
                        puedeIncrementar = !item.manejaInventario || item.hayStockPara(linea.cantidad + 1.0),
                        onMenos = {
                            val nueva = (linea.cantidad - 1.0).coerceAtLeast(0.0)
                            if (nueva <= 0) onEliminar()
                            else {
                                cantidadTexto = formatCantidad(nueva)
                                onCantidadChange(nueva)
                            }
                        },
                        onMas = {
                            val nueva = linea.cantidad + 1.0
                            if (item.manejaInventario && !item.hayStockPara(nueva)) return@EmitStepperCantidad
                            cantidadTexto = formatCantidad(nueva)
                            onCantidadChange(nueva)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Subtotal", fontSize = 12.sp, color = C.textSecondary)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = formatearSoles(linea.subtotal),
                    fontWeight = FontWeight.Bold,
                    color = C.primary,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun EmitStepperCantidad(
    cantidad: Double,
    cantidadTexto: String? = null,
    onCantidadTextoChange: ((String) -> Unit)? = null,
    puedeIncrementar: Boolean,
    onMenos: () -> Unit,
    onMas: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = C.surface,
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenos, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Remove, "Menos", tint = C.accent, modifier = Modifier.size(18.dp))
            }
            if (cantidadTexto != null && onCantidadTextoChange != null) {
                CantidadInputCompact(value = cantidadTexto, onValueChange = onCantidadTextoChange)
            } else {
                Text(
                    text = formatCantidad(cantidad),
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = C.textPrimary,
                    fontSize = 15.sp,
                )
            }
            IconButton(
                onClick = onMas,
                modifier = Modifier.size(36.dp),
                enabled = puedeIncrementar,
            ) {
                Icon(Icons.Default.Add, "Más", tint = C.accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CantidadInputCompact(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.width(44.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.labelLarge.copy(
            color = C.textPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorBrush = SolidColor(C.accent),
    )
}

private fun formatCantidad(cantidad: Double): String =
    if (cantidad == cantidad.toLong().toDouble()) {
        cantidad.toLong().toString()
    } else {
        "%.2f".format(cantidad)
    }

private fun TipoComprobante.icono(): ImageVector = when (this) {
    TipoComprobante.FACTURA -> Icons.Default.Receipt
    TipoComprobante.BOLETA -> Icons.Default.Description
    TipoComprobante.NOTA_CREDITO -> Icons.AutoMirrored.Filled.Undo
    TipoComprobante.NOTA_DEBITO -> Icons.Default.Add
    TipoComprobante.GUIA_EMISION -> Icons.Default.LocalShipping
}

@Composable
private fun FacturaVinculadaCard(
    referencia: Invoice,
    onEliminar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(C.surfaceSoft)
            .border(1.dp, C.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Receipt, null, tint = C.accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(10.dp))
            Column {
                Text(referencia.etiqueta, fontWeight = FontWeight.Bold, color = C.textPrimary)
                Text(referencia.tipo, fontSize = 12.sp, color = C.textSecondary)
            }
        }
        IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Quitar", tint = C.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ItemsVacioCard(
    titulo: String,
    subtitulo: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(C.surfaceSoft)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(shape = CircleShape, color = C.accentSoft) {
            Icon(
                Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = C.accent,
                modifier = Modifier
                    .padding(14.dp)
                    .size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(titulo, color = C.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitulo,
            color = C.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmitirComprobanteScreenPreview() {
    EasyTheme {
        EmitirComprobanteScreen(tipo = TipoComprobante.FACTURA)
    }
}
