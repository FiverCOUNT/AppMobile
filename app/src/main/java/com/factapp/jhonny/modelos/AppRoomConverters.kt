package com.factapp.jhonny.modelos

import androidx.room.TypeConverter
import com.factapp.jhonny.network.dto.model.BusinessTemplate
import java.sql.Timestamp

/**
 * Converters que Room usa para guardar tipos que SQLite no entiende nativamente.
 */
class AppRoomConverters {
    @TypeConverter
    fun estadoDesdeTexto(valor: String): EstadoUsuario = when (valor.uppercase()) {
        "ACTIVO" -> EstadoUsuario.ACTIVO
        "DELETED" -> EstadoUsuario.DELETED
        else -> EstadoUsuario.DISABLED
    }

    @TypeConverter
    fun estadoATexto(estado: EstadoUsuario): String = estado.name

    @TypeConverter
    fun rolDesdeTexto(valor: String): RolUsuario = when (valor.uppercase()) {
        "ADMIN" -> RolUsuario.ADMIN
        else -> RolUsuario.USUARIO
    }

    @TypeConverter
    fun rolATexto(rol: RolUsuario): String = rol.name

    @TypeConverter
    fun timestampDesdeLong(valor: Long): Timestamp = Timestamp(valor)

    @TypeConverter
    fun timestampALong(valor: Timestamp): Long = valor.time

    @TypeConverter
    fun businessTemplateDesdeTexto(valor: String): BusinessTemplate =
        runCatching { BusinessTemplate.valueOf(valor.uppercase()) }
            .getOrDefault(BusinessTemplate.GENERAL)

    @TypeConverter
    fun businessTemplateATexto(plantilla: BusinessTemplate): String = plantilla.name
}
