package com.factapp.jhonny.network

import android.content.Context
import com.factapp.jhonny.data.local.FactAppDatabase

/** Acceso local a la empresa del usuario (no confundir con clientes). */
object CompanyRepository {

    suspend fun obtenerEmpresaLocal(context: Context) =
        FactAppDatabase.obtener(context).companyDao().listarTodas()
}
