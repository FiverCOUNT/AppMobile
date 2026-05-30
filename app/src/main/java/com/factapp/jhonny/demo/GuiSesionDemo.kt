package com.factapp.jhonny.demo

import android.content.Context
import com.factapp.jhonny.FactApplication
import com.factapp.jhonny.modelos.Company
import com.factapp.jhonny.modelos.EstadoUsuario
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.dto.model.BusinessTemplate
import com.factapp.jhonny.network.dto.RUC_DEMO_CATALOGO
import java.sql.Timestamp

/**
 * Sesión y empresa de ejemplo cuando el API no responde (solo respaldo para maquetar la UI).
 */
object GuiSesionDemo {

    private const val TOKEN_DEMO = "token-demo-offline"
    private const val EMPRESA_NOMBRE = "Comercial Demo SAC"

    fun crearUsuario(
        email: String = "demo@factapp.pe",
        pin: String = "123456",
    ): Usuario {
        val ahora = Timestamp(System.currentTimeMillis())
        return Usuario(
            email = email.trim().lowercase(),
            contrasena = pin,
            token = TOKEN_DEMO,
            refreshToken = null,
            lastUpdated = ahora,
            estado = EstadoUsuario.ACTIVO,
            company = Company(
                ruc = RUC_DEMO_CATALOGO,
                nombre = EMPRESA_NOMBRE,
                rutaFirma = null,
                rutaLogo = null,
                name_logo = null,
                direccion = "Av. Ejemplo 123, Lima",
                telefono = "999 888 777",
                plantilla = BusinessTemplate.GENERAL,
            ),
        )
    }

    suspend fun guardarEnRoom(context: Context, usuario: Usuario): Usuario {
        val app = context.applicationContext as FactApplication
        val db = app.database
        usuario.company?.let { db.companyDao().insertar(it) }
        db.usuarioDao().insertar(usuario)
        return usuario
    }

    suspend fun entrarConEmailPin(
        context: Context,
        email: String,
        pin: String,
    ): Usuario = guardarEnRoom(context, crearUsuario(email, pin))

    suspend fun entrarInvitado(context: Context): Usuario =
        guardarEnRoom(context, crearUsuario(email = "invitado@factapp.pe", pin = "000000"))
}
