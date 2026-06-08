package com.factapp.jhonny.network.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** Acepta IDs como string o número (p. ej. `almacenId` / `companyId` del backend Prisma). */
class StringFromJsonAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): String? = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            null
        }
        JsonToken.NUMBER -> reader.nextLong().toString()
        JsonToken.STRING -> reader.nextString().takeIf { it.isNotBlank() }
        else -> {
            reader.skipValue()
            null
        }
    }
}
