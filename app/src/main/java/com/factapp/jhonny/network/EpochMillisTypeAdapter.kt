package com.factapp.jhonny.network

import com.factapp.jhonny.modelos.TimestampConverter
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** Deserializa fechas del API como epoch ms (número o string legacy). */
class EpochMillisTypeAdapter : TypeAdapter<Long>() {
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(`in`: JsonReader): Long = readEpochMillis(`in`) ?: 0L
}

class NullableEpochMillisTypeAdapter : TypeAdapter<Long?>() {
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(`in`: JsonReader): Long? = readEpochMillis(`in`)
}

private fun readEpochMillis(`in`: JsonReader): Long? = when (`in`.peek()) {
    JsonToken.NULL -> {
        `in`.nextNull()
        null
    }
    JsonToken.NUMBER -> `in`.nextLong()
    JsonToken.STRING -> TimestampConverter.fromJson(`in`.nextString())
    else -> {
        `in`.skipValue()
        null
    }
}
