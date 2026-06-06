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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.ui.components.ApplySystemBarsColor
import com.factapp.jhonny.ui.components.PartialOptionCard
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import com.factapp.jhonny.ui.theme.EasyTheme
import androidx.compose.foundation.BorderStroke

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
    onNuevaFactura: () -> Unit = {},
    onClientes: () -> Unit = {},
    onCatalogo: () -> Unit = {},
    onVerMasResumen: () -> Unit = {},
    onEmitirComprobante: (TipoComprobante) -> Unit = {},
    onCompras: () -> Unit = {},
    onSalidas: () -> Unit = {},
    onIngresos: () -> Unit = {},
    onHistorial: () -> Unit = {},
    onInventario: () -> Unit = {},
    onAlmacenes: () -> Unit = {},
    onComprobantesEmitidos: () -> Unit = {},
    onConfiguracion: () -> Unit = {},
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    var mostrarMenuEmitir by remember { mutableStateOf(false) }
    var mostrarMenuMas by remember { mutableStateOf(false) }
    val nombreSaludo = usuario?.company?.nombre
        ?: usuario?.email?.substringBefore("@")
        ?: "Usuario"
    val ruc = usuario?.company?.ruc ?: "—"

    ApplySystemBarsColor(
        statusBarColor = DashboardBg,
        navigationBarColor = DashboardCard,
        lightStatusBarIcons = true,
        lightNavigationBarIcons = true,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DashboardBg,
        bottomBar = {
            DashboardBottomBar(
                tabSeleccionado = tabSeleccionado,
                onTabSelected = { tabSeleccionado = it },
                onEmitirClick = {
                    tabSeleccionado = 1
                    mostrarMenuEmitir = true
                },
                onComprobantesClick = onComprobantesEmitidos,
                onConfiguracionClick = onConfiguracion,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            DashboardHeader(nombreSaludo = nombreSaludo)

            Spacer(modifier = Modifier.height(16.dp))

            ResumenFacturacionCard(
                ventasMes = "S/ 12,450.00",
                pendientes = "S/ 3,280.00",
                progresoCobrado = 0.79f,
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
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComprobanteCard(
                titulo = "Factura electrónica",
                detalle = "Serie F001 • Última emitida",
                monto = "S/ 850.00",
                etiquetaMonto = "Pendiente de cobro",
            )

            Spacer(modifier = Modifier.height(10.dp))

            ComprobanteCard(
                titulo = "Boleta electrónica",
                detalle = "Serie B001 • Hoy",
                monto = "S/ 120.50",
                etiquetaMonto = "Enviada a SUNAT",
            )

            Spacer(modifier = Modifier.height(12.dp))

            EmpresaResumenCard(
                nombreEmpresa = usuario?.company?.nombre ?: "Sin empresa vinculada",
                ruc = ruc,
            )

            Spacer(modifier = Modifier.height(24.dp))
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
                onTipoSeleccionado = { tipo ->
                    mostrarMenuEmitir = false
                    onEmitirComprobante(tipo)
                },
            )
        }
    }

    if (mostrarMenuMas) {
        PartialOptionsBottomSheet(
            onDismiss = { mostrarMenuMas = false },
            title = "Más opciones",
            subtitle = "Gestión adicional de tu empresa",
            theme = PartialSheetTheme.Dashboard,
        ) {
            MenuMasOpciones(
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
                onInventario = {
                    mostrarMenuMas = false
                    onInventario()
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
    onCompras: () -> Unit,
    onSalidas: () -> Unit,
    onIngresos: () -> Unit,
    onHistorial: () -> Unit,
    onInventario: () -> Unit,
    onAlmacenes: () -> Unit,
) {
    PartialOptionCard(
        icon = Icons.Default.ShoppingCart,
        titulo = "Compras",
        detalle = "Compras registradas como empresa",
        theme = PartialSheetTheme.Dashboard,
        iconTint = Color(0xFFEF6C00),
        iconBackground = Color(0xFFFFE0B2),
        tituloColor = Color(0xFFE65100),
        onClick = onCompras,
    )
    Spacer(modifier = Modifier.height(10.dp))
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
    Spacer(modifier = Modifier.height(10.dp))
    PartialOptionCard(
        icon = Icons.Default.History,
        titulo = "Historial",
        detalle = "Vida del producto · trazabilidad",
        theme = PartialSheetTheme.Dashboard,
        iconTint = Color(0xFF6A1B9A),
        iconBackground = Color(0xFFE1BEE7),
        tituloColor = Color(0xFF4A148C),
        onClick = onHistorial,
    )
    Spacer(modifier = Modifier.height(10.dp))
    PartialOptionCard(
        icon = Icons.Default.Inventory2,
        titulo = "Inventario",
        detalle = "Stock actual de productos por almacén",
        theme = PartialSheetTheme.Dashboard,
        iconTint = Color(0xFF4527A0),
        iconBackground = Color(0xFFD1C4E9),
        tituloColor = Color(0xFF311B92),
        onClick = onInventario,
    )
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

@Composable
private fun MenuEmitirComprobanteOpciones(
    onTipoSeleccionado: (TipoComprobante) -> Unit,
) {
    TipoComprobante.entries.forEachIndexed { index, tipo ->
        if (index > 0) Spacer(modifier = Modifier.height(10.dp))
        val colores = tipo.emitirMenuColors()
        PartialOptionCard(
            icon = tipo.emitirMenuIcon(),
            titulo = tipo.titulo,
            detalle = "Serie ${tipo.serie} · ${tipo.detalle} · SUNAT",
            theme = PartialSheetTheme.Emit,
            iconTint = colores.iconTint,
            iconBackground = colores.iconBackground,
            tituloColor = colores.title,
            onClick = { onTipoSeleccionado(tipo) },
        )
    }
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
}

@Composable
private fun DashboardHeader(nombreSaludo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Hola, $nombreSaludo",
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = DashboardNavy,
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderIconAction(
                icon = Icons.Outlined.HelpOutline,
                label = "Ayuda",
            )
            HeaderIconAction(
                icon = Icons.Default.Menu,
                label = "Menú",
            )
        }
    }
}

@Composable
private fun HeaderIconAction(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 48.dp, max = 56.dp)
            .padding(horizontal = 2.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = DashboardNavy,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = DashboardNavy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ResumenFacturacionCard(
    ventasMes: String,
    pendientes: String,
    progresoCobrado: Float,
    onVerMas: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ResumenFila(label = "Ventas del mes", valor = ventasMes)
            Spacer(modifier = Modifier.height(12.dp))
            ResumenFila(label = "Por cobrar", valor = pendientes)
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progresoCobrado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DashboardSky,
                trackColor = DashboardProgressTrack,
            )
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
            .padding(horizontal = 12.dp),
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
    horizontalPadding: Dp = 20.dp,
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
            .padding(horizontal = 20.dp),
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

private data class DashboardNavTab(
    val label: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector,
)

private val dashboardNavTabs = listOf(
    DashboardNavTab("Inicio", Icons.Outlined.Home, Icons.Default.Home),
    DashboardNavTab("Emitir", Icons.Outlined.SwapHoriz, Icons.Default.Receipt),
    DashboardNavTab("Nuevo", Icons.Outlined.AddCircleOutline, Icons.Default.Add),
    DashboardNavTab("Comprob.", Icons.Outlined.ReceiptLong, Icons.Default.ReceiptLong),
    DashboardNavTab("Ajustes", Icons.Outlined.Settings, Icons.Default.Settings),
)

@Composable
private fun DashboardBottomBar(
    tabSeleccionado: Int,
    onTabSelected: (Int) -> Unit,
    onEmitirClick: () -> Unit,
    onComprobantesClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DashboardCard,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dashboardNavTabs.forEachIndexed { index, tab ->
                DashboardBottomNavItem(
                    tab = tab,
                    selected = tabSeleccionado == index,
                    onClick = {
                        when (index) {
                            1 -> onEmitirClick()
                            3 -> onComprobantesClick()
                            4 -> onConfiguracionClick()
                            else -> onTabSelected(index)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DashboardBottomNavItem(
    tab: DashboardNavTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DashboardNavy),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tab.iconFilled,
                    contentDescription = tab.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            Icon(
                imageVector = tab.iconOutlined,
                contentDescription = tab.label,
                tint = DashboardNavy,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tab.label,
            fontSize = 11.sp,
            color = DashboardNavy,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    EasyTheme {
        DashboardScreen()
    }
}
