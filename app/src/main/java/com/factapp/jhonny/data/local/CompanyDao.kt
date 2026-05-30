package com.factapp.jhonny.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.factapp.jhonny.modelos.Company

@Dao
interface CompanyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(company: Company)

    @Query("SELECT * FROM companies ORDER BY nombre COLLATE NOCASE ASC")
    suspend fun listarTodas(): List<Company>
}
