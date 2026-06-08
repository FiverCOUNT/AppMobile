package com.factapp.jhonny.network.dto.model

import com.google.gson.annotations.SerializedName

/**
 * Empresa emisora, cliente receptor y datos fiscales en comprobantes e inventario.
 * La ubicación va en [address].
 */
data class Company(
    val id: Long? = null,
    val ruc: String = "",
    @SerializedName(value = "nombre", alternate = ["razon_social"])
    val nombre: String = "",
    @SerializedName("nombre_comercial")
    val nombreComercial: String? = null,
    /** Cat. 06 — útil cuando [Company] es receptor (DNI, RUC, etc.). */
    @SerializedName("tipo_doc")
    val tipoDoc: String? = null,
    @SerializedName(value = "numero_doc", alternate = ["num_doc"])
    val numeroDoc: String? = null,
    @SerializedName(value = "address", alternate = ["domicilio_fiscal"])
    val address: Address? = null,
    val email: String? = null,
    @SerializedName("telefono")
    val telefono: String? = null,
    @SerializedName("telefonos")
    val telefonos: List<String>? = null,
    @SerializedName("emails")
    val emails: List<String>? = null,
    @SerializedName("cuentas_bancarias")
    val cuentasBancarias: List<String>? = null,
    @SerializedName("billeteras_digitales")
    val billeterasDigitales: List<String>? = null,
    @SerializedName("mensaje_agradecimiento")
    val mensajeAgradecimiento: String? = null,
    @SerializedName("mensaje_promocional")
    val mensajePromocional: String? = null,
    @SerializedName("sol_user")
    val solUser: String? = null,
    @SerializedName("sol_pass")
    val solPass: String? = null,
    @SerializedName("client_id")
    val clientId: String? = null,
    @SerializedName("client_secret")
    val clientSecret: String? = null,
    @SerializedName(value = "ruta_firma", alternate = ["certificate_path"])
    val rutaFirma: String? = null,
    @SerializedName("certificate_password")
    val certificatePassword: String? = null,
    @SerializedName(value = "ruta_logo", alternate = ["logo_path", "logo_url"])
    val rutaLogo: String? = null,
    @SerializedName("name_logo")
    val nameLogo: String? = null,
    @SerializedName(value = "entorno", alternate = ["environment"])
    val entorno: String? = null,
    @SerializedName("webhook_url")
    val webhookUrl: String? = null,
    @SerializedName("api_key")
    val apiKey: String? = null,
    @SerializedName("api_secret")
    val apiSecret: String? = null,
    val plan: String? = null,
    @SerializedName("tax_regime")
    val taxRegime: String? = null,
    @SerializedName("igv_rate_override")
    val igvRateOverride: Double? = null,
    @SerializedName("nrus_categoria")
    val nrusCategoria: Int? = null,
    @SerializedName("max_documents_month")
    val maxDocumentsMonth: Int? = null,
    @SerializedName("documents_this_month")
    val documentsThisMonth: Int? = null,
    @SerializedName("ai_messages_this_month")
    val aiMessagesThisMonth: Int? = null,
    @SerializedName("usage_reset_month")
    val usageResetMonth: String? = null,
    @SerializedName("user_id")
    val userId: Long? = null,
    @SerializedName("sire_enabled")
    val sireEnabled: Boolean? = null,
    @SerializedName("sire_last_period_synced")
    val sireLastPeriodSynced: String? = null,
    @SerializedName("sire_last_reconciliation_at")
    val sireLastReconciliationAt: String? = null,
    @SerializedName("sire_client_id")
    val sireClientId: String? = null,
    @SerializedName("sire_client_secret")
    val sireClientSecret: String? = null,
    @SerializedName("activo")
    val activo: Boolean? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    @SerializedName("tiene_certificado")
    val tieneCertificado: Boolean? = null,
    @SerializedName("tiene_webhook")
    val tieneWebhook: Boolean? = null,
    @SerializedName("creado_en")
    val creadoEn: String? = null,
) {
    val telefonoPrincipal: String?
        get() = telefono ?: telefonos?.firstOrNull()

    val emailPrincipal: String?
        get() = email ?: emails?.firstOrNull()

    /** Número de documento fiscal (RUC emisor o doc. del receptor). */
    val documentoNumero: String
        get() = numeroDoc ?: ruc

    val documentoTipo: String
        get() = tipoDoc ?: when (documentoNumero.length) {
            11 -> TIPO_DOC_RUC
            8 -> TIPO_DOC_DNI
            else -> tipoDoc ?: TIPO_DOC_RUC
        }

    val estaActiva: Boolean?
        get() = activo ?: isActive
}

val Company.direccion: String?
    get() = address?.lineaPrincipal

/** Alias de [nombre] (JSON `razon_social` en receptor/cliente). */
val Company.razonSocial: String
    get() = nombre

fun companyReceptor(
    tipoDoc: String,
    numeroDoc: String,
    nombre: String,
    address: Address? = null,
    email: String? = null,
    telefono: String? = null,
): Company = Company(
    ruc = if (tipoDoc == TIPO_DOC_RUC) numeroDoc else "",
    nombre = nombre,
    tipoDoc = tipoDoc,
    numeroDoc = numeroDoc,
    address = address,
    email = email,
    telefono = telefono,
)

fun Cliente.aCompany(): Company = Company(
    id = id.toLongOrNull(),
    ruc = if (tipoDoc == TIPO_DOC_RUC) numeroDoc else "",
    tipoDoc = tipoDoc,
    numeroDoc = numeroDoc,
    nombre = razonSocial,
    address = address,
    telefono = telefono,
)

/** Receptor UBL para [com.factapp.jhonny.network.dto.request.EmitirComprobanteRequest]. */
fun receptorParaEmitir(
    esBoleta: Boolean,
    cliente: Cliente?,
    docManual: String,
    nombreManual: String,
): Company? {
    cliente?.let { return it.aCompany() }
    val doc = docManual.filter { it.isDigit() }
    val nombre = nombreManual.trim()
    if (doc.isBlank() || nombre.isBlank()) return null
    val tipoDoc = when {
        esBoleta -> TIPO_DOC_DNI
        doc.length == 11 -> TIPO_DOC_RUC
        else -> TIPO_DOC_DNI
    }
    return companyReceptor(tipoDoc = tipoDoc, numeroDoc = doc, nombre = nombre)
}
