package com.factapp.jhonny

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.factapp.jhonny.FactApplication
import com.factapp.jhonny.data.local.toEntity
import com.factapp.jhonny.demo.GuiSesionDemo
import com.factapp.jhonny.extras.LoadingOverlay
import com.factapp.jhonny.network.RetrofitClient
import com.factapp.jhonny.network.dto.request.LoginRequest
import com.factapp.jhonny.network.dto.model.Usuario as UsuarioDto
import com.factapp.jhonny.network.mensajeLogin
import com.factapp.jhonny.modelos.Usuario as UsuarioLocal
import com.factapp.jhonny.ui.theme.EasyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Ruta o URL del logo (archivo, `content://`, `https://`, etc.). Dejalo vacio hasta tener la imagen. */
private const val LOGIN_LOGO_RUTA: String = "https://th.bing.com/th/id/R.9d84de07e43c64cf79db113a1ffc0120?rik=w7Ba5PaTDHQ9SA&riu=http%3a%2f%2f3.bp.blogspot.com%2f-51fY7E_ChOo%2fU17muLIroPI%2fAAAAAAAAAgU%2fFhGJln5vw7I%2fs1600%2fpepa.jpg&ehk=xUhVcIFVy4mjbdLIWrFwmEALP6CM3dYvN3ogCbp60aI%3d&risl=&pid=ImgRaw&r=0"

private val loginLogoShape = RoundedCornerShape(10.dp)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    /** Huella: restaura la última sesión guardada en Room tras un login con PIN. */
    onBiometricSuccess: (UsuarioLocal) -> Unit = {},
    /** Login con email + PIN: usuario ya guardado en Room (entidad local). */
    onLoginBackendExitoso: (UsuarioLocal) -> Unit = {},
    /** Login fallido: mensaje listo para mostrarlo en otro composable (Snackbar, dialogo, etc.). */
    onLoginBackendFallo: (mensaje: String) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var recordarSesion by remember { mutableStateOf(true) }
    var cargando by remember { mutableStateOf(false) }

    val emailValido = email.contains("@") && email.contains(".")
    val pinValido = pin.length == 6

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(loginLogoShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = loginLogoShape,
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (LOGIN_LOGO_RUTA.isNotBlank()) {
                            AsyncImage(
                                model = LOGIN_LOGO_RUTA,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(loginLogoShape),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ingresa con email y PIN de 6 digitos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("Email") },
                    placeholder = { Text("correo@ejemplo.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { nuevo ->
                        pin = nuevo.filter { it.isDigit() }.take(6)
                    },
                    label = { Text("PIN") },
                    placeholder = { Text("******") },
                    supportingText = { Text("${pin.length}/6") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = recordarSesion,
                            onCheckedChange = { recordarSesion = it },
                        )
                        Text("Recordarme")
                    }

                    TextButton(onClick = { }) {
                        Text("Olvide mi PIN")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val app = context.applicationContext as? FactApplication
                        if (app == null) {
                            onLoginBackendFallo("No se pudo acceder a la base de datos de la app.")
                            return@Button
                        }
                        scope.launch {
                            cargando = true
                            try {
                                val usuarioGuardado = withContext(Dispatchers.IO) {
                                    val dto: UsuarioDto = RetrofitClient.api.autenticar(
                                        LoginRequest(email = email, pin = pin),
                                    )
                                    val entidad = dto.toEntity()
                                    val db = app.database
                                    entidad.company?.let { db.companyDao().insertar(it) }
                                    db.usuarioDao().insertar(entidad)
                                    entidad
                                }
                                onLoginBackendExitoso(usuarioGuardado)
                            } catch (e: Exception) {
                                val usuarioSimulado = GuiSesionDemo.entrarConEmailPin(
                                    context = context,
                                    email = email,
                                    pin = pin,
                                )
                                Toast.makeText(
                                    context,
                                    "Sin conexión al servidor. Usando datos de demostración.",
                                    Toast.LENGTH_LONG,
                                ).show()
                                onLoginBackendExitoso(usuarioSimulado)
                            } finally {
                                cargando = false
                            }
                        }
                    },
                    enabled = emailValido && pinValido && !cargando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Iniciar sesion")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        if (activity == null) {
                            Toast.makeText(
                                context,
                                "No se pudo iniciar biometria en este contexto.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@OutlinedButton
                        }

                        val canAuth = BiometricManager.from(context).canAuthenticate(
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                        )

                        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                            val message = when (canAuth) {
                                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                                    "Este dispositivo no tiene sensor biometrico."
                                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                                    "El sensor biometrico no esta disponible ahora."
                                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                                    "Configura huella o bloqueo del dispositivo primero."
                                else -> "Biometria no disponible."
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }

                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(
                            activity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(
                                    result: BiometricPrompt.AuthenticationResult,
                                ) {
                                    super.onAuthenticationSucceeded(result)
                                    val app = context.applicationContext as? FactApplication
                                    if (app == null) {
                                        onLoginBackendFallo("No se pudo acceder a la base de datos de la app.")
                                        return
                                    }
                                    scope.launch {
                                        cargando = true
                                        try {
                                            val usuario = withContext(Dispatchers.IO) {
                                                app.database.usuarioDao().obtenerSesionReciente()
                                            }
                                            if (usuario != null) {
                                                onBiometricSuccess(usuario)
                                            } else {
                                                onLoginBackendFallo(
                                                    "Primero inicia sesión con email y PIN.",
                                                )
                                            }
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                }

                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence,
                                ) {
                                    super.onAuthenticationError(errorCode, errString)
                                    Toast.makeText(
                                        context,
                                        errString,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                        )

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Acceso seguro")
                            .setSubtitle("Valida tu identidad para entrar")
                            .setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                            )
                            .build()

                        biometricPrompt.authenticate(promptInfo)
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Entrar con huella")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { }, enabled = !cargando) {
                        Text("Crear cuenta")
                    }
                    TextButton(
                        onClick = {
                            val app = context.applicationContext as? FactApplication
                            if (app == null) {
                                onLoginBackendFallo("No se pudo acceder a la base de datos de la app.")
                                return@TextButton
                            }
                            scope.launch {
                                cargando = true
                                try {
                                    val invitado = GuiSesionDemo.entrarInvitado(context)
                                    onLoginBackendExitoso(invitado)
                                } finally {
                                    cargando = false
                                }
                            }
                        },
                        enabled = !cargando,
                    ) {
                        Text("Entrar como invitado")
                    }
                }
            }

            LoadingOverlay(
                visible = cargando,
                message = "Validando sesion...",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    EasyTheme {
        LoginScreen()
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
