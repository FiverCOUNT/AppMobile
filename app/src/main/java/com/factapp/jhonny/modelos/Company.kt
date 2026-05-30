package com.factapp.jhonny.modelos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.factapp.jhonny.network.dto.model.BusinessTemplate

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    @ColumnInfo(name = "ruc")
    val ruc: String,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "ruta_firma")
    val rutaFirma: String?,
    @ColumnInfo(name = "ruta_logo")
    val rutaLogo: String?,
    @ColumnInfo(name = "name_logo")
    val name_logo: String?,
    @ColumnInfo(name = "direccion")
    val direccion: String?,
    @ColumnInfo(name = "telefono")
    val telefono: String?,
    /** Plantilla visual del negocio (retail, servicios, etc.). */
    @ColumnInfo(name = "plantilla", defaultValue = "GENERAL")
    val plantilla: BusinessTemplate = BusinessTemplate.GENERAL,
)
