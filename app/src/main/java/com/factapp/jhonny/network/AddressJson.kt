package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.ADDRESS_JSON_KEYS
import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Company
import com.factapp.jhonny.network.dto.model.esSoloLinea
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
        return context.deserialize(json, Address::class.java)
    }

    override fun serialize(
        src: Address?,
        typeOfSrc: Type?,
        context: JsonSerializationContext,
    ): JsonElement {
        if (src == null) return JsonNull.INSTANCE
        if (src.esSoloLinea()) return JsonPrimitive(src.direccion)
        return context.serialize(src)
    }
}

/** Campos de domicilio planos en la raíz del JSON de empresa o anidados en `address`. */
class CompanyTypeAdapter : JsonDeserializer<Company> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Company {
        val obj = json.asJsonObject
        val address = parseAddressFromCompanyJson(obj, context)
        val stripped = obj.deepCopy()
        ADDRESS_JSON_KEYS.forEach { stripped.remove(it) }
        val company = context.deserialize<Company>(stripped, Company::class.java)
        return when {
            address == null -> company
            company.address == null -> company.copy(address = address)
            else -> company.copy(address = mergeAddress(company.address, address))
        }
    }

    private fun parseAddressFromCompanyJson(
        obj: JsonObject,
        context: JsonDeserializationContext,
    ): Address? {
        val nested = when {
            obj.has("address") && obj.get("address").isJsonObject -> obj.get("address")
            obj.has("domicilio_fiscal") && obj.get("domicilio_fiscal").isJsonObject ->
                obj.get("domicilio_fiscal")
            obj.has("direccion") && obj.get("direccion").isJsonObject -> obj.get("direccion")
            else -> obj
        }
        return context.deserialize(nested, Address::class.java)
    }

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
