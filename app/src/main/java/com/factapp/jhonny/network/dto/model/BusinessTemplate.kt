package com.factapp.jhonny.network.dto.model

/**
 * Plantilla del negocio (API / config): afecta la UI, no el DTO del ítem en sí.
 */
enum class BusinessTemplate(
    val etiqueta: String,
    val itemPorDefecto: CatalogItemKind,
) {
    GENERAL("Comercio general", CatalogItemKind.PRODUCT),
    RETAIL("Tienda / retail", CatalogItemKind.PRODUCT),
    SERVICIOS("Servicios profesionales", CatalogItemKind.SERVICE),
    ;

    fun configFormulario(kind: CatalogItemKind): FormularioItemConfig = when (this) {
        GENERAL -> FormularioItemConfig(
            muestraSku = kind == CatalogItemKind.PRODUCT,
            muestraStock = false,
            muestraDuracion = kind == CatalogItemKind.SERVICE,
        )
        RETAIL -> FormularioItemConfig(
            muestraSku = true,
            muestraStock = kind == CatalogItemKind.PRODUCT,
            muestraDuracion = false,
        )
        SERVICIOS -> FormularioItemConfig(
            muestraSku = false,
            muestraStock = false,
            muestraDuracion = kind == CatalogItemKind.SERVICE,
        )
    }
}

data class FormularioItemConfig(
    val muestraSku: Boolean,
    val muestraStock: Boolean,
    val muestraDuracion: Boolean,
)
