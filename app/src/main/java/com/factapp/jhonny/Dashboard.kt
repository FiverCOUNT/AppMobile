package com.factapp.jhonny

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.factapp.jhonny.data.local.serieConfigurada
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.modelos.esAdmin
import com.factapp.jhonny.network.AuthRepository
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.fechaEmisionLocal
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.ui.components.ApplySystemBarsColor
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.components.PartialOptionCard
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme
import com.factapp.jhonny.ui.emitir.GuiaRemisionEventosSheet
import com.factapp.jhonny.ui.emitir.GuiaRemisionOpcionesSheet
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import com.factapp.jhonny.ui.theme.EasyTheme
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class DashboardComprobanteResumen(
    val titulo: String,
    val detalle: String,
    val monto: String,
    val etiquetaMonto: String,
)

private data class DashboardResumenCalculado(
    val ventasMes: Double,
    val porCobrar: Double,
    val progresoCobrado: Float,
    val comprobantes: List<DashboardComprobanteResumen>,
)

private fun Invoice.esVentaDashboard(): Boolean = when (tipo) {
    InvoiceTipoDoc.FACTURA, InvoiceTipoDoc.BOLETA -> true
    InvoiceTipoDoc.COD_FACTURA, InvoiceTipoDoc.COD_BOLETA -> true
    else -> false
}

private fun Invoice.esFacturaDashboard(): Boolean =
    tipo == InvoiceTipoDoc.FACTURA || tipoDoc == InvoiceTipoDoc.COD_FACTURA

private fun Invoice.esBoletaDashboard(): Boolean =
    tipo == InvoiceTipoDoc.BOLETA || tipoDoc == InvoiceTipoDoc.COD_BOLETA

private fun Invoice.detalleDashboard(): String {
    val fecha = fechaEmisionLocal()
    val etiquetaFecha = when (fecha) {
        null -> "Sin fecha"
        LocalDate.now() -> "Hoy"
        LocalDate.now().minusDays(1) -> "Ayer"
        else -> DateTimeFormatter.ofPattern("d MMM", Locale("es", "PE")).format(fecha)
    }
    return "Serie $serie • $etiquetaFecha"
}

private fun Invoice.etiquetaEstadoDashboard(): String = when (estado) {
    ComprobanteEstado.ENVIADO -> "Enviada a SUNAT"
    ComprobanteEstado.ACEPTADO -> "Aceptada por SUNAT"
    ComprobanteEstado.BORRADOR -> "Pendiente de cobro"
    ComprobanteEstado.RECHAZADO -> "Rechazada"
    ComprobanteEstado.ANULADO -> "Anulada"
}

private fun Invoice.aDashboardResumen(): DashboardComprobanteResumen =
    DashboardComprobanteResumen(
        titulo = "${etiquetaTipo()} electrónica",
        detalle = detalleDashboard(),
        monto = formatearSoles(totales.total),
        etiquetaMonto = etiquetaEstadoDashboard(),
    )

