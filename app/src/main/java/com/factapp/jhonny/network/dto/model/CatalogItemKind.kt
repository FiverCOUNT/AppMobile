package com.factapp.jhonny.network.dto.model

/**
 * Tipo de ítem en catálogo (API). Producto y servicio comparten el mismo DTO [CatalogItem].
 */
enum class CatalogItemKind(val etiqueta: String) {
    PRODUCT("Producto"),
    SERVICE("Servicio"),
    ;

    fun unidadPorDefecto(): String = when (this) {
        PRODUCT -> "NIU"
        SERVICE -> "ZZ"
    }
}
