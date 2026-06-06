package com.factapp.jhonny.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.factapp.jhonny.modelos.Usuario

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios ORDER BY last_updated DESC LIMIT 1")
    suspend fun obtenerSesionReciente(): Usuario?

    @Query("DELETE FROM usuarios")
    suspend fun eliminarTodos()
}
