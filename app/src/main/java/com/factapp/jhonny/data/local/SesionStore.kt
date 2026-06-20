package com.factapp.jhonny.data.local

import android.content.Context
import com.factapp.jhonny.FactApplication
import com.factapp.jhonny.modelos.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Persistencia local de la sesión (Room).
 * Una sola fuente de verdad para token, refresh y empresa embebida.
 */
object SesionStore {

    private fun db(context: Context) =
        (context.applicationContext as FactApplication).database

    suspend fun guardar(context: Context, usuario: Usuario) = withContext(Dispatchers.IO) {
        val database = db(context)
        usuario.company?.takeIf { it.ruc.isNotBlank() }?.let { database.companyDao().insertar(it) }
        database.usuarioDao().insertar(usuario)
    }

    /** Guarda sesión cuando el usuario aún no tiene empresa vinculada en el token. */
    suspend fun guardarSinEmpresa(context: Context, usuario: Usuario) = withContext(Dispatchers.IO) {
        db(context).usuarioDao().insertar(usuario.copy(company = null))
    }

    suspend fun obtenerSesionReciente(context: Context): Usuario? = withContext(Dispatchers.IO) {
        try {
            db(context).usuarioDao().obtenerSesionReciente()
        } catch (_: Exception) {
            try {
                db(context).usuarioDao().eliminarTodos()
            } catch (_: Exception) {
                // Ignorar: sesión corrupta; el usuario volverá a iniciar sesión.
            }
            null
        }
    }

    suspend fun eliminar(context: Context) = withContext(Dispatchers.IO) {
        db(context).usuarioDao().eliminarTodos()
    }
}
