package com.factapp.jhonny.network.dto.model

import com.factapp.jhonny.network.dto.parseFechaEmisionIso
import com.google.gson.annotations.SerializedName

/**
 * Comprobante electrónico unificado (Greenter `Invoice` / voucher UBL).
 *
 * Un solo modelo para factura (`01`), boleta (`03`), notas (`07`/`08`), guía (`09`), etc.;
 * el tipo se distingue con [tipoDoc]. Incluye campos del API Laravel ([id], [estado], [pdfUrl]…).
 *
 * Para catálogo e inventario ver [LineaCatalogoItem]; aquí solo [SaleDetail] persistido.
 */
data class Invoice(
    val id: String,
    @SerializedName("company_ruc")
    val companyRuc: String = "",

    // — Greenter Voucher / Invoice —
    @SerializedName("ubl_version")
    val ublVersion: String = "2.1",
    @SerializedName("tipo_operacion")
    val tipoOperacion: String = "0101",
    /** Cat. 01 — 01 factura, 03 boleta, 07 NC, 08 ND, 09 guía, etc. */
    @SerializedName(value = "tipo_doc", alternate = ["tipo", "tipo_documento"])
    val tipoDoc: String,
    val serie: String,
    @SerializedName(value = "correlativo", alternate = ["numero"])
    val correlativo: String,
    @SerializedName("fecha_emision")
    val fechaEmision: Long? = null,
    @SerializedName(value = "fec_vencimiento", alternate = ["fecha_vencimiento"])
    val fecVencimiento: String? = null,
    @SerializedName("tipo_moneda")
    val tipoMoneda: String = "PEN",
    @SerializedName(value = "company", alternate = ["emisor"])
    val company: Company? = null,
    /** Receptor UBL Greenter (factura, guía, notas — RUC / datos fiscales). */
    @SerializedName(value = "client", alternate = ["receptor"])
    val client: Company = Company(nombre = ""),
    /** Cliente del catálogo CRUD; en boleta (`03`) el receptor suele venir aquí, no en [client]. */
    val cliente: Cliente? = null,
    @SerializedName(value = "details", alternate = ["lineas", "items"])
    val details: List<SaleDetail> = emptyList(),
    @SerializedName(value = "legends", alternate = ["leyendas"])
    val legends: List<Legend>? = null,
    @SerializedName("forma_pago")
    val formaPago: String? = null,
    @SerializedName(value = "observacion", alternate = ["observaciones"])
    val observacion: String? = null,
    @SerializedName("mto_oper_gravadas")
    val mtoOperGravadas: Double? = null,
    @SerializedName("mto_oper_exoneradas")
    val mtoOperExoneradas: Double? = null,
    @SerializedName("mto_oper_inafectas")
    val mtoOperInafectas: Double? = null,
    @SerializedName("mto_oper_exportacion")
    val mtoOperExportacion: Double? = null,
    @SerializedName("mto_igv")
    val mtoIgv: Double? = null,
    @SerializedName("total_impuestos")
    val totalImpuestos: Double? = null,
    @SerializedName("sub_total")
    val subTotal: Double? = null,
    @SerializedName("mto_imp_venta")
    val mtoImpVenta: Double? = null,

    // — Notas, guías y metadatos del API —
    /** Solo notas: comprobante que se afecta (referencia o snapshot embebido). */
    @SerializedName(value = "documento_afectado", alternate = ["doc_afectado"])
    val documentoAfectado: Invoice? = null,
    /** Solo guía de emisión: facturas que ampara el traslado (una o más). */
    val facturas: List<Invoice>? = null,
    @SerializedName("motivo_codigo")
    val motivoCodigo: String? = null,
    @SerializedName("motivo_nota")
    val motivoNota: String? = null,

    val estado: ComprobanteEstado = ComprobanteEstado.BORRADOR,
    @SerializedName("cdr_estado")
    val cdrEstado: String? = null,

    // — Archivos del comprobante (XML, PDF, CDR) —
    /** Representación imprimible del comprobante (PDF). */
    @SerializedName("pdf_url")
    val pdfUrl: String? = null,
    /** Paquete CDR de SUNAT (.zip con XML de respuesta). */
    @SerializedName("cdr_zip_url")
    val cdrZipUrl: String? = null,
    @SerializedName("xml_url")
    val xmlUrl: String? = null,

    // — Respuesta SUNAT —
    @SerializedName("sunat_estado")
    val sunatEstadoDirecto: String? = null,
    @SerializedName("sunat_codigo")
    val sunatCodigoDirecto: String? = null,
    @SerializedName("sunat_descripcion")
    val sunatDescripcionDirecto: String? = null,
    @SerializedName("sunat_notas")
    val sunatNotasDirecto: List<String>? = null,
    @SerializedName(value = "hash", alternate = ["hash_cpe"])
    val hash: String? = null,
    /** JSON anidado legacy: `"sunat": { "estado", "codigo", … }`. */
    @SerializedName("sunat")
    val sunatJson: SunatJson? = null,

    @SerializedName("enviar_automatico")
    val enviarAutomatico: Boolean? = null,

    /** Respuesta del API al emitir (BackEndEasy → mobile). */
    val success: Boolean? = null,
    @SerializedName("sunat_ok")
    val sunatOk: Boolean? = null,
    @SerializedName("puede_reenviar")
    val puedeReenviar: Boolean? = null,
    val message: String? = null,
) {
    /** Tipo normalizado para UI/API legado (FACTURA, BOLETA, …). */
    val tipo: String
        get() = normalizarTipoDocumento(tipoDoc)

    val numero: String
        get() = correlativo

    val esBoleta: Boolean
        get() = tipo == InvoiceTipoDoc.BOLETA || tipoDoc == InvoiceTipoDoc.COD_BOLETA

    /** Receptor para UI y búsqueda: boleta → [cliente]; resto → [client]. */
    val receptor: Company
        get() = if (esBoleta && cliente != null) cliente.aCompany() else client

    val lineas: List<SaleDetail>
        get() = details

    val items: List<SaleDetail>
        get() = details

    val observaciones: String?
        get() = observacion

    val leyendas: List<Legend>?
        get() = legends

    val emisor: Company?
        get() = company

    /** Proveedor en compras: empresa que te emitió el comprobante. */
    val proveedor: Company
        get() = emisor?.takeIf { it.nombre.isNotBlank() || it.ruc.isNotBlank() }
            ?: Company(ruc = companyRuc, nombre = companyRuc.ifBlank { "Proveedor" })

    val etiquetaCompleta: String
        get() = "$serie-$correlativo"

    val etiqueta: String
        get() = etiquetaCompleta

    val esGuiaEmision: Boolean
        get() = tipo == InvoiceTipoDoc.GUIA_EMISION || tipoDoc == InvoiceTipoDoc.COD_GUIA

    /** Solo identifica otro comprobante (sin líneas ni datos de negocio). */
    val esSoloReferencia: Boolean
        get() = details.isEmpty() && id.isBlank()

    val motivoDescripcion: String?
        get() = motivoNota

    val cantidadFacturasVinculadas: Int
        get() = facturas?.size ?: 0

    /** Totales desde columnas UBL o suma de [details]. */
    val totales: ComprobanteTotales
        get() {
            val sub = subTotal
                ?: mtoOperGravadas
                ?: details.takeIf { it.isNotEmpty() }?.sumOf { it.subtotal }
                ?: 0.0
            val igvVal = mtoIgv
                ?: totalImpuestos
                ?: details.takeIf { it.isNotEmpty() }?.sumOf { it.igv }
                ?: 0.0
            val totalVal = mtoImpVenta ?: (sub + igvVal)
            return ComprobanteTotales(
                subtotal = sub,
                igv = igvVal,
                total = totalVal,
                moneda = tipoMoneda,
                mtoOperGravadas = mtoOperGravadas,
                mtoOperExoneradas = mtoOperExoneradas,
                mtoOperInafectas = mtoOperInafectas,
                mtoOperExportacion = mtoOperExportacion,
                totalImpuestos = totalImpuestos,
            )
        }

    /** Estado devuelto por SUNAT (`ACEPTADO`, `RECHAZADO`, …). */
    val sunatEstado: String?
        get() = sunatEstadoDirecto ?: sunatJson?.estado ?: cdrEstado

    val sunatCodigo: String?
        get() = sunatCodigoDirecto ?: sunatJson?.codigo

    val sunatDescripcion: String?
        get() = sunatDescripcionDirecto ?: sunatJson?.descripcion

    val sunatNotas: List<String>?
        get() = sunatNotasDirecto ?: sunatJson?.notas

    val hashCpe: String?
        get() = hash ?: sunatJson?.hashCpe

    fun normalizado(): Invoice {
        val tipoNormalizado = normalizarTipoDocumento(tipoDoc)
        val estadoNormalizado = sunatEstado?.let { parseEstadoSunat(it) } ?: estado
        val correlativoNormalizado = correlativo.ifBlank { "0" }
        return copy(
            tipoDoc = tipoNormalizado,
            correlativo = correlativoNormalizado,
            estado = estadoNormalizado,
            motivoNota = motivoNota ?: documentoAfectado?.motivoNota ?: documentoAfectado?.motivoDescripcion,
        )
    }

    companion object {
        const val FORMA_PAGO_CONTADO = "Contado"
        const val FORMA_PAGO_CREDITO = "Credito"

        /** Referencia mínima a otro comprobante (nota, guía → facturas). */
        fun referencia(
            tipoDoc: String = InvoiceTipoDoc.FACTURA,
            serie: String,
            correlativo: String,
            motivoCodigo: String? = null,
            motivoDescripcion: String? = null,
            fechaEmision: String? = null,
            id: String = "",
        ): Invoice = Invoice(
            id = id,
            tipoDoc = tipoDoc,
            serie = serie,
            correlativo = correlativo,
            motivoCodigo = motivoCodigo,
            motivoNota = motivoDescripcion,
            fechaEmision = parseFechaEmisionIso(fechaEmision),
        )

        /** Parsea `F001-00001234` → referencia (tipo por defecto factura). */
        fun fromEtiqueta(
            etiqueta: String,
            tipoDoc: String = InvoiceTipoDoc.FACTURA,
        ): Invoice? {
            val partes = etiqueta.trim().split("-", limit = 2)
            if (partes.size != 2 || partes[0].isBlank() || partes[1].isBlank()) return null
            return referencia(
                tipoDoc = tipoDoc,
                serie = partes[0],
                correlativo = partes[1],
            )
        }
    }
}

