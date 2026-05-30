package com.factapp.jhonny

import android.app.Application
import com.factapp.jhonny.data.local.FactAppDatabase

/**
 * Arranque de la app: aquí se inicializa Room una sola vez (singleton en [FactAppDatabase]).
 */
class FactApplication : Application() {

    lateinit var database: FactAppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = FactAppDatabase.obtener(this)
    }
}
