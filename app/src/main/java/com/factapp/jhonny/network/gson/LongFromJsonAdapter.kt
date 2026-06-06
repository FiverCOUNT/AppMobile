package com.factapp.jhonny.network.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** Acepta epoch como número o como string (p. ej. BackEndEasy `lastUpdated`). */
class LongFromJsonAdapter : TypeAdapter<Long>() {
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Long = when (reader.peek()) {
        JsonToken.NULL -> {
            reader.nextNull()
            0L
        }
        JsonToken.NUMBER -> reader.nextLong()
        JsonToken.STRING -> reader.nextString().toLongOrNull() ?: 0L
        else -> {
            reader.skipValue()
            0L
        }
    }
}