/** Valores de [Invoice.tipoDoc] / emitir comprobante en el API. */
object InvoiceTipoDoc {
    const val FACTURA = "FACTURA"
    const val BOLETA = "BOLETA"
    const val NOTA_CREDITO = "NOTA_CREDITO"
    const val NOTA_DEBITO = "NOTA_DEBITO"
    const val GUIA_EMISION = "GUIA_EMISION"
    const val GUIA_TRANSPORTISTA = "GUIA_TRANSPORTISTA"

    const val COD_FACTURA = "01"
    const val COD_BOLETA = "03"
    const val COD_NOTA_CREDITO = "07"
    const val COD_NOTA_DEBITO = "08"
    const val COD_GUIA = "09"
    const val COD_GUIA_TRANSPORTISTA = "31"
}

private fun normalizarTipoDocumento(tipoRaw: String): String = when (tipoRaw.trim()) {
    InvoiceTipoDoc.COD_FACTURA -> InvoiceTipoDoc.FACTURA
    InvoiceTipoDoc.COD_BOLETA -> InvoiceTipoDoc.BOLETA
    InvoiceTipoDoc.COD_NOTA_CREDITO -> InvoiceTipoDoc.NOTA_CREDITO
    InvoiceTipoDoc.COD_NOTA_DEBITO -> InvoiceTipoDoc.NOTA_DEBITO
    InvoiceTipoDoc.COD_GUIA -> InvoiceTipoDoc.GUIA_EMISION
    InvoiceTipoDoc.COD_GUIA_TRANSPORTISTA -> InvoiceTipoDoc.GUIA_TRANSPORTISTA
    else -> tipoRaw
}

private fun parseEstadoSunat(estadoRaw: String): ComprobanteEstado? = when (estadoRaw.trim().uppercase()) {
    "ACEPTADO", "ACEPTADA" -> ComprobanteEstado.ACEPTADO
    "ENVIADO", "GENERADA", "ENVIADA", "PROCESANDO", "EN_PROCESO", "PENDIENTE" -> ComprobanteEstado.ENVIADO
    "RECHAZADO", "RECHAZADA", "ERROR" -> ComprobanteEstado.RECHAZADO
    "ANULADO", "ANULACION_EN_PROCESO" -> ComprobanteEstado.ANULADO
    "BORRADOR" -> ComprobanteEstado.BORRADOR
    else -> null
}

/** Deserialización JSON anidado `"sunat": { … }` (legacy API). */
data class SunatJson(
    val estado: String? = null,
    val codigo: String? = null,
    val descripcion: String? = null,
    val notas: List<String>? = null,
    @SerializedName(value = "hash", alternate = ["hash_cpe"])
    val hashCpe: String? = null,
)
