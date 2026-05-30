package com.factapp.jhonny.network.dto.demo

import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.model.TIPO_DOC_DNI
import com.factapp.jhonny.network.dto.model.TIPO_DOC_RUC
import com.factapp.jhonny.network.dto.model.dniValido
import com.factapp.jhonny.network.dto.request.CrearClienteRequest

/** Clientes de ejemplo cuando el API aún no responde. */
object ClientesDemo {

    private val agregados = mutableMapOf<String, MutableList<Cliente>>()

    fun listar(companyRuc: String): List<Cliente> =
        base(companyRuc) + agregados.getOrElse(companyRuc) { emptyList() }

    fun crear(companyRuc: String, body: CrearClienteRequest): Cliente {
        require(body.tipoDoc == TIPO_DOC_DNI) { "Solo se registran clientes con DNI desde la app" }
        require(dniValido(body.numeroDoc)) { "El DNI debe tener 8 dígitos" }
        require(body.razonSocial.isNotBlank()) { "El nombre es obligatorio" }

        val existente = listar(companyRuc).any {
            it.tipoDoc == body.tipoDoc && it.numeroDoc == body.numeroDoc
        }
        if (existente) {
            throw IllegalArgumentException("Ya existe un cliente con ese DNI")
        }

        val cliente = Cliente(
            id = "demo-cli-${System.currentTimeMillis()}",
            companyRuc = companyRuc,
            tipoDoc = body.tipoDoc,
            numeroDoc = body.numeroDoc,
            razonSocial = body.razonSocial.trim(),
            address = body.address,
            telefono = body.telefono?.trim()?.takeIf { it.isNotBlank() },
        )
        agregados.getOrPut(companyRuc) { mutableListOf() }.add(cliente)
        return cliente
    }

    private fun base(companyRuc: String): List<Cliente> = listOf(
        Cliente(
            id = "demo-cli-1",
            companyRuc = companyRuc,
            tipoDoc = TIPO_DOC_RUC,
            numeroDoc = "20123456789",
            razonSocial = "Comercial Andina SAC",
            address = Address.linea("Av. Javier Prado 1234, San Isidro, Lima"),
            telefono = "+51 999 111 222",
        ),
        Cliente(
            id = "demo-cli-2",
            companyRuc = companyRuc,
            tipoDoc = TIPO_DOC_RUC,
            numeroDoc = "20555666777",
            razonSocial = "Servicios Integrales del Sur SA",
            address = Address.linea("Av. Circunvalación 890, Arequipa"),
            telefono = "054 285963",
        ),
        Cliente(
            id = "demo-cli-3",
            companyRuc = companyRuc,
            tipoDoc = TIPO_DOC_DNI,
            numeroDoc = "10111222",
            razonSocial = "Juan Pérez García",
            address = Address.linea("Jr. Huancavelica 321, Lima"),
            telefono = "+51 912 345 678",
        ),
        Cliente(
            id = "demo-cli-4",
            companyRuc = companyRuc,
            tipoDoc = TIPO_DOC_DNI,
            numeroDoc = "44556677",
            razonSocial = "María García López",
            telefono = "+51 987 654 321",
        ),
    )
}