private fun calcularDashboardResumen(comprobantes: List<Invoice>): DashboardResumenCalculado {
    val hoy = LocalDate.now()
    val inicioMes = hoy.withDayOfMonth(1)
    val delMes = comprobantes.filter { doc ->
        val fecha = doc.fechaEmisionLocal() ?: return@filter false
        !fecha.isBefore(inicioMes) && !fecha.isAfter(hoy) && doc.esVentaDashboard()
    }
    val ventasMes = delMes.sumOf { it.totales.total }
    val porCobrar = delMes
        .filter { it.estado == ComprobanteEstado.BORRADOR || it.estado == ComprobanteEstado.ENVIADO }
        .sumOf { it.totales.total }
    val progreso = if (ventasMes > 0) {
        ((ventasMes - porCobrar) / ventasMes).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    val ultimaFactura = comprobantes
        .filter { it.esFacturaDashboard() }
        .maxByOrNull { it.fechaEmisionLocal() ?: LocalDate.MIN }
    val ultimaBoleta = comprobantes
        .filter { it.esBoletaDashboard() }
        .maxByOrNull { it.fechaEmisionLocal() ?: LocalDate.MIN }
    val destacados = listOfNotNull(ultimaFactura, ultimaBoleta).map { it.aDashboardResumen() }
    return DashboardResumenCalculado(
        ventasMes = ventasMes,
        porCobrar = porCobrar,
        progresoCobrado = progreso,
        comprobantes = destacados,
    )
}

// Paleta alineada a la captura (fondo gris claro, azul institucional, acento celeste en barra)
private val DashboardBg = Color(0xFFEBEBEB)
private val DashboardNavy = Color(0xFF003B7A)
private val DashboardNavyBanner = Color(0xFF003B7A)
private val DashboardSky = Color(0xFF00B4E6)
private val DashboardCard = Color(0xFFFFFFFF)
private val DashboardTextMuted = Color(0xFF5A6578)
private val DashboardProgressTrack = Color(0xFFD4D8DE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    abrirMenuEmitir: Boolean = false,
    onMenuEmitirConsumido: () -> Unit = {},
    onNuevaFactura: () -> Unit = {},
    onClientes: () -> Unit = {},
    onCatalogo: () -> Unit = {},
    onVerMasResumen: () -> Unit = {},
    onEmitirComprobante: (TipoComprobante) -> Unit = {},
    onGuiaRemisionEventos: (GuiaRemisionEventoOpcion) -> Unit = {},
    onCompras: () -> Unit = {},
    onSalidas: () -> Unit = {},
    onIngresos: () -> Unit = {},
    onHistorial: () -> Unit = {},
    onAlmacenes: () -> Unit = {},
    onComprobantesEmitidos: () -> Unit = {},
    onConfiguracion: () -> Unit = {},
    onSesionActualizada: (Usuario) -> Unit = {},
) {
    val context = LocalContext.current
    var mostrarMenuEmitir by remember { mutableStateOf(false) }
    var mostrarMenuGuiaRemision by remember { mutableStateOf(false) }
    var mostrarMenuGuiaEventos by remember { mutableStateOf(false) }
    var mostrarMenuMas by remember { mutableStateOf(false) }
    var refrescando by remember { mutableStateOf(false) }
    var cargandoInicial by remember { mutableStateOf(true) }
    var ventasMes by remember { mutableDoubleStateOf(0.0) }
    var porCobrar by remember { mutableDoubleStateOf(0.0) }
    var progresoCobrado by remember { mutableStateOf(0f) }
    var comprobantesResumen by remember { mutableStateOf<List<DashboardComprobanteResumen>>(emptyList()) }
    var refreshKey by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val companyRuc = usuario?.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val esAdmin = usuario?.esAdmin() == true
    val nombreSaludo = usuario?.company?.nombre
        ?: usuario?.email?.substringBefore("@")
        ?: "Usuario"
    val ruc = usuario?.company?.ruc ?: "—"

    suspend fun cargarDashboard() {
        if (companyRuc.isBlank()) {
            comprobantesResumen = emptyList()
            ventasMes = 0.0
            porCobrar = 0.0
            progresoCobrado = 0f
            return
        }
        val hoy = LocalDate.now()
        val inicioMes = hoy.withDayOfMonth(1)
        withContext(Dispatchers.IO) {
            ComprobanteRepository.listarEmitidos(
                companyRuc = companyRuc,
                token = token,
                desde = inicioMes,
                hasta = hoy,
            )
        }.onSuccess { lista ->
            val resumen = calcularDashboardResumen(lista)
            ventasMes = resumen.ventasMes
            porCobrar = resumen.porCobrar
            progresoCobrado = resumen.progresoCobrado
            comprobantesResumen = resumen.comprobantes
            refreshKey++
        }
    }

    LaunchedEffect(companyRuc, token) {
        cargandoInicial = true
        try {
            cargarDashboard()
        } catch (_: Exception) {
            comprobantesResumen = emptyList()
            ventasMes = 0.0
            porCobrar = 0.0
            progresoCobrado = 0f
        } finally {
            cargandoInicial = false
        }
    }

    LaunchedEffect(abrirMenuEmitir) {
        if (abrirMenuEmitir) {
            mostrarMenuEmitir = true
            onMenuEmitirConsumido()
        }
    }

    fun refrescarDashboard() {
        scope.launch {
            refrescando = true
            try {
                AuthRepository.sincronizarSesion(context, usuario)
                    .onSuccess { actualizado ->
                        onSesionActualizada(actualizado)
                    }
                cargarDashboard()
            } catch (_: Exception) {
                comprobantesResumen = emptyList()
                ventasMes = 0.0
                porCobrar = 0.0
                progresoCobrado = 0f
            } finally {
                refrescando = false
            }
        }
    }

    ApplySystemBarsColor(
        statusBarColor = ComprobanteEmitColors.topBar,
        navigationBarColor = DashboardCard,
        lightStatusBarIcons = false,
        lightNavigationBarIcons = true,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DashboardBg,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = refrescando,
            onRefresh = { refrescarDashboard() },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                DashboardTopHeader(
                    nombreSaludo = nombreSaludo,
                    onRefresh = { refrescarDashboard() },
                    refrescando = refrescando || cargandoInicial,
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResumenFacturacionCard(
                    ventasMes = if (cargandoInicial && !refrescando) "—" else formatearSoles(ventasMes),
                    pendientes = if (cargandoInicial && !refrescando) "—" else formatearSoles(porCobrar),
                    progresoCobrado = progresoCobrado,
                    cargando = cargandoInicial && !refrescando,
                    onVerMas = onVerMasResumen,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AccionesRapidasRow(
                    onNuevaFactura = onNuevaFactura,
                    onClientes = onClientes,
                    onCatalogo = onCatalogo,
                    onMas = { mostrarMenuMas = true },
                )

                Spacer(modifier = Modifier.height(20.dp))

                BannerFacturacionElectronica()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Comprobantes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DashboardNavy,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (cargandoInicial && !refrescando) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = DashboardNavy)
                    }
                } else if (comprobantesResumen.isEmpty()) {
                    Text(
                        text = "No hay comprobantes recientes",
                        color = DashboardTextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                } else {
                    comprobantesResumen.forEachIndexed { index, item ->
                        if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                        key(refreshKey, item.titulo, item.detalle) {
                            ComprobanteCard(
                                titulo = item.titulo,
                                detalle = item.detalle,
                                monto = item.monto,
                                etiquetaMonto = item.etiquetaMonto,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                key(refreshKey, nombreSaludo, ruc) {
                    EmpresaResumenCard(
                        nombreEmpresa = usuario?.company?.nombre ?: "Sin empresa vinculada",
                        ruc = ruc,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (mostrarMenuEmitir) {
        PartialOptionsBottomSheet(
            onDismiss = { mostrarMenuEmitir = false },
            title = "Emitir comprobante",
            subtitle = "Elige el tipo de documento electrónico",
            theme = PartialSheetTheme.Emit,
        ) {
            MenuEmitirComprobanteOpciones(
                context = context,
                onTipoSeleccionado = { tipo ->
                    mostrarMenuEmitir = false
                    onEmitirComprobante(tipo)
                },
                onAbrirGuiaRemision = {
                    mostrarMenuEmitir = false
                    mostrarMenuGuiaRemision = true
                },
            )
        }
    }

    GuiaRemisionOpcionesSheet(
        visible = mostrarMenuGuiaRemision,
        onDismiss = { mostrarMenuGuiaRemision = false },
        onOpcion = { opcion ->
            when (opcion) {
                GuiaRemisionOpcion.REMITENTE -> {
                    mostrarMenuGuiaRemision = false
                    onEmitirComprobante(TipoComprobante.GUIA_EMISION)
                }
                GuiaRemisionOpcion.TRANSPORTISTA -> {
                    mostrarMenuGuiaRemision = false
                    onEmitirComprobante(TipoComprobante.GUIA_TRANSPORTISTA)
                }
                GuiaRemisionOpcion.EVENTOS -> {
                    mostrarMenuGuiaRemision = false
                    mostrarMenuGuiaEventos = true
                }
            }
        },
    )

    GuiaRemisionEventosSheet(
        visible = mostrarMenuGuiaEventos,
        onDismiss = { mostrarMenuGuiaEventos = false },
        onEvento = { evento ->
            mostrarMenuGuiaEventos = false
            onGuiaRemisionEventos(evento)
        },
    )

    if (mostrarMenuMas) {
        PartialOptionsBottomSheet(
            onDismiss = { mostrarMenuMas = false },
            title = "Más opciones",
            subtitle = "Gestión adicional de tu empresa",
            theme = PartialSheetTheme.Dashboard,
        ) {
            MenuMasOpciones(
                esAdmin = esAdmin,
                onCompras = {
                    mostrarMenuMas = false
                    onCompras()
                },
                onSalidas = {
                    mostrarMenuMas = false
                    onSalidas()
                },
                onIngresos = {
                    mostrarMenuMas = false
                    onIngresos()
                },
                onHistorial = {
                    mostrarMenuMas = false
                    onHistorial()
                },
                onAlmacenes = {
                    mostrarMenuMas = false
                    onAlmacenes()
                },
            )
        }
    }
}

@Composable
private fun MenuMasOpciones(
    esAdmin: Boolean,
    onCompras: () -> Unit,
    onSalidas: () -> Unit,
    onIngresos: () -> Unit,
    onHistorial: () -> Unit,
    onAlmacenes: () -> Unit,
) {
    if (esAdmin) {
        PartialOptionCard(
            icon = Icons.Default.ShoppingCart,
            titulo = "Compras",
            detalle = "Comprobantes que otras empresas te emitieron",
            theme = PartialSheetTheme.Dashboard,
            iconTint = Color(0xFFEF6C00),
            iconBackground = Color(0xFFFFE0B2),
            tituloColor = Color(0xFFE65100),
            onClick = onCompras,
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
    PartialOptionCard(
        icon = Icons.Default.LocalShipping,
        titulo = "Salidas",
        detalle = "Despachos y salidas de almacén",
        theme = PartialSheetTheme.Dashboard,
        iconTint = Color(0xFF1565C0),
        iconBackground = Color(0xFFBBDEFB),
        tituloColor = Color(0xFF0D47A1),
        onClick = onSalidas,
    )
    if (esAdmin) {
        Spacer(modifier = Modifier.height(10.dp))
        PartialOptionCard(
            icon = Icons.Default.Input,
            titulo = "Ingresos",
            detalle = "Entradas de mercadería al inventario",
            theme = PartialSheetTheme.Dashboard,
            iconTint = Color(0xFF2E7D32),
            iconBackground = Color(0xFFC8E6C9),
            tituloColor = Color(0xFF1B5E20),
            onClick = onIngresos,
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    PartialOptionCard(
        icon = Icons.Default.History,
        titulo = "Historial",
        detalle = "Trazabilidad por ítem · serie o nombre",
        theme = PartialSheetTheme.Dashboard,
        iconTint = Color(0xFF6A1B9A),
        iconBackground = Color(0xFFE1BEE7),
        tituloColor = Color(0xFF4A148C),
        onClick = onHistorial,
    )
    if (esAdmin) {
        Spacer(modifier = Modifier.height(10.dp))
        PartialOptionCard(
            icon = Icons.Default.Store,
            titulo = "Almacenes",
            detalle = "Bodegas y ubicaciones de stock",
            theme = PartialSheetTheme.Dashboard,
            iconTint = Color(0xFF00838F),
            iconBackground = Color(0xFFB2EBF2),
            tituloColor = Color(0xFF006064),
            onClick = onAlmacenes,
        )
    }
}

@Composable
private fun MenuEmitirComprobanteOpciones(
    context: android.content.Context,
    onTipoSeleccionado: (TipoComprobante) -> Unit,
    onAbrirGuiaRemision: () -> Unit,
) {
    val tiposDirectos = TipoComprobante.entries.filter {
        it != TipoComprobante.GUIA_EMISION && it != TipoComprobante.GUIA_TRANSPORTISTA
    }
    tiposDirectos.forEachIndexed { index, tipo ->
        if (index > 0) Spacer(modifier = Modifier.height(10.dp))
        val colores = tipo.emitirMenuColors()
        PartialOptionCard(
            icon = tipo.emitirMenuIcon(),
            titulo = tipo.titulo,
            detalle = "Serie ${tipo.serieConfigurada(context)} · ${tipo.detalle} · SUNAT",
            theme = PartialSheetTheme.Emit,
            iconTint = colores.iconTint,
            iconBackground = colores.iconBackground,
            tituloColor = colores.title,
            onClick = { onTipoSeleccionado(tipo) },
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    val coloresGuia = TipoComprobante.GUIA_EMISION.emitirMenuColors()
    PartialOptionCard(
        icon = Icons.Default.LocalShipping,
        titulo = "Guía de remisión",
        detalle = "GRE remitente, transportista y eventos · SUNAT",
        theme = PartialSheetTheme.Emit,
        iconTint = coloresGuia.iconTint,
        iconBackground = coloresGuia.iconBackground,
        tituloColor = coloresGuia.title,
        onClick = onAbrirGuiaRemision,
    )
}

private data class EmitirMenuColors(
    val iconTint: Color,
    val iconBackground: Color,
    val title: Color,
)

private fun TipoComprobante.emitirMenuIcon(): ImageVector = when (this) {
    TipoComprobante.FACTURA -> Icons.Default.Receipt
    TipoComprobante.BOLETA -> Icons.Default.Description
    TipoComprobante.NOTA_CREDITO -> Icons.Default.History
    TipoComprobante.NOTA_DEBITO -> Icons.Default.Add
    TipoComprobante.GUIA_EMISION -> Icons.Default.LocalShipping
    TipoComprobante.GUIA_TRANSPORTISTA -> Icons.Default.LocalShipping
}

private fun TipoComprobante.emitirMenuColors(): EmitirMenuColors = when (this) {
    TipoComprobante.FACTURA -> EmitirMenuColors(
        iconTint = Color(0xFF1565C0),
        iconBackground = Color(0xFFBBDEFB),
        title = Color(0xFF0D47A1),
    )
    TipoComprobante.BOLETA -> EmitirMenuColors(
        iconTint = Color(0xFF2E7D32),
        iconBackground = Color(0xFFC8E6C9),
        title = Color(0xFF1B5E20),
    )
    TipoComprobante.NOTA_CREDITO -> EmitirMenuColors(
        iconTint = Color(0xFF6A1B9A),
        iconBackground = Color(0xFFE1BEE7),
        title = Color(0xFF4A148C),
    )
    TipoComprobante.NOTA_DEBITO -> EmitirMenuColors(
        iconTint = Color(0xFFEF6C00),
        iconBackground = Color(0xFFFFE0B2),
        title = Color(0xFFE65100),
    )
    TipoComprobante.GUIA_EMISION -> EmitirMenuColors(
        iconTint = Color(0xFF00838F),
        iconBackground = Color(0xFFB2EBF2),
        title = Color(0xFF006064),
    )
    TipoComprobante.GUIA_TRANSPORTISTA -> EmitirMenuColors(
        iconTint = Color(0xFFEF6C00),
        iconBackground = Color(0xFFFFE0B2),
        title = Color(0xFFE65100),
    )
}

@Composable
private fun DashboardTopHeader(
    nombreSaludo: String,
    onRefresh: () -> Unit,
    refrescando: Boolean,
) {
    val barColor = ComprobanteEmitColors.topBar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(barColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Hola,",
                    fontSize = 14.sp,
                    color = ComprobanteEmitColors.onPrimary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = nombreSaludo,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ComprobanteEmitColors.onPrimary,
                        lineHeight = 26.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Resumen de tu negocio",
                    fontSize = 13.sp,
                    color = ComprobanteEmitColors.onPrimary.copy(alpha = 0.78f),
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = ComprobanteEmitColors.onPrimary.copy(alpha = 0.16f),
            ) {
                IconButton(
                    onClick = onRefresh,
                    enabled = !refrescando,
                    modifier = Modifier.size(48.dp),
                ) {
                    if (refrescando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = ComprobanteEmitColors.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = ComprobanteEmitColors.onPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenFacturacionCard(
    ventasMes: String,
    pendientes: String,
    progresoCobrado: Float,
    cargando: Boolean,
    onVerMas: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ResumenFila(label = "Ventas del mes", valor = ventasMes)
            Spacer(modifier = Modifier.height(12.dp))
            ResumenFila(label = "Por cobrar", valor = pendientes)
            Spacer(modifier = Modifier.height(16.dp))
            if (cargando) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = DashboardSky,
                    trackColor = DashboardProgressTrack,
                )
            } else {
                LinearProgressIndicator(
                    progress = { progresoCobrado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = DashboardSky,
                    trackColor = DashboardProgressTrack,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ver más",
                color = DashboardNavy,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onVerMas),
            )
        }
    }
}

@Composable
private fun ResumenFila(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = DashboardTextMuted, fontSize = 15.sp)
        Text(
            text = valor,
            color = DashboardNavy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun AccionesRapidasRow(
    onNuevaFactura: () -> Unit,
    onClientes: () -> Unit,
    onCatalogo: () -> Unit,
    onMas: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AccionRapida(
            icon = Icons.Default.Receipt,
            label = "Nueva\nfactura",
            onClick = onNuevaFactura,
        )
        AccionRapida(
            icon = Icons.Default.People,
            label = "Clientes",
            onClick = onClientes,
        )
        AccionRapida(
            icon = Icons.Default.Description,
            label = "Catálogo",
            onClick = onCatalogo,
        )
        AccionRapida(
            icon = Icons.Default.MoreHoriz,
            label = "Más",
            onClick = onMas,
        )
    }
}

@Composable
private fun AccionRapida(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick),
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = DashboardCard,
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = DashboardNavy)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = DashboardNavy,
            lineHeight = 14.sp,
        )
    }
}

@Composable
private fun BannerFacturacionElectronica() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardNavyBanner),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = DashboardSky,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Facturación electrónica",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Emite facturas y boletas válidas ante SUNAT desde tu móvil.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun ComprobanteCard(
    titulo: String,
    detalle: String,
    monto: String,
    etiquetaMonto: String,
    horizontalPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.SemiBold,
                    color = DashboardNavy,
                )
                Text(
                    text = detalle,
                    fontSize = 13.sp,
                    color = DashboardTextMuted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = monto,
                    fontWeight = FontWeight.Bold,
                    color = DashboardNavy,
                )
                Text(
                    text = etiquetaMonto,
                    fontSize = 11.sp,
                    color = DashboardTextMuted,
                )
            }
        }
    }
}

@Composable
private fun EmpresaResumenCard(nombreEmpresa: String, ruc: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardCard),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = nombreEmpresa,
                    fontWeight = FontWeight.SemiBold,
                    color = DashboardNavy,
                )
                Text(
                    text = "RUC $ruc",
                    fontSize = 13.sp,
                    color = DashboardTextMuted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ver empresa",
                tint = DashboardNavy,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    EasyTheme {
        DashboardScreen()
    }
}
