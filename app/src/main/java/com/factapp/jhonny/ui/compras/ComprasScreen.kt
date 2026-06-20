package com.factapp.jhonny.ui.compras

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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.dto.fechaEmisionLegible
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.colorArgb
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.emisorParaPdf
import com.factapp.jhonny.network.dto.etiqueta
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.tieneCdrZip
import com.factapp.jhonny.network.dto.tienePdf
import com.factapp.jhonny.ui.catalogo.CatalogoBusquedaBar
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

private fun List<Invoice>.filtrarCompras(query: String): List<Invoice> {
    if (query.isBlank()) return this
    val q = query.trim().lowercase()
    return filter { c ->
        c.etiquetaCompleta.lowercase().contains(q) ||
            c.receptor.nombre.lowercase().contains(q) ||
            c.receptor.documentoNumero.lowercase().contains(q) ||
            c.observaciones?.lowercase()?.contains(q) == true ||
            c.estado.etiqueta().lowercase().contains(q)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprasScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token
    val emisorPdf = usuario.emisorParaPdf()

    var compras by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var compraDetalle by remember { mutableStateOf<Invoice?>(null) }

    val filtradas by remember(compras, busqueda) {
        derivedStateOf { compras.filtrarCompras(busqueda) }
    }

    val subtituloHeader by remember(compras.size, busqueda, filtradas.size) {
        derivedStateOf {
            when {
                compras.isEmpty() -> "Facturas de compra registradas"
                busqueda.isNotBlank() -> "${filtradas.size} de ${compras.size} documentos"
                else -> "${compras.size} compras registradas"
            }
        }
    }

    LaunchedEffect(companyRuc, token) {
        cargando = true
        error = null
        withContext(Dispatchers.IO) {
            ComprobanteRepository.listarCompras(companyRuc, token)
                .onSuccess { compras = it }
                .onFailure { error = it.message ?: "No se pudieron cargar las compras" }
        }
        cargando = false
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Compras",
        subtitulo = subtituloHeader,
        icono = Icons.Default.ShoppingCart,
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
                    Text(error!!, color = C.textSecondary)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                CatalogoBusquedaBar(
                                    value = busqueda,
                                    onValueChange = { busqueda = it },
                                )
                            }
                        }
                        if (compras.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "No hay compras registradas",
                                        color = C.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else if (filtradas.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Sin resultados para tu búsqueda",
                                        color = C.textSecondary,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        } else {
                            items(filtradas, key = { it.id }) { compra ->
                                CompraCard(
                                    compra = compra,
                                    companyRuc = companyRuc,
                                    token = token,
                                    emisorFallback = emisorPdf,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { compraDetalle = compra },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    CompraDetalleSheet(
        compra = compraDetalle,
        onDismiss = { compraDetalle = null },
        companyRuc = companyRuc,
        token = token,
        emisorFallback = emisorPdf,
    )
}

@Composable
private fun CompraCard(
    compra: Invoice,
    companyRuc: String,
    token: String?,
    emisorFallback: Company? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var descargandoPdf by remember(compra.id) { mutableStateOf(false) }
    val estadoColor = Color(compra.estado.colorArgb())
    val muestraDocumentos = compra.tienePdf() || compra.tieneCdrZip()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = C.accentSoft,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = C.accent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text(
                            text = compra.receptor.nombre,
                            fontWeight = FontWeight.Bold,
                            color = C.textPrimary,
                            fontSize = 15.sp,
                        )
                        Text(
                            text = "RUC ${compra.receptor.documentoNumero}",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatearSoles(compra.totales.total),
                        fontWeight = FontWeight.Bold,
                        color = C.primary,
                        fontSize = 16.sp,
                    )
                    compra.fechaEmisionLegible()?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = C.textSecondary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${compra.etiquetaTipo()} ${compra.etiquetaCompleta}",
                    fontSize = 13.sp,
                    color = C.accent,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = estadoColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = compra.estado.etiqueta(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = estadoColor,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${compra.lineas.size} línea(s) · IGV ${formatearSoles(compra.totales.igv)}",
                fontSize = 12.sp,
                color = C.textSecondary,
            )
        }
        if (muestraDocumentos) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = C.border.copy(alpha = 0.35f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (compra.tienePdf()) {
                    TextButton(
                        enabled = !descargandoPdf,
                        onClick = {
                            descargandoPdf = true
                            scope.launch {
                                ComprobanteDocumentIntents.abrirPdf(
                                    context = context,
                                    comprobante = compra,
                                    companyRuc = companyRuc,
                                    token = token,
                                    emisorFallback = emisorFallback,
                                )
                                descargandoPdf = false
                            }
                        },
                    ) {
                        if (descargandoPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = C.accent,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = C.accent,
                            )
                        }
                        Spacer(Modifier.size(6.dp))
                        Text(
                            if (descargandoPdf) "Abriendo…" else "Ver PDF",
                            fontSize = 13.sp,
                            color = C.accent,
                        )
                    }
                }
                if (compra.tieneCdrZip()) {
                    TextButton(
                        onClick = { ComprobanteDocumentIntents.abrirCdrZip(context, compra) },
                    ) {
                        Icon(
                            Icons.Outlined.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = C.primary,
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("CDR (.zip)", fontSize = 13.sp, color = C.primary)
                    }
                }
            }
        }
    }
}
