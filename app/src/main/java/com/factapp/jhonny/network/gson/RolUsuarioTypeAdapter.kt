package com.factapp.jhonny.network.gson

import com.factapp.jhonny.modelos.RolUsuario
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class RolUsuarioTypeAdapter : TypeAdapter<RolUsuario>() {
    override fun write(out: JsonWriter, value: RolUsuario?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name)
        }
    }

    override fun read(reader: JsonReader): RolUsuario = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            RolUsuario.USUARIO
        }
        else -> when (reader.nextString().uppercase()) {
            "ADMIN" -> RolUsuario.ADMIN
            else -> RolUsuario.USUARIO
        }
    }
}
