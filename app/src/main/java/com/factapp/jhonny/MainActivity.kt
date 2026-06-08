package com.factapp.jhonny

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.ui.almacenes.AlmacenesScreen
import com.factapp.jhonny.ui.clientes.ClientesScreen
import com.factapp.jhonny.ui.configuracion.ConfiguracionScreen
import com.factapp.jhonny.ui.compras.ComprasScreen
import com.factapp.jhonny.ui.comprobantes.ComprobantesEmitidosScreen
import com.factapp.jhonny.ui.inventario.InventarioSaldoScreen
import com.factapp.jhonny.ui.inventario.SalidasScreen
import com.factapp.jhonny.ui.inventario.RegistrarSalidaScreen
import com.factapp.jhonny.ui.inventario.HistorialInventarioScreen
import com.factapp.jhonny.ui.inventario.IngresosScreen
import com.factapp.jhonny.ui.theme.EasyTheme

/**
 * Debe ser [FragmentActivity] para que [androidx.biometric.BiometricPrompt] funcione.
 */
class MainActivity : FragmentActivity() {

  private sealed interface Pantalla {
    data object Login : Pantalla
    data object Dashboard : Pantalla
    data object Catalogo : Pantalla
    data object Clientes : Pantalla
    data object Compras : Pantalla
    data object ComprobantesEmitidos : Pantalla
    data object Salidas : Pantalla
    data object NuevaSalida : Pantalla
    data object Ingresos : Pantalla
    data object HistorialInventario : Pantalla
    data object InventarioSaldo : Pantalla
    data object Almacenes : Pantalla
    data object Configuracion : Pantalla
    data class EmitirComprobante(val tipo: TipoComprobante) : Pantalla
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    setContent {
      EasyTheme {
        var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Login) }
        var pantallaAnterior by remember { mutableStateOf<Pantalla>(Pantalla.Dashboard) }
        var usuarioSesion by remember { mutableStateOf<Usuario?>(null) }

        when (val actual = pantalla) {
          Pantalla.Login -> {
            LoginScreen(
              modifier = Modifier.fillMaxSize(),
              onBiometricSuccess = { usuario ->
                usuarioSesion = usuario
                pantalla = Pantalla.Dashboard
              },
              onLoginBackendExitoso = { usuario ->
                usuarioSesion = usuario
                pantalla = Pantalla.Dashboard
              },
              onLoginBackendFallo = { mensaje ->
                Toast.makeText(
                  this@MainActivity,
                  mensaje,
                  Toast.LENGTH_LONG,
                ).show()
              },
            )
          }

          Pantalla.Dashboard -> {
            if (usuarioSesion == null) {
              LaunchedEffect(Unit) {
                pantalla = Pantalla.Login
              }
            } else {
            DashboardScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onNuevaFactura = {
                pantalla = Pantalla.EmitirComprobante(TipoComprobante.FACTURA)
              },
              onEmitirComprobante = { tipo ->
                pantalla = Pantalla.EmitirComprobante(tipo)
              },
              onCompras = {
                pantallaAnterior = Pantalla.Dashboard
                pantalla = Pantalla.Compras
              },
              onCatalogo = {
                pantallaAnterior = Pantalla.Dashboard
                pantalla = Pantalla.Catalogo
              },
              onClientes = {
                pantallaAnterior = Pantalla.Dashboard
                pantalla = Pantalla.Clientes
              },
              onSalidas = { pantalla = Pantalla.Salidas },
              onIngresos = { pantalla = Pantalla.Ingresos },
              onHistorial = { pantalla = Pantalla.HistorialInventario },
              onInventario = { pantalla = Pantalla.InventarioSaldo },
              onAlmacenes = { pantalla = Pantalla.Almacenes },
              onComprobantesEmitidos = {
                pantallaAnterior = Pantalla.Dashboard
                pantalla = Pantalla.ComprobantesEmitidos
              },
              onConfiguracion = {
                pantallaAnterior = Pantalla.Dashboard
                pantalla = Pantalla.Configuracion
              },
            )
            }
          }

          Pantalla.ComprobantesEmitidos -> {
            ComprobantesEmitidosScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = pantallaAnterior },
            )
          }

          Pantalla.Catalogo -> {
            CatalogoScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = pantallaAnterior },
            )
          }

          Pantalla.Clientes -> {
            ClientesScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = pantallaAnterior },
            )
          }

          Pantalla.Compras -> {
            ComprasScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = pantallaAnterior },
            )
          }

          Pantalla.Salidas -> {
            SalidasScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = Pantalla.Dashboard },
              onNuevaSalida = { pantalla = Pantalla.NuevaSalida },
            )
          }

          Pantalla.NuevaSalida -> {
            RegistrarSalidaScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = Pantalla.Salidas },
              onRegistrada = {
                Toast.makeText(
                  this@MainActivity,
                  "Salida registrada",
                  Toast.LENGTH_SHORT,
                ).show()
              },
            )
          }

          Pantalla.Ingresos -> {
            IngresosScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = Pantalla.Dashboard },
              onIrACatalogo = {
                pantallaAnterior = Pantalla.Ingresos
                pantalla = Pantalla.Catalogo
              },
            )
          }

          Pantalla.HistorialInventario -> {
            HistorialInventarioScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = Pantalla.Dashboard },
            )
          }

          Pantalla.InventarioSaldo -> {
            InventarioSaldoScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = Pantalla.Dashboard },
            )
          }

          Pantalla.Almacenes -> {
            AlmacenesScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = Pantalla.Dashboard },
            )
          }

          Pantalla.Configuracion -> {
            ConfiguracionScreen(
              modifier = Modifier.fillMaxSize(),
              usuario = usuarioSesion,
              onVolver = { pantalla = pantallaAnterior },
            )
          }

          is Pantalla.EmitirComprobante -> {
            EmitirComprobanteScreen(
              modifier = Modifier.fillMaxSize(),
              tipo = actual.tipo,
              usuario = usuarioSesion,
              onVolver = { pantalla = Pantalla.Dashboard },
              onEmitir = { tipo ->
                Toast.makeText(
                  this@MainActivity,
                  "${tipo.titulo} enviado a SUNAT (demo)",
                  Toast.LENGTH_SHORT,
                ).show()
                pantalla = Pantalla.Dashboard
              },
            )
          }
        }
      }
    }
  }
}
