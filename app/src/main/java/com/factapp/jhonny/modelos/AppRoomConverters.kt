package com.factapp.jhonny.modelos

import androidx.room.TypeConverter
import com.factapp.jhonny.network.dto.model.BusinessTemplate
import java.sql.Timestamp

/**
 * Converters que Room usa para guardar tipos que SQLite no entiende nativamente.
 */
class AppRoomConverters {
    @TypeConverter
    fun estadoDesdeTexto(valor: String): EstadoUsuario = EstadoUsuario.valueOf(valor)

    @TypeConverter
    fun estadoATexto(estado: EstadoUsuario): String = estado.name

    @TypeConverter
    fun timestampDesdeLong(valor: Long): Timestamp = Timestamp(valor)

    @TypeConverter
    fun timestampALong(valor: Timestamp): Long = valor.time

    @TypeConverter
    fun businessTemplateDesdeTexto(valor: String): BusinessTemplate =
        BusinessTemplate.valueOf(valor)

    @TypeConverter
    fun businessTemplateATexto(plantilla: BusinessTemplate): String = plantilla.name
}
