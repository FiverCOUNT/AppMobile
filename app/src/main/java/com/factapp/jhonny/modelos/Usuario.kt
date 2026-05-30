package com.factapp.jhonny.modelos

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(
    tableName = "usuarios"
)
data class Usuario(
    @PrimaryKey
    val email: String,
    @ColumnInfo(name = "contrasena")
    val contrasena: String,
    @ColumnInfo(name = "token")
    val token: String?,
    @ColumnInfo(name = "refresh_token")
    val refreshToken: String?,
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Timestamp,
    @ColumnInfo(name = "estado")
    val estado: EstadoUsuario,
    /** Empresa asociada; Room aplana sus columnas en `usuarios` (mismos nombres que en [Company]). */
    @Embedded
    val company: Company?,
)