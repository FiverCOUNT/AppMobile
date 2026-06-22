package com.factapp.jhonny.network.gson

import com.factapp.jhonny.network.dto.model.ProductoSerieEstado
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class ProductoSerieEstadoTypeAdapter : TypeAdapter<ProductoSerieEstado>() {
    override fun write(out: JsonWriter, value: ProductoSerieEstado?) {
        if (value == null) out.nullValue() else out.value(value.name)
    }

    override fun read(reader: JsonReader): ProductoSerieEstado = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            ProductoSerieEstado.DISPONIBLE
        }
        else -> when (reader.nextString().trim().uppercase()) {
            "DISPONIBLE" -> ProductoSerieEstado.DISPONIBLE
            "RESERVADO" -> ProductoSerieEstado.RESERVADO
            "VENDIDO" -> ProductoSerieEstado.VENDIDO
            "ENTREGADO" -> ProductoSerieEstado.ENTREGADO
            "BAJA" -> ProductoSerieEstado.BAJA
            else -> ProductoSerieEstado.DISPONIBLE
        }
    }
}
