package com.factapp.jhonny



import android.os.Bundle

import android.widget.Toast

import androidx.activity.compose.setContent

import androidx.activity.SystemBarStyle

import androidx.activity.enableEdgeToEdge

import android.graphics.Color

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Scaffold

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.fragment.app.FragmentActivity

import com.factapp.jhonny.modelos.Usuario

import com.factapp.jhonny.modelos.esAdmin

import com.factapp.jhonny.network.AuthRepository

import com.factapp.jhonny.network.dto.model.Cliente

import kotlinx.coroutines.launch

import com.factapp.jhonny.ui.almacenes.AlmacenesScreen

import com.factapp.jhonny.ui.clientes.ClienteDetalleScreen

import com.factapp.jhonny.ui.clientes.ClientesScreen

import com.factapp.jhonny.ui.components.AppBottomNavTabs

import com.factapp.jhonny.ui.components.AppBottomNavigationBar

import com.factapp.jhonny.ui.components.ApplyAppTopBarColor

import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset

import com.factapp.jhonny.ui.configuracion.ConfiguracionScreen

import com.factapp.jhonny.ui.compras.ComprasScreen

import com.factapp.jhonny.ui.comprobantes.ComprobantesEmitidosScreen

import com.factapp.jhonny.ui.inventario.SalidasScreen

import com.factapp.jhonny.ui.inventario.RegistrarSalidaScreen

import com.factapp.jhonny.ui.inventario.HistorialInventarioScreen

import com.factapp.jhonny.ui.inventario.IngresosScreen

