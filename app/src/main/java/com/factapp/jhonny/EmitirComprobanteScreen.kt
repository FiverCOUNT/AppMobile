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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.OutlinedButton
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.CatalogRepository
import com.factapp.jhonny.network.ClienteRepository
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.dto.model.aptosParaBoleta
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.LineaCatalogoItem
import com.factapp.jhonny.network.dto.model.aEmitirLineas
import com.factapp.jhonny.network.dto.model.actualizarCantidad
import com.factapp.jhonny.network.dto.model.actualizarPrecioAcreditar
import com.factapp.jhonny.network.dto.almacenIdParaOperaciones
import com.factapp.jhonny.network.mensajeAuth
import com.factapp.jhonny.network.dto.usaSeriesInventario
import com.factapp.jhonny.network.dto.model.agregarDesdeCatalogo
import com.factapp.jhonny.network.dto.model.agregarDesdeCatalogoSiHayStock
import com.factapp.jhonny.network.dto.model.calcularTotales
import com.factapp.jhonny.network.dto.model.descripcionEnComprobante
import com.factapp.jhonny.network.dto.model.eliminarLinea
import com.factapp.jhonny.network.dto.model.receptorParaEmitir
import com.factapp.jhonny.network.dto.cantidadMaximaEnEmision
import com.factapp.jhonny.network.dto.coerceCantidadParaEmision
import com.factapp.jhonny.network.dto.debeValidarStockEnEmision
import com.factapp.jhonny.network.dto.etiquetaStock
import com.factapp.jhonny.network.dto.formatCantidadConUnidad
import com.factapp.jhonny.network.dto.hayStockParaEmision
import com.factapp.jhonny.network.dto.model.itemParaEmision
import com.factapp.jhonny.network.dto.model.lineasListasParaEmitir
import com.factapp.jhonny.network.dto.model.lineasListasParaEmitirNotaFiscal
import com.factapp.jhonny.network.dto.stockDisponible
import com.factapp.jhonny.network.dto.model.requireItem
import com.factapp.jhonny.network.dto.model.requiereSeries
import com.factapp.jhonny.network.dto.model.seriesValidas
import com.factapp.jhonny.network.dto.aLineasParaAcreditar
import com.factapp.jhonny.network.dto.prepararParaMotivoNc
import com.factapp.jhonny.network.dto.prepararParaMotivoNd
import com.factapp.jhonny.network.dto.buscarPorDocumento
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.filtrarComprobantesAfectados
import com.factapp.jhonny.network.dto.formatearSoles
import com.factapp.jhonny.network.dto.MotivosNotaCreditoSunat
import com.factapp.jhonny.network.dto.MotivosNotaDebitoSunat
import com.factapp.jhonny.network.dto.request.EmitirComprobanteRequest
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.SeleccionSeriesSheet
import com.factapp.jhonny.ui.emitir.CatalogoBuscarSheet
import com.factapp.jhonny.ui.emitir.ComprobanteAfectadoBuscarField
import com.factapp.jhonny.ui.emitir.MotivoNotaCreditoSelector
import com.factapp.jhonny.ui.emitir.MotivoNotaDebitoSelector
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
    val context = LocalContext.current
    val companyRuc = usuario.companyRucParaCatalogo()
    val token = usuario?.token
    val almacenId = usuario.almacenIdParaOperaciones().orEmpty()

    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var docReceptor by remember { mutableStateOf("") }
    var nombreReceptor by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var mostrarBuscarCliente by remember { mutableStateOf(false) }
    var busquedaCliente by remember { mutableStateOf("") }
    var docReferencia by remember { mutableStateOf("") }
    var comprobanteAfectado by remember { mutableStateOf<Invoice?>(null) }
    var comprobantesEmitidos by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var cargandoComprobantes by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }
    var motivoCodigoNc by remember { mutableStateOf<String?>(null) }
    var motivoCodigoNd by remember { mutableStateOf<String?>(null) }
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
    var lineaSeriesLineaId by remember { mutableStateOf<String?>(null) }

    val esNota = tipo.esNota

    LaunchedEffect(companyRuc, token, tipo) {
        if (companyRuc.isNullOrBlank()) return@LaunchedEffect
        when {
            tipo == TipoComprobante.BOLETA -> {
                withContext(Dispatchers.IO) {
                    ClienteRepository.listar(companyRuc, token).onSuccess { lista ->
                        clientes = lista.aptosParaBoleta()
                        if (clienteSeleccionado?.esEmpresa == true) {
                            clienteSeleccionado = null
                        }
                    }
                }
            }
            tipo.esNota -> {
                withContext(Dispatchers.IO) {
                    ClienteRepository.listar(companyRuc, token).onSuccess { clientes = it }
                }
            }
        }
    }

    LaunchedEffect(companyRuc, token, tipo) {
        if (companyRuc.isNullOrBlank()) return@LaunchedEffect
        if (tipo != TipoComprobante.NOTA_CREDITO && tipo != TipoComprobante.NOTA_DEBITO) return@LaunchedEffect
        cargandoComprobantes = true
        withContext(Dispatchers.IO) {
            ComprobanteRepository.listarEmitidos(companyRuc, token)
                .onSuccess { comprobantesEmitidos = it }
        }
        cargandoComprobantes = false
    }

    LaunchedEffect(docReceptor, clientes, esNota, tipo) {
        if (!esNota || clientes.isEmpty()) return@LaunchedEffect
        val doc = docReceptor.filter { it.isDigit() }
        val docValido = when (tipo) {
            TipoComprobante.NOTA_CREDITO -> doc.length == 11
            else -> doc.length == 8 || doc.length == 11
        }
        if (!docValido) return@LaunchedEffect
        clientes.buscarPorDocumento(doc)?.let { cli ->
            nombreReceptor = cli.razonSocial
            clienteSeleccionado = cli
        }
    }

    LaunchedEffect(companyRuc, token, almacenId) {
        if (companyRuc.isNullOrBlank()) return@LaunchedEffect
        cargandoCatalogo = true
        errorCatalogo = null
        withContext(Dispatchers.IO) {
            val result = if (almacenId.isNotBlank()) {
                CatalogRepository.listarPorAlmacen(companyRuc, token, almacenId)
                    .map { items -> items.filter { it.activo } }
            } else {
                CatalogRepository.listarPorEmpresa(companyRuc, token)
            }
            result
                .onSuccess { catalogo = it }
                .onFailure { errorCatalogo = it.message ?: "No se pudo cargar el catálogo" }
        }
        cargandoCatalogo = false
    }

    val esBoleta = tipo == TipoComprobante.BOLETA
    val esFactura = tipo == TipoComprobante.FACTURA
    val esGuiaEmision = tipo.esGuiaEmision
    val esNotaCredito = tipo == TipoComprobante.NOTA_CREDITO
    val esNotaDebito = tipo == TipoComprobante.NOTA_DEBITO
    val esNotaFiscal = esNotaCredito || esNotaDebito
    val acreditacionPorMonto = MotivosNotaCreditoSunat.esAcreditacionPorMonto(motivoCodigoNc)
    val ajustePorItemsNd = MotivosNotaDebitoSunat.esAjustePorItems(motivoCodigoNd)
    val documentoAfectado = comprobanteAfectado
        ?: Invoice.fromEtiqueta(docReferencia.trim())
    val docReferenciaValida = comprobanteAfectado != null ||
        documentoAfectado != null ||
        referenciaComprobanteValida(docReferencia)
    val puedeAgregarDesdeCatalogo =
        !esGuiaEmision && usuario != null && (!esNotaFiscal || docReferenciaValida)

    val sugerenciasComprobante by remember(
        comprobantesEmitidos,
        docReferencia,
        docReceptor,
        comprobanteAfectado,
        esNotaCredito,
        esNotaDebito,
    ) {
        derivedStateOf {
            if (comprobanteAfectado != null) return@derivedStateOf emptyList()
            val doc = docReceptor.filter { it.isDigit() }
            val docMinimo = if (esNotaDebito) 8 else 11
            if (docReferencia.isBlank() && doc.length < docMinimo) return@derivedStateOf emptyList()
            comprobantesEmitidos.filtrarComprobantesAfectados(
                query = docReferencia,
                docCliente = docReceptor,
                soloFacturas = esNotaCredito,
            )
        }
    }

    LaunchedEffect(catalogo, comprobanteAfectado?.id, esNotaCredito, motivoCodigoNc) {
        val inv = comprobanteAfectado ?: return@LaunchedEffect
        if (catalogo.isEmpty()) return@LaunchedEffect
        if (!esNotaCredito) return@LaunchedEffect
        val nuevas = inv.aLineasParaAcreditar(catalogo, almacenId.takeIf { it.isNotBlank() })
        if (nuevas.isNotEmpty()) {
            lineas = nuevas.prepararParaMotivoNc(motivoCodigoNc)
        }
    }

    LaunchedEffect(motivoCodigoNc, esNotaCredito) {
        if (!esNotaCredito || lineas.isEmpty()) return@LaunchedEffect
        lineas = lineas.prepararParaMotivoNc(motivoCodigoNc)
    }

    LaunchedEffect(motivoCodigoNd, esNotaDebito, comprobanteAfectado?.id, catalogo) {
        if (!esNotaDebito) return@LaunchedEffect
        if (!ajustePorItemsNd) {
            lineas = lineas.filter {
                it.cantidadOriginalReferencia == null && it.precioOriginalReferencia == null
            }
            return@LaunchedEffect
        }
        val inv = comprobanteAfectado ?: run {
            lineas = emptyList()
            return@LaunchedEffect
        }
        if (catalogo.isEmpty()) return@LaunchedEffect
        lineas = inv.aLineasParaAcreditar(catalogo, almacenId.takeIf { it.isNotBlank() })
            .prepararParaMotivoNd(motivoCodigoNd)
    }

    fun aplicarComprobanteAfectado(invoice: Invoice) {
        comprobanteAfectado = invoice
        docReferencia = invoice.etiquetaCompleta
        lineas = when {
            esNotaCredito -> invoice.aLineasParaAcreditar(catalogo, almacenId.takeIf { it.isNotBlank() })
                .prepararParaMotivoNc(motivoCodigoNc)
            esNotaDebito && ajustePorItemsNd -> invoice.aLineasParaAcreditar(catalogo, almacenId.takeIf { it.isNotBlank() })
                .prepararParaMotivoNd(motivoCodigoNd)
            else -> lineas
        }
        invoice.cliente?.let { cli ->
            clienteSeleccionado = cli
            docReceptor = cli.numeroDoc
            nombreReceptor = cli.razonSocial
        } ?: run {
            val doc = invoice.receptor.documentoNumero.filter { it.isDigit() }
            if (doc.isNotBlank()) {
                docReceptor = doc
                nombreReceptor = invoice.receptor.nombre
                clienteSeleccionado = clientes.buscarPorDocumento(doc)
            }
        }
    }

    fun limpiarComprobanteAfectado() {
        comprobanteAfectado = null
        lineas = emptyList()
    }

    val totales = lineas.calcularTotales()

    val receptor = remember(esBoleta, esFactura, esNotaCredito, clienteSeleccionado, docReceptor, nombreReceptor) {
        receptorParaEmitir(
            esBoleta = esBoleta,
            esFactura = esFactura || esNotaCredito,
            cliente = clienteSeleccionado,
            docManual = docReceptor,
            nombreManual = nombreReceptor,
        )
    }
    val receptorValido = receptor != null
    val docReceptorInvalido = when {
        esFactura || esNotaCredito ->
            docReceptor.isNotBlank() && docReceptor.length != 11
        esNotaDebito ->
            docReceptor.isNotBlank() && docReceptor.length !in listOf(8, 11)
        else -> false
    }

    val labelDocCliente = when (tipo) {
        TipoComprobante.FACTURA -> "RUC del cliente"
        TipoComprobante.BOLETA -> "DNI del cliente"
        TipoComprobante.GUIA_EMISION -> "RUC / DNI del destinatario"
        TipoComprobante.NOTA_CREDITO -> "RUC del cliente"
        TipoComprobante.NOTA_DEBITO -> "RUC / DNI del cliente"
    }
    val labelNombre = when (tipo) {
        TipoComprobante.BOLETA -> "Nombre completo"
        TipoComprobante.GUIA_EMISION -> "Razón social o nombre del destinatario"
        else -> "Razón social o nombre"
    }
    val maxDocReceptor = when (tipo) {
        TipoComprobante.FACTURA, TipoComprobante.NOTA_CREDITO -> 11
        TipoComprobante.BOLETA -> 8
        TipoComprobante.NOTA_DEBITO -> 11
        else -> 15
    }
    fun onDocReceptorChange(raw: String) {
        docReceptor = raw.filter { it.isDigit() }.take(maxDocReceptor)
        if (esNotaCredito) {
            clienteSeleccionado = null
            limpiarComprobanteAfectado()
        } else if (esNotaDebito) {
            val doc = docReceptor.filter { it.isDigit() }
            if (doc.length !in listOf(8, 11)) {
                clienteSeleccionado = null
            }
        }
    }

    val puedeEmitir = !guardando && when {
        esGuiaEmision ->
            facturasVinculadas.isNotEmpty() && receptorValido
        esNotaCredito -> {
            val lineasNcOk = if (acreditacionPorMonto) {
                lineas.all { lin ->
                    val maxPrecio = lin.precioOriginalReferencia ?: 0.0
                    val maxCant = lin.cantidadOriginalReferencia ?: lin.cantidad
                    lin.precioUnitarioEfectivo > 0 &&
                        lin.precioUnitarioEfectivo <= maxPrecio + 0.009 &&
                        lin.cantidad > 0 &&
                        lin.cantidad <= maxCant + 0.009
                }
            } else {
                lineas.all { it.cantidad > 0 }
            }
            lineas.isNotEmpty() &&
                lineasNcOk &&
                lineas.lineasListasParaEmitirNotaFiscal() &&
                docReferenciaValida &&
                motivoCodigoNc != null &&
                receptorValido
        }
        esNotaDebito -> {
            val lineasNdOk = if (ajustePorItemsNd) {
                lineas.all { lin ->
                    val maxCant = lin.cantidadOriginalReferencia ?: lin.cantidad
                    lin.precioUnitarioEfectivo > 0 &&
                        lin.cantidad > 0 &&
                        lin.cantidad <= maxCant + 0.009
                }
            } else {
                lineas.all { it.precioUnitarioEfectivo > 0 && it.cantidad > 0 }
            }
            lineas.isNotEmpty() &&
                lineasNdOk &&
                lineas.lineasListasParaEmitirNotaFiscal() &&
                docReferenciaValida &&
                motivoCodigoNd != null &&
                receptorValido
        }
        else -> lineas.isNotEmpty() && lineas.lineasListasParaEmitir() && receptorValido
    }

    fun emitirComprobante() {
        val ruc = companyRuc.orEmpty()
        val receptorEmitir = receptor ?: return
        val request = EmitirComprobanteRequest(
            companyRuc = ruc,
            tipo = tipo.tipoApi,
            receptor = receptorEmitir,
            lineas = when {
                esGuiaEmision -> emptyList()
                esNotaCredito -> lineas.aEmitirLineas(incluirPrecio = acreditacionPorMonto)
                esNotaDebito -> lineas.aEmitirLineas(incluirPrecio = ajustePorItemsNd)
                else -> lineas.aEmitirLineas()
            },
            documentoAfectado = if (tipo.esNota) {
                comprobanteAfectado?.let { inv ->
                    Invoice.referencia(
                        id = inv.id,
                        tipoDoc = inv.tipoDoc,
                        serie = inv.serie,
                        correlativo = inv.correlativo,
                    )
                } ?: documentoAfectado
            } else {
                null
            },
            facturas = facturasVinculadas.takeIf { esGuiaEmision },
            motivoNota = when {
                esNotaCredito -> MotivosNotaCreditoSunat.porCodigo(motivoCodigoNc.orEmpty())?.descripcionSunat
                esNotaDebito -> MotivosNotaDebitoSunat.porCodigo(motivoCodigoNd.orEmpty())?.descripcionSunat
                else -> motivo.takeIf { it.isNotBlank() }
            },
            motivoCodigo = when {
                esNotaCredito -> motivoCodigoNc
                esNotaDebito -> motivoCodigoNd
                else -> null
            },
            observaciones = observaciones.takeIf { it.isNotBlank() },
            almacenId = almacenId.takeIf { it.isNotBlank() },
        )
        guardando = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                ComprobanteRepository.emitir(ruc, token, request)
            }
            guardando = false
            result.fold(
                onSuccess = { invoice ->
                    val etiqueta = "${invoice.serie}-${invoice.correlativo}"
                    val mensaje = when {
                        invoice.sunatOk == false || invoice.success == false ->
                            invoice.sunatDescripcion
                                ?: invoice.message
                                ?: "$etiqueta rechazado por SUNAT"
                        invoice.estado.name == "ACEPTADO" ->
                            "$etiqueta aceptado por SUNAT"
                        invoice.estado.name == "RECHAZADO" ->
                            buildString {
                                append(invoice.sunatDescripcion ?: "$etiqueta registrado pero rechazado por SUNAT")
                                append(". Stock reservado. Reenvía desde Comprobantes emitidos.")
                            }
                        else -> "$etiqueta registrado correctamente"
                    }
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    onEmitir(tipo)
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
    }

    val lineaSeries = lineas.firstOrNull { it.lineaId == lineaSeriesLineaId }
    val seriesExcluidasEnOtrasLineas = remember(lineas, lineaSeriesLineaId) {
        lineas
            .filter { it.lineaId != lineaSeriesLineaId }
            .flatMap { it.series }
            .map { it.id }
            .toSet()
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
        soloPersonasNaturales = true,
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
            if (esNotaFiscal) {
                lineas = lineas.agregarDesdeCatalogo(
                    item,
                    almacenId = almacenId.takeIf { it.isNotBlank() },
                )
            } else {
                val (nuevas, agregado) = lineas.agregarDesdeCatalogoSiHayStock(
                    item,
                    almacenId = almacenId.takeIf { it.isNotBlank() },
                )
                if (!agregado) {
                    val msg = if (item.debeValidarStockEnEmision()) {
                        "Stock insuficiente. Disponible: ${formatCantidadConUnidad(item.stockDisponible, item.unidad)}"
                    } else {
                        "No se pudo agregar el ítem"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    return@CatalogoBuscarSheet
                }
                lineas = nuevas
            }
            if (item.usaSeriesInventario && !esNotaFiscal) {
                lineaSeriesLineaId = lineas.lastOrNull { it.catalogItemId == item.id }?.lineaId
            }
        },
    )

    SeleccionSeriesSheet(
        visible = lineaSeries != null,
        companyRuc = companyRuc.orEmpty(),
        almacenId = almacenId,
        token = token,
        catalogItem = lineaSeries?.catalogItem,
        seriesIniciales = lineaSeries?.series.orEmpty(),
        seriesExcluidasIds = seriesExcluidasEnOtrasLineas,
        onDismiss = { lineaSeriesLineaId = null },
        onConfirmar = { seleccionadas ->
            val lineaId = lineaSeries?.lineaId ?: return@SeleccionSeriesSheet
            lineas = lineas.map { linea ->
                if (linea.lineaId == lineaId) {
                    linea.copy(
                        series = seleccionadas,
                        numerosSerie = emptyList(),
                        almacenId = almacenId.takeIf { it.isNotBlank() }
                            ?: seleccionadas.firstOrNull()?.almacenId,
                        cantidad = seleccionadas.size.toDouble(),
                    )
                } else {
                    linea
                }
            }
            lineaSeriesLineaId = null
        },
    )

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
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
        bottomBar = {
            EmitDockInferior(
                puedeEmitir = puedeEmitir,
                guardando = guardando,
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
                        if (esFactura && !receptorValido) {
                            Text(
                                text = "El cliente seleccionado debe tener un RUC de 11 dígitos.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 2.dp, top = 8.dp),
                            )
                        }
                    } ?: run {
                        Spacer(modifier = Modifier.height(10.dp))
                        EmitCampo(
                            value = docReceptor,
                            onValueChange = ::onDocReceptorChange,
                            label = labelDocCliente,
                            placeholder = if (esFactura) "Ej. 20123456789" else "Ej. 45678901",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        if (docReceptorInvalido) {
                            Text(
                                text = "La factura exige un RUC de 11 dígitos.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                            )
                        }
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
                        onValueChange = ::onDocReceptorChange,
                        label = labelDocCliente,
                        placeholder = if (esNotaDebito) "RUC 11 dígitos o DNI 8 dígitos" else "Ej. 20123456789",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    if (docReceptorInvalido) {
                        Text(
                            text = when {
                                esNotaDebito ->
                                    "Ingresa un RUC (11 dígitos) o DNI (8 dígitos) válido."
                                else ->
                                    "La nota de crédito exige un RUC de 11 dígitos."
                            },
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                        )
                    } else if (esNotaCredito && docReceptor.length in 1..10) {
                        Text(
                            text = "Ingresa el RUC completo (11 dígitos) del cliente.",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                            modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                        )
                    }
                    if (esNota && !esNotaCredito && docReceptor.length in listOf(8, 11) &&
                        nombreReceptor.isBlank() && clientes.isEmpty()
                    ) {
                        Text(
                            text = "Buscando cliente registrado…",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                            modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    EmitCampo(
                        value = nombreReceptor,
                        onValueChange = { nombreReceptor = it },
                        label = labelNombre,
                        placeholder = "Nombre o razón social",
                        readOnly = esNota && clienteSeleccionado != null,
                    )
                    if (esNota && clienteSeleccionado != null) {
                        Text(
                            text = "Nombre cargado del cliente registrado.",
                            fontSize = 12.sp,
                            color = C.accent,
                            modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                        )
                    }
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
                    icono = if (esNotaDebito) Icons.Default.Add else Icons.AutoMirrored.Filled.Undo,
                    titulo = "Documento afectado",
                    subtitulo = when {
                        esNotaCredito -> "Busca la factura afectada y carga sus ítems"
                        esNotaDebito -> "Busca la factura o boleta afectada"
                        else -> "Comprobante original y motivo"
                    },
                ) {
                    ComprobanteAfectadoBuscarField(
                        value = docReferencia,
                        onValueChange = { docReferencia = it },
                        sugerencias = sugerenciasComprobante,
                        cargando = cargandoComprobantes,
                        comprobanteSeleccionado = comprobanteAfectado,
                        onSeleccionar = { aplicarComprobanteAfectado(it) },
                        onLimpiarSeleccion = { limpiarComprobanteAfectado() },
                        label = if (esNotaDebito) "Comprobante afectado" else "Factura afectada",
                        placeholder = if (esNotaDebito) {
                            "Busca factura F001, boleta B001 o cliente…"
                        } else {
                            "Busca por serie F001, número o cliente…"
                        },
                    )
                    if (docReferenciaValida && lineas.isNotEmpty()) {
                        Text(
                            text = when {
                                esNotaDebito && ajustePorItemsNd ->
                                    "${lineas.size} ítem(s) cargados de $docReferencia. Indica el monto adicional por unidad."
                                esNotaCredito ->
                                    "${lineas.size} ítem(s) cargados de $docReferencia. Ajusta cantidades a acreditar."
                                else -> "${lineas.size} ítem(s) cargados de $docReferencia."
                            },
                            fontSize = 12.sp,
                            color = C.accent,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                        )
                    } else if (sugerenciasComprobante.isEmpty() && !cargandoComprobantes) {
                        val doc = docReceptor.filter { it.isDigit() }
                        val docListo = if (esNotaDebito) doc.length in listOf(8, 11) else doc.length == 11
                        if (docListo) {
                            Text(
                                text = if (esNotaDebito) {
                                    "No hay comprobantes aceptados para este documento."
                                } else {
                                    "No hay facturas aceptadas para este RUC."
                                },
                                fontSize = 12.sp,
                                color = C.textSecondary,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (esNotaCredito) {
                        MotivoNotaCreditoSelector(
                            codigoSeleccionado = motivoCodigoNc,
                            onCodigoChange = { motivoCodigoNc = it },
                        )
                        MotivosNotaCreditoSunat.hintAcreditacion(motivoCodigoNc)?.let { hint ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = hint,
                                fontSize = 12.sp,
                                color = C.textSecondary,
                                lineHeight = 17.sp,
                            )
                        }
                    } else if (esNotaDebito) {
                        MotivoNotaDebitoSelector(
                            codigoSeleccionado = motivoCodigoNd,
                            onCodigoChange = { motivoCodigoNd = it },
                        )
                        MotivosNotaDebitoSunat.hintDebitacion(motivoCodigoNd)?.let { hint ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = hint,
                                fontSize = 12.sp,
                                color = C.textSecondary,
                                lineHeight = 17.sp,
                            )
                        }
                    }
                }
            }

            if (!esGuiaEmision) {
                EmitSeccionCard(
                    icono = Icons.Default.ShoppingCart,
                    titulo = when {
                        esNotaCredito -> "Ítems a acreditar"
                        esNotaDebito -> "Ítems a debitar"
                        else -> "Detalle de venta"
                    },
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
                                esNotaFiscal && !docReferenciaValida ->
                                    if (esNotaDebito) "Sin ítems a debitar" else "Sin ítems a acreditar"
                                else -> "Tu comprobante está vacío"
                            },
                            subtitulo = when {
                                esNotaFiscal && !docReferenciaValida ->
                                    "Primero indica el documento afectado y el motivo SUNAT"
                                esNotaDebito && !ajustePorItemsNd ->
                                    "Agrega líneas del catálogo con el monto del cargo (intereses, penalidad…)"
                                esNotaFiscal ->
                                    "Esperando líneas del comprobante o catálogo"
                                else ->
                                    "Agrega productos o servicios del catálogo"
                            },
                        )
                    } else {
                        lineas.forEach { linea ->
                            LineaItemCard(
                                linea = linea,
                                catalogo = catalogo,
                                onCantidadChange = { nueva ->
                                    lineas = lineas.actualizarCantidad(linea.lineaId, nueva)
                                },
                                onAbrirSeries = {
                                    lineaSeriesLineaId = linea.lineaId
                                },
                                onEliminar = {
                                    lineas = lineas.eliminarLinea(linea.lineaId)
                                },
                                modoDescuentoMonto = acreditacionPorMonto,
                                modoDebitoMonto = ajustePorItemsNd,
                                precioFacturaOriginal = linea.precioOriginalReferencia,
                                cantidadMaximaFactura = linea.cantidadOriginalReferencia,
                                onPrecioAcreditarChange = { monto ->
                                    lineas = lineas.actualizarPrecioAcreditar(
                                        linea.lineaId,
                                        monto,
                                        limitarAlOriginal = acreditacionPorMonto,
                                    )
                                },
                                validarStock = !esNotaFiscal,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (puedeAgregarDesdeCatalogo) {
                        Spacer(modifier = Modifier.height(4.dp))
                        EmitBotonAgregarCatalogo(
                            texto = when {
                                esNotaCredito -> "Agregar ítem extra del catálogo"
                                esNotaDebito -> "Agregar cargo del catálogo"
                                else -> "Buscar en catálogo y agregar"
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
    readOnly: Boolean = false,
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
            readOnly = readOnly,
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
    guardando: Boolean,
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
                if (guardando) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = C.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text("Enviando a SUNAT…", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
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
}

@Composable
private fun LineaItemCard(
    linea: LineaCatalogoItem,
    catalogo: List<CatalogItem>,
    onCantidadChange: (Double) -> Unit,
    onAbrirSeries: () -> Unit,
    onEliminar: () -> Unit,
    modoDescuentoMonto: Boolean = false,
    modoDebitoMonto: Boolean = false,
    precioFacturaOriginal: Double? = null,
    cantidadMaximaFactura: Double? = null,
    onPrecioAcreditarChange: ((Double) -> Unit)? = null,
    validarStock: Boolean = true,
) {
    val item = remember(linea.lineaId, linea.catalogItemId, catalogo) {
        linea.itemParaEmision(catalogo)
    }
    val conSerie = linea.requiereSeries
    var cantidadTexto by remember(linea.lineaId) {
        mutableStateOf(formatCantidad(linea.cantidad))
    }
    LaunchedEffect(linea.cantidad) {
        val esperado = formatCantidad(linea.cantidad)
        if (cantidadTexto.toDoubleOrNull() != linea.cantidad) {
            cantidadTexto = esperado
        }
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
                            text = when {
                                (modoDescuentoMonto || modoDebitoMonto) && precioFacturaOriginal != null ->
                                    "Facturado: ${formatearSoles(precioFacturaOriginal)} c/u"
                                else -> "${formatearSoles(item.precioUnitario)} c/u"
                            },
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

            if (modoDescuentoMonto && onPrecioAcreditarChange != null) {
                val precioFacturado = precioFacturaOriginal ?: linea.precioOriginalReferencia ?: 0.0
                val descuentoUnitario = linea.precioUnitarioEfectivo
                var precioNuevoTexto by remember(linea.lineaId, linea.precioUnitario) {
                    mutableStateOf(
                        if (precioFacturado > 0 && descuentoUnitario > 0) {
                            formatCantidad((precioFacturado - descuentoUnitario).coerceAtLeast(0.0))
                        } else {
                            ""
                        },
                    )
                }
                LaunchedEffect(linea.precioUnitario, precioFacturado) {
                    if (precioFacturado > 0 && descuentoUnitario > 0) {
                        val esperado = formatCantidad((precioFacturado - descuentoUnitario).coerceAtLeast(0.0))
                        if (precioNuevoTexto.toDoubleOrNull() != (precioFacturado - descuentoUnitario).coerceAtLeast(0.0)) {
                            precioNuevoTexto = esperado
                        }
                    }
                }
                Column {
                    Text(
                        text = "Precio nuevo por unidad (con IGV)",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = precioNuevoTexto,
                        onValueChange = { texto ->
                            val filtrado = texto.filter { c -> c.isDigit() || c == '.' }
                            precioNuevoTexto = filtrado
                            val precioNuevo = filtrado.toDoubleOrNull() ?: 0.0
                            val nuevoAjustado = precioNuevo.coerceIn(0.0, precioFacturado)
                            val descuento = (precioFacturado - nuevoAjustado).coerceAtLeast(0.0)
                            onPrecioAcreditarChange(descuento)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                if (precioFacturado > 0) {
                                    "Ej. ${formatCantidad(precioFacturado * 0.8)}"
                                } else {
                                    "Ej. 47.20"
                                },
                                color = C.textSecondary.copy(alpha = 0.55f),
                                fontSize = 14.sp,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    if (precioFacturado > 0) {
                        Text(
                            text = "Precio facturado: ${formatearSoles(precioFacturado)}",
                            fontSize = 11.sp,
                            color = C.textSecondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (descuentoUnitario > 0) {
                        Text(
                            text = "Descuento por unidad: ${formatearSoles(descuentoUnitario)}",
                            fontSize = 11.sp,
                            color = C.accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (modoDebitoMonto && onPrecioAcreditarChange != null) {
                var montoAdicionalTexto by remember(linea.lineaId, linea.precioUnitario) {
                    mutableStateOf(
                        if (linea.precioUnitarioEfectivo > 0) {
                            formatCantidad(linea.precioUnitarioEfectivo)
                        } else {
                            ""
                        },
                    )
                }
                val precioFacturado = precioFacturaOriginal ?: linea.precioOriginalReferencia
                Column {
                    Text(
                        text = "Monto adicional por unidad (con IGV)",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = montoAdicionalTexto,
                        onValueChange = { texto ->
                            val filtrado = texto.filter { c -> c.isDigit() || c == '.' }
                            montoAdicionalTexto = filtrado
                            val parsed = filtrado.toDoubleOrNull() ?: 0.0
                            onPrecioAcreditarChange(parsed.coerceAtLeast(0.0))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Ej. 180.00",
                                color = C.textSecondary.copy(alpha = 0.55f),
                                fontSize = 14.sp,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    precioFacturado?.takeIf { it > 0 }?.let { ref ->
                        Text(
                            text = "Precio facturado (referencia): ${formatearSoles(ref)}",
                            fontSize = 11.sp,
                            color = C.textSecondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (conSerie) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (linea.series.isNotEmpty()) {
                                "${linea.series.size} serie(s) seleccionada(s)"
                            } else {
                                "Sin series seleccionadas"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = C.textSecondary,
                        )
                        linea.series.take(3).forEach { serie ->
                            Text(
                                text = "• ${serie.numeroSerie}",
                                fontSize = 12.sp,
                                color = C.textPrimary,
                            )
                        }
                        if (linea.series.size > 3) {
                            Text(
                                text = "… y ${linea.series.size - 3} más",
                                fontSize = 11.sp,
                                color = C.textSecondary,
                            )
                        }
                    }
                    IconButton(onClick = onAbrirSeries) {
                        Icon(Icons.Default.MoreHoriz, "Elegir series", tint = C.accent)
                    }
                }
                if (!linea.seriesValidas()) {
                    Text(
                        text = "Toca el botón para elegir series del almacén",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            } else {
                val maxCantidad = when {
                    cantidadMaximaFactura != null -> cantidadMaximaFactura
                    validarStock -> item.cantidadMaximaEnEmision()
                    else -> null
                }
                val validaStock = validarStock && item.debeValidarStockEnEmision()
                val sinStockSuficiente = validaStock && maxCantidad != null && linea.cantidad > maxCantidad
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = when {
                                    modoDebitoMonto -> "Unidades con aumento"
                                    modoDescuentoMonto -> "Unidades con descuento"
                                    else -> "Cantidad"
                                },
                                fontSize = 13.sp,
                                color = C.textSecondary,
                                fontWeight = FontWeight.Medium,
                            )
                            item.etiquetaStock()?.let { stockLabel ->
                                Text(
                                    text = stockLabel,
                                    fontSize = 11.sp,
                                    color = C.textSecondary,
                                )
                            }
                        }
                        EmitStepperCantidad(
                            cantidad = linea.cantidad,
                            cantidadTexto = cantidadTexto,
                            maxCantidad = if (validaStock) maxCantidad else null,
                            onCantidadTextoChange = { texto ->
                                val filtrado = texto.filter { c -> c.isDigit() || c == '.' }
                                if (filtrado.isEmpty()) {
                                    cantidadTexto = ""
                                    return@EmitStepperCantidad
                                }
                                val parsed = filtrado.toDoubleOrNull()
                                if (parsed == null) {
                                    cantidadTexto = filtrado
                                    return@EmitStepperCantidad
                                }
                                val ajustada = if (maxCantidad != null) {
                                    parsed.coerceIn(0.0, maxCantidad)
                                } else {
                                    item.coerceCantidadParaEmision(parsed)
                                }
                                cantidadTexto = formatCantidad(ajustada)
                                onCantidadChange(ajustada)
                            },
                            puedeIncrementar = !validaStock || item.hayStockParaEmision(linea.cantidad + 1.0),
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
                                if (maxCantidad != null && nueva > maxCantidad) return@EmitStepperCantidad
                                if (validaStock && !item.hayStockParaEmision(nueva)) return@EmitStepperCantidad
                                cantidadTexto = formatCantidad(nueva)
                                onCantidadChange(nueva)
                            },
                        )
                    }
                    if (sinStockSuficiente) {
                        Text(
                            text = if (cantidadMaximaFactura != null) {
                                "Máximo según factura: ${formatCantidadConUnidad(maxCantidad ?: 0.0, item.unidad)}"
                            } else {
                                "Máximo disponible: ${formatCantidadConUnidad(maxCantidad ?: 0.0, item.unidad)}"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    when {
                        modoDebitoMonto -> "Total a debitar"
                        modoDescuentoMonto -> "Total a acreditar"
                        else -> "Subtotal"
                    },
                    fontSize = 12.sp,
                    color = C.textSecondary,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = formatearSoles(
                        if (modoDescuentoMonto || modoDebitoMonto) linea.total else linea.subtotal,
                    ),
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
    maxCantidad: Double? = null,
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
                CantidadInputCompact(
                    value = cantidadTexto,
                    maxCantidad = maxCantidad,
                    onValueChange = onCantidadTextoChange,
                )
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
    maxCantidad: Double? = null,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = { raw ->
            val filtrado = raw.filter { it.isDigit() || it == '.' }
            if (filtrado.isEmpty()) {
                onValueChange("")
                return@BasicTextField
            }
            val parsed = filtrado.toDoubleOrNull()
            if (parsed == null) {
                onValueChange(filtrado)
                return@BasicTextField
            }
            if (maxCantidad != null && maxCantidad != Double.MAX_VALUE) {
                val capped = parsed.coerceIn(0.0, maxCantidad)
                onValueChange(formatCantidad(capped))
            } else {
                onValueChange(filtrado)
            }
        },
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

/** Formato serie-correlativo, ej. F001-00000001 */
private fun referenciaComprobanteValida(docReferencia: String): Boolean {
    val ref = docReferencia.trim()
    return ref.length >= 10 && ref.contains("-")
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
