package com.factapp.jhonny.network.dto.demo

import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.model.MovimientoCliente
import com.factapp.jhonny.network.dto.model.MovimientoEstado
import com.factapp.jhonny.network.dto.model.MovimientoTipo
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.model.ProductoSerieEstado
import com.factapp.jhonny.network.dto.model.lineaCatalogoConNumerosSerie
import com.factapp.jhonny.network.dto.model.lineaCatalogoConSerie
import com.factapp.jhonny.network.dto.model.lineaCatalogoSinSerie
import com.factapp.jhonny.network.dto.request.CrearAlmacenRequest
import com.factapp.jhonny.network.dto.request.RegistrarEntradaRequest
import com.factapp.jhonny.network.dto.request.RegistrarSalidaRequest

/**
 * Datos de inventario para desarrollo cuando el API aún no expone almacén/series/salidas.
 */
object InventarioDemo {

    private val almacenesAgregados = mutableMapOf<String, MutableList<Almacen>>()

    fun almacenPrincipal(companyRuc: String): Almacen = Almacen(
        id = "demo-alm-central",
        companyRuc = companyRuc,
        codigo = "CENTRAL",
        nombre = "Almacén central",
        address = Address.linea("Local principal"),
    )

    fun almacenesDemo(companyRuc: String): List<Almacen> =
        almacenesBase(companyRuc) + almacenesAgregados.getOrElse(companyRuc) { emptyList() }

    private fun almacenesBase(companyRuc: String): List<Almacen> = listOf(
        almacenPrincipal(companyRuc),
        Almacen(
            id = "demo-alm-sur",
            companyRuc = companyRuc,
            codigo = "SUR",
            nombre = "Almacén sur",
            address = Address.linea("Sede sur · traslados internos"),
        ),
    )

    fun crearAlmacenDemo(companyRuc: String, body: CrearAlmacenRequest): Almacen {
        val almacen = Almacen(
            id = "demo-alm-${System.currentTimeMillis()}",
            companyRuc = companyRuc,
            codigo = body.codigo.trim().uppercase(),
            nombre = body.nombre.trim(),
            address = body.address,
        )
        almacenesAgregados.getOrPut(companyRuc) { mutableListOf() }.add(almacen)
        return almacen
    }

    fun seriesDisponibles(
        companyRuc: String,
        catalogItemId: String,
        almacenId: String? = null,
    ): List<ProductoSerie> {
        val centralId = almacenPrincipal(companyRuc).id
        if (almacenId != null && almacenId != centralId) return emptyList()
        return when (catalogItemId) {
            "demo-pc" -> listOf(
                serie(companyRuc, "demo-pc", "ser-pc-1", "DL-SN-2026-00487"),
                serie(companyRuc, "demo-pc", "ser-pc-2", "DL-SN-2026-00488"),
            )
            else -> emptyList()
        }
    }

    private fun serie(
        companyRuc: String,
        catalogItemId: String,
        id: String,
        numeroSerie: String,
    ) = ProductoSerie(
        id = id,
        companyRuc = companyRuc,
        catalogItemId = catalogItemId,
        numeroSerie = numeroSerie,
        almacenId = "demo-alm-central",
        estado = ProductoSerieEstado.DISPONIBLE,
    )

    fun salidasDemo(companyRuc: String): List<Movimiento> = listOf(
        Movimiento(
            id = "demo-ent-1",
            companyRuc = companyRuc,
            almacenId = "demo-alm-central",
            tipo = MovimientoTipo.SALIDA,
            numero = "ENT-00001",
            estado = MovimientoEstado.DESPACHADA,
            comprobanteId = "demo-cmp-f001",
            guiaRemisionId = "cmp-venta-guia",
            guiaRemision = Invoice.referencia(
                tipoDoc = InvoiceTipoDoc.GUIA_EMISION,
                serie = "T001",
                correlativo = "00000102",
            ),
            lineas = listOf(
                lineaCatalogoSinSerie("demo-01", 10.0),
                lineaCatalogoConSerie(
                    serie = serie(companyRuc, "demo-pc", "ser-pc-1", "DL-SN-2026-00487")
                        .copy(estado = ProductoSerieEstado.ENTREGADO, entregaId = "demo-ent-1"),
                ),
            ),
            fecha = "2026-05-18T14:30:00Z",
            cliente = MovimientoCliente(
                tipoDoc = "6",
                numeroDoc = "20100070970",
                razonSocial = "Cliente Demo SAC",
            ),
            fechaDespacho = "2026-05-18T14:30:00Z",
            referenciaTipo = "SALIDA",
        ),
        Movimiento(
            id = "demo-ent-2",
            companyRuc = companyRuc,
            almacenId = "demo-alm-central",
            almacenDestinoId = "demo-alm-sur",
            tipo = MovimientoTipo.SALIDA,
            numero = "ENT-00002",
            estado = MovimientoEstado.DESPACHADA,
            lineas = listOf(lineaCatalogoSinSerie("demo-03", 24.0)),
            fecha = "2026-05-17T09:00:00Z",
            fechaDespacho = "2026-05-17T09:00:00Z",
            referenciaTipo = "TRASLADO",
        ),
    )

    fun movimientosEntradaDemo(companyRuc: String): List<Movimiento> = listOf(
        Movimiento(
            id = "demo-mov-1",
            companyRuc = companyRuc,
            almacenId = "demo-alm-central",
            tipo = MovimientoTipo.ENTRADA,
            referenciaTipo = "COMPRA",
            lineas = listOf(
                lineaCatalogoSinSerie("demo-01", 100.0),
                lineaCatalogoSinSerie("demo-02", 40.0),
            ),
            fecha = "2026-05-17T09:00:00Z",
            observaciones = "Ingreso por compra a proveedor",
        ),
        Movimiento(
            id = "demo-mov-2",
            companyRuc = companyRuc,
            almacenId = "demo-alm-central",
            tipo = MovimientoTipo.ENTRADA,
            referenciaTipo = "INGRESO_MANUAL",
            lineas = listOf(
                lineaCatalogoConNumerosSerie(
                    catalogItemId = "demo-pc",
                    numerosSerie = listOf("DL-SN-2026-00487", "DL-SN-2026-00488"),
                ),
            ),
            fecha = "2026-05-16T11:15:00Z",
            observaciones = "Laptops con serie registrada",
        ),
    )

    fun historialDemo(companyRuc: String): List<Movimiento> =
        (movimientosEntradaDemo(companyRuc) + salidasDemo(companyRuc))
            .sortedByDescending { it.fechaEfectiva }
}