import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

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

    data class NuevaSalida(val clienteInicial: Cliente? = null) : Pantalla

    data class ClienteDetalle(val cliente: Cliente) : Pantalla

    data object Ingresos : Pantalla

    data object HistorialInventario : Pantalla

    data object Almacenes : Pantalla

    data object Configuracion : Pantalla

    data class EmitirComprobante(val tipo: TipoComprobante) : Pantalla

  }



  private fun Pantalla.mostrarBottomBar(): Boolean = when (this) {
    Pantalla.Login, is Pantalla.EmitirComprobante, is Pantalla.NuevaSalida -> false
    else -> true
  }



  private fun Pantalla.tabBottomBar(): Int = when (this) {

    Pantalla.ComprobantesEmitidos -> AppBottomNavTabs.COMPROBANTES

    Pantalla.Configuracion -> AppBottomNavTabs.AJUSTES

    else -> AppBottomNavTabs.INICIO

  }



  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)

    enableEdgeToEdge(

      statusBarStyle = SystemBarStyle.dark(0xFF0B2341.toInt()),

      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),

    )

    setContent {

      EasyTheme {

        var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Login) }

        var pantallaAnterior by remember { mutableStateOf<Pantalla>(Pantalla.Dashboard) }

        var usuarioSesion by remember { mutableStateOf<Usuario?>(null) }

        var abrirMenuEmitir by remember { mutableStateOf(false) }

        var abrirAgregarClientes by remember { mutableStateOf(false) }

        var pantallaRetornoClientes by remember { mutableStateOf<Pantalla?>(null) }

        val scope = rememberCoroutineScope()

        val mostrarBottomBar = pantalla.mostrarBottomBar()

        if (pantalla != Pantalla.Login) {
          ApplyAppTopBarColor(ComprobanteEmitColors.topBar)
        }

        fun irAlDashboard() {

          pantalla = Pantalla.Dashboard

        }



        Scaffold(

          modifier = Modifier.fillMaxSize(),

          containerColor = ComprobanteEmitColors.background,

          contentWindowInsets = scaffoldContentWithoutTopInset(),

          bottomBar = {

            if (mostrarBottomBar) {

              AppBottomNavigationBar(

                tabSeleccionado = pantalla.tabBottomBar(),

                onInicioClick = { irAlDashboard() },

                onEmitirClick = {

                  irAlDashboard()

                  abrirMenuEmitir = true

                },

                onNuevoClick = {

                  pantalla = Pantalla.EmitirComprobante(TipoComprobante.FACTURA)

                },

                onComprobantesClick = {

                  pantallaAnterior = Pantalla.Dashboard

                  pantalla = Pantalla.ComprobantesEmitidos

                },

                onAjustesClick = {

                  pantallaAnterior = Pantalla.Dashboard

                  pantalla = Pantalla.Configuracion

                },

              )

            }

          },

        ) { scaffoldPadding ->

          Box(

            modifier = Modifier

              .fillMaxSize()

              .then(

                if (mostrarBottomBar) {

                  Modifier.padding(bottom = scaffoldPadding.calculateBottomPadding())

                } else {

                  Modifier

                },

              ),

          ) {

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

                    abrirMenuEmitir = abrirMenuEmitir,

                    onMenuEmitirConsumido = { abrirMenuEmitir = false },

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

                    onAlmacenes = { pantalla = Pantalla.Almacenes },

                    onComprobantesEmitidos = {

                      pantallaAnterior = Pantalla.Dashboard

                      pantalla = Pantalla.ComprobantesEmitidos

                    },

                    onConfiguracion = {

                      pantallaAnterior = Pantalla.Dashboard

                      pantalla = Pantalla.Configuracion

                    },

                    onSesionActualizada = { actualizado ->

                      usuarioSesion = actualizado

                    },

                  )

                }

              }



              Pantalla.ComprobantesEmitidos -> {
                ComprobantesEmitidosScreen(
                  modifier = Modifier.fillMaxSize(),
                  usuario = usuarioSesion,
                  onVolver = null,
                )
              }



              Pantalla.Catalogo -> {

                CatalogoScreen(

                  modifier = Modifier.fillMaxSize(),

                  usuario = usuarioSesion,

                  onVolver = { irAlDashboard() },

                )

              }



              Pantalla.Clientes -> {

                ClientesScreen(

                  modifier = Modifier.fillMaxSize(),

                  usuario = usuarioSesion,

                  onVolver = {

                    val retorno = pantallaRetornoClientes

                    pantallaRetornoClientes = null

                    abrirAgregarClientes = false

                    pantalla = retorno ?: Pantalla.Dashboard

                  },

                  onVerCliente = { cliente ->

                    pantalla = Pantalla.ClienteDetalle(cliente)

                  },

                  abrirAgregarInicial = abrirAgregarClientes,

                  onAgregarInicialConsumido = { abrirAgregarClientes = false },

                  onClienteCreado = { cliente ->

                    val retorno = pantallaRetornoClientes

                    pantallaRetornoClientes = null

                    abrirAgregarClientes = false

                    pantalla = when (retorno) {

                      is Pantalla.NuevaSalida -> Pantalla.NuevaSalida(clienteInicial = cliente)

                      else -> retorno ?: Pantalla.Clientes

                    }

                  },

                )

              }



              is Pantalla.ClienteDetalle -> {

                ClienteDetalleScreen(

                  modifier = Modifier.fillMaxSize(),

                  cliente = actual.cliente,

                  usuario = usuarioSesion,

                  onVolver = { pantalla = Pantalla.Clientes },

                  onNuevaEntrega = { cliente ->

                    pantallaAnterior = actual

                    pantalla = Pantalla.NuevaSalida(clienteInicial = cliente)

                  },

                )

              }



              Pantalla.Compras -> {

                if (usuarioSesion?.esAdmin() != true) {

                  LaunchedEffect(Unit) { irAlDashboard() }

                } else {

                  ComprasScreen(

                    modifier = Modifier.fillMaxSize(),

                    usuario = usuarioSesion,

                    onVolver = { irAlDashboard() },

                  )

                }

              }



              Pantalla.Salidas -> {

                SalidasScreen(

                  modifier = Modifier.fillMaxSize(),

                  usuario = usuarioSesion,

                  onVolver = { irAlDashboard() },

                  onNuevaSalida = {

                    pantallaAnterior = Pantalla.Salidas

                    pantalla = Pantalla.NuevaSalida()

                  },

                )

              }



              is Pantalla.NuevaSalida -> {

                RegistrarSalidaScreen(

                  modifier = Modifier.fillMaxSize(),

                  usuario = usuarioSesion,

                  clienteInicial = actual.clienteInicial,

                  onVolver = { pantalla = pantallaAnterior },

                  onRegistrada = {

                    Toast.makeText(

                      this@MainActivity,

                      "Salida registrada",

                      Toast.LENGTH_SHORT,

                    ).show()

                  },

                  onNuevoCliente = {

                    pantallaRetornoClientes = actual

                    abrirAgregarClientes = true

                    pantalla = Pantalla.Clientes

                  },

                )

              }



              Pantalla.Ingresos -> {

                if (usuarioSesion?.esAdmin() != true) {

                  LaunchedEffect(Unit) { irAlDashboard() }

                } else {

                  IngresosScreen(

                    modifier = Modifier.fillMaxSize(),

                    usuario = usuarioSesion,

                    onVolver = { irAlDashboard() },

                    onIrACatalogo = {

                      pantallaAnterior = Pantalla.Ingresos

                      pantalla = Pantalla.Catalogo

                    },

                  )

                }

              }



              Pantalla.HistorialInventario -> {

                HistorialInventarioScreen(

                  modifier = Modifier.fillMaxSize(),

                  usuario = usuarioSesion,

                  onVolver = { irAlDashboard() },

                )

              }



              Pantalla.Almacenes -> {

                if (usuarioSesion?.esAdmin() != true) {

                  LaunchedEffect(Unit) { irAlDashboard() }

                } else {

                  AlmacenesScreen(

                    modifier = Modifier.fillMaxSize(),

                    usuario = usuarioSesion,

                    onVolver = { irAlDashboard() },

                  )

                }

              }



              Pantalla.Configuracion -> {

                ConfiguracionScreen(

                  modifier = Modifier.fillMaxSize(),

                  usuario = usuarioSesion,

                  onVolver = { irAlDashboard() },

                  onCerrarSesion = {

                    scope.launch {

                      AuthRepository.cerrarSesion(this@MainActivity)

                      usuarioSesion = null

                      pantalla = Pantalla.Login

                    }

                  },

                )

              }



              is Pantalla.EmitirComprobante -> {

                EmitirComprobanteScreen(

                  modifier = Modifier.fillMaxSize(),

                  tipo = actual.tipo,

                  usuario = usuarioSesion,

                  onVolver = { irAlDashboard() },

                  onEmitir = {

                    pantalla = Pantalla.Dashboard

                  },

                )

              }

            }

          }

        }

      }

    }

  }

}

