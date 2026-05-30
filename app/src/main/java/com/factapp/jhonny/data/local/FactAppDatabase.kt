package com.factapp.jhonny.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.factapp.jhonny.modelos.AppRoomConverters
import com.factapp.jhonny.modelos.Company
import com.factapp.jhonny.modelos.Usuario

@Database(
    entities = [
        Company::class,
        Usuario::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(AppRoomConverters::class)
abstract class FactAppDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE companies ADD COLUMN plantilla TEXT NOT NULL DEFAULT 'GENERAL'",
                )
                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN plantilla TEXT NOT NULL DEFAULT 'GENERAL'",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS catalog_items (
                        id TEXT NOT NULL PRIMARY KEY,
                        company_ruc TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        codigo TEXT,
                        nombre TEXT NOT NULL,
                        descripcion TEXT,
                        unidad TEXT NOT NULL,
                        precio_unitario REAL NOT NULL,
                        afectacion_igv TEXT NOT NULL DEFAULT '10',
                        activo INTEGER NOT NULL DEFAULT 1,
                        maneja_stock INTEGER NOT NULL DEFAULT 0,
                        stock_actual REAL,
                        duracion_minutos INTEGER,
                        FOREIGN KEY(company_ruc) REFERENCES companies(ruc) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_catalog_items_company_ruc ON catalog_items(company_ruc)",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS catalog_items")
            }
        }

        @Volatile
        private var instancia: FactAppDatabase? = null

        fun obtener(context: Context): FactAppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    FactAppDatabase::class.java,
                    "fact_app.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
