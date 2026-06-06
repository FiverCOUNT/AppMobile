package com.factapp.jhonny.data.local

import android.content.Context
import com.factapp.jhonny.FactApplication
import com.factapp.jhonny.modelos.Company
import com.factapp.jhonny.modelos.Usuario

/**
 * Persistencia local de la sesión (Room).
 * Una sola fuente de verdad para token, refresh y empresa embebida.
 */
object SesionStore {

    private fun db(context: Context) =
        (context.applicationContext as FactApplication).database

    suspend fun guardar(context: Context, usuario: Usuario) {
        val database = db(context)
        usuario.company?.let { database.companyDao().insertar(it) }
        database.usuarioDao().insertar(usuario)
    }

    suspend fun obtenerSesionReciente(context: Context): Usuario? =
        db(context).usuarioDao().obtenerSesionReciente()

    suspend fun eliminar(context: Context) {
        db(context).usuarioDao().eliminarTodos()
    }
}
