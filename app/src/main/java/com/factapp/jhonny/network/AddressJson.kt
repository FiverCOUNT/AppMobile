package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.ADDRESS_JSON_KEYS
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.esSoloLinea
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/** Acepta `"direccion": "texto"` o un objeto con ubigeo / departamento / etc. */
class AddressTypeAdapter : JsonDeserializer<Address>, JsonSerializer<Address> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext,
    ): Address? {
        if (json == null || json.isJsonNull) return null
        if (json.isJsonPrimitive) {
            val texto = json.asString.trim()
            return texto.takeIf { it.isNotEmpty() }?.let { Address.linea(it) }
        }
        if (!json.isJsonObject) return null
        val obj = json.asJsonObject
        return Address(
            ubigeo = obj.stringOrNull("ubigeo"),
            departamento = obj.stringOrNull("departamento"),
            provincia = obj.stringOrNull("provincia"),
            distrito = obj.stringOrNull("distrito"),
            urbanizacion = obj.stringOrNull("urbanizacion"),
            direccion = obj.stringOrNull("direccion"),
            codLocal = obj.stringOrNull("cod_local"),
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        get(key)?.takeIf { !it.isJsonNull }?.asString?.trim()?.takeIf { it.isNotEmpty() }

    override fun serialize(
        src: Address?,
        typeOfSrc: Type?,
        context: JsonSerializationContext,
    ): JsonElement {
        if (src == null) return JsonNull.INSTANCE
        if (src.esSoloLinea()) return JsonPrimitive(src.direccion)
        val obj = JsonObject()
        src.ubigeo?.let { obj.addProperty("ubigeo", it) }
        src.departamento?.let { obj.addProperty("departamento", it) }
        src.provincia?.let { obj.addProperty("provincia", it) }
        src.distrito?.let { obj.addProperty("distrito", it) }
        src.urbanizacion?.let { obj.addProperty("urbanizacion", it) }
        src.direccion?.let { obj.addProperty("direccion", it) }
        src.codLocal?.let { obj.addProperty("cod_local", it) }
        return obj
    }
}

/** Campos de domicilio planos en la raíz del JSON de empresa o anidados en `address`. */
class CompanyTypeAdapter : JsonDeserializer<Company> {

    /** Gson sin este adaptador: evita recursión infinita al parsear `company` en login. */
    private val delegate: Gson = GsonBuilder()
        .registerTypeAdapter(Address::class.java, AddressTypeAdapter())
        .create()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Company {
        val obj = json.asJsonObject
        val address = parseAddressFromCompanyJson(obj)
        val stripped = obj.deepCopy()
        listOf("address", "domicilio_fiscal").forEach { stripped.remove(it) }
        ADDRESS_JSON_KEYS.forEach { stripped.remove(it) }
        val company = delegate.fromJson(stripped, Company::class.java)
        return when {
            address == null -> company
            company.address == null -> company.copy(address = address)
            else -> company.copy(address = mergeAddress(company.address, address))
        }
    }

    private fun parseAddressFromCompanyJson(obj: JsonObject): Address? {
        val nested = when {
            obj.has("address") && !obj.get("address").isJsonNull -> obj.get("address")
            obj.has("domicilio_fiscal") && !obj.get("domicilio_fiscal").isJsonNull ->
                obj.get("domicilio_fiscal")
            obj.has("direccion") && obj.get("direccion").isJsonObject -> obj.get("direccion")
            else -> return null
        }
        if (nested.isJsonPrimitive) {
            val texto = nested.asString.trim()
            return texto.takeIf { it.isNotEmpty() }?.let { Address.linea(it) }
        }
        if (!nested.isJsonObject) return null
        val addr = nested.asJsonObject
        return Address(
            ubigeo = addr.stringOrNull("ubigeo"),
            departamento = addr.stringOrNull("departamento"),
            provincia = addr.stringOrNull("provincia"),
            distrito = addr.stringOrNull("distrito"),
            urbanizacion = addr.stringOrNull("urbanizacion"),
            direccion = addr.stringOrNull("direccion"),
            codLocal = addr.stringOrNull("cod_local"),
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        get(key)?.takeIf { !it.isJsonNull }?.asString?.trim()?.takeIf { it.isNotEmpty() }

    private fun mergeAddress(existing: Address?, incoming: Address): Address {
        if (existing == null) return incoming
        return Address(
            ubigeo = existing.ubigeo ?: incoming.ubigeo,
            departamento = existing.departamento ?: incoming.departamento,
            provincia = existing.provincia ?: incoming.provincia,
            distrito = existing.distrito ?: incoming.distrito,
            urbanizacion = existing.urbanizacion ?: incoming.urbanizacion,
            direccion = existing.direccion ?: incoming.direccion,
            codLocal = existing.codLocal ?: incoming.codLocal,
        )
    }
}
