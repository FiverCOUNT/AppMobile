package com.factapp.jhonny.network.dto.request

import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.Address
import com.google.gson.annotations.SerializedName

data class GreEnvioRequest(
    @SerializedName("cod_traslado")
    val codTraslado: String = "01",
    @SerializedName("mod_traslado")
    val modTraslado: String = "02",
    @SerializedName("fecha_traslado")
    val fechaTraslado: String? = null,
    @SerializedName("peso_total")
    val pesoTotal: Double? = null,
    @SerializedName("und_peso_total")
    val undPesoTotal: String = "KGM",
    val vehiculo: GreVehiculoRequest? = null,
    val partida: Address? = null,
    val llegada: Address? = null,
)

data class GreVehiculoRequest(
    val placa: String? = null,
)

data class GreEventoRequest(
    @SerializedName("codigo_evento")
    val codigoEvento: String,
    val detalle: String? = null,
)

data class GreBajaRequest(
    val motivo: String,
)

data class GreOperacionResponse(
    val success: Boolean = false,
    val message: String? = null,
    val guia: Invoice? = null,
)
