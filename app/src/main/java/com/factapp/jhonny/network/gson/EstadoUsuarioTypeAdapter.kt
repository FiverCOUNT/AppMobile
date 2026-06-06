package com.factapp.jhonny.network.gson

import com.factapp.jhonny.modelos.EstadoUsuario
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** Mapea estados del API (p. ej. INACTIVO, PENDIENTE) al enum local. */
class EstadoUsuarioTypeAdapter : TypeAdapter<EstadoUsuario>() {
    override fun write(out: JsonWriter, value: EstadoUsuario?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name)
        }
    }

    override fun read(reader: JsonReader): EstadoUsuario = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            EstadoUsuario.DISABLED
        }
        else -> when (reader.nextString().uppercase()) {
            "ACTIVO" -> EstadoUsuario.ACTIVO
            "DELETED" -> EstadoUsuario.DELETED
            else -> EstadoUsuario.DISABLED
        }
    }
}
