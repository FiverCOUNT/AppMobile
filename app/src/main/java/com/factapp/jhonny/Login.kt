package com.factapp.jhonny

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.factapp.jhonny.data.local.LoginPreferences
import com.factapp.jhonny.extras.LoadingOverlay
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.AuthRepository
import com.factapp.jhonny.network.mensajeAuth
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import com.factapp.jhonny.ui.theme.EasyTheme
import kotlinx.coroutines.launch

/** Ruta o URL del logo (archivo, `content://`, `https://`, etc.). */
private const val LOGIN_LOGO_RUTA: String = "https://th.bing.com/th/id/R.9d84de07e43c64cf79db113a1ffc0120?rik=w7Ba5PaTDHQ9SA&riu=http%3a%2f%2f3.bp.blogspot.com%2f-51fY7E_ChOo%2fU17muLIroPI%2fAAAAAAAAAgU%2fFhGJln5vw7I%2fs1600%2fpepa.jpg&ehk=xUhVcIFVy4mjbdLIWrFwmEALP6CM3dYvN3ogCbp60aI%3d&risl=&pid=ImgRaw&r=0"

private val C = ComprobanteEmitColors
private val loginCardShape = RoundedCornerShape(28.dp)
private val loginLogoShape = RoundedCornerShape(20.dp)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    /** Huella: renueva tokens con `/api/auth/refresh` o restaura sesión local si no hay red. */
    onBiometricSuccess: (Usuario) -> Unit = {},
    /** Login con email + PIN contra BackEndEasy; sesión guardada en Room. */
    onLoginBackendExitoso: (Usuario) -> Unit = {},
    onLoginBackendFallo: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var recordarSesion by remember { mutableStateOf(true) }
    var cargando by remember { mutableStateOf(false) }
    var sesionPersistida by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val recordar = LoginPreferences.recordarSesion(context)
        recordarSesion = recordar
        if (recordar) {
            LoginPreferences.emailRecordado(context)?.let { email = it }
        }
        sesionPersistida = AuthRepository.puedeUsarBiometria(context)
    }

    val emailValido = email.contains("@") && email.contains(".")
    val pinValido = pin.length >= 4
    val puedeUsarHuella = recordarSesion && sesionPersistida
    val fieldColors = loginFieldColors()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        C.headerStart,
                        C.headerEnd,
                        C.headerBottom,
                        C.background,
                        C.background,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 120.dp)
                .clip(CircleShape)
                .background(C.accentBright.copy(alpha = 0.22f)),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 80.dp)
                .clip(CircleShape)
                .background(C.accentSoft.copy(alpha = 0.55f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            LoginHeroSection()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-28).dp)
                    .shadow(12.dp, loginCardShape, clip = false),
                shape = loginCardShape,
                colors = CardDefaults.cardColors(containerColor = C.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = C.textPrimary,
                    )
                    Text(
                        text = "Ingresa tu email y PIN",
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                        color = C.textSecondary,
                        fontSize = 14.sp,
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = C.accent,
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { nuevo ->
                            pin = nuevo.filter { it.isDigit() }.take(32)
                        },
                        label = { Text("PIN") },
                        placeholder = { Text("••••") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = C.accent,
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            autoCorrect = false,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = recordarSesion,
                                onCheckedChange = { recordarSesion = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = C.primary,
                                    checkmarkColor = C.onPrimary,
                                ),
                            )
                            Text(
                                text = "Recordarme",
                                color = C.textPrimary,
                                fontSize = 14.sp,
                            )
                        }

                        TextButton(onClick = { }) {
                            Text(
                                text = "Olvidé mi PIN",
                                color = C.accent,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                cargando = true
                                AuthRepository.login(context, email, pin, recordarSesion)
                                    .onSuccess { usuario ->
                                        onLoginBackendExitoso(usuario)
                                    }
                                    .onFailure { error ->
                                        onLoginBackendFallo(error.mensajeAuth())
                                    }
                                cargando = false
                            }
                        },
                        enabled = emailValido && pinValido && !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = C.primary,
                            contentColor = C.onPrimary,
                            disabledContainerColor = C.primary.copy(alpha = 0.35f),
                            disabledContentColor = C.onPrimary.copy(alpha = 0.7f),
                        ),
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
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
                                        scope.launch {
                                            cargando = true
                                            val renovado = AuthRepository.refreshSesion(context)
                                            if (renovado.isSuccess) {
                                                renovado.getOrNull()?.let { onBiometricSuccess(it) }
                                            } else {
                                                val local = AuthRepository.restaurarSesionLocal(context)
                                                if (local?.token != null) {
                                                    onBiometricSuccess(local)
                                                } else {
                                                    onLoginBackendFallo(
                                                        renovado.exceptionOrNull()?.mensajeAuth()
                                                            ?: "Inicia sesión con email y PIN.",
                                                    )
                                                }
                                            }
                                            cargando = false
                                        }
                                    }

                                    override fun onAuthenticationError(
                                        errorCode: Int,
                                        errString: CharSequence,
                                    ) {
                                        super.onAuthenticationError(errorCode, errString)
                                        Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
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
                        enabled = !cargando && puedeUsarHuella,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = C.accent,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Entrar con huella",
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    }

                    TextButton(
                        onClick = { },
                        enabled = !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        Text(
                            text = "Crear cuenta",
                            color = C.primaryDeep,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        LoadingOverlay(
            visible = cargando,
            message = "Validando sesión...",
        )
    }
}

@Composable
private fun LoginHeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .shadow(10.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(C.surface),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(loginLogoShape)
                    .background(C.accentSoft),
                contentAlignment = Alignment.Center,
            ) {
                if (LOGIN_LOGO_RUTA.isNotBlank()) {
                    AsyncImage(
                        model = LOGIN_LOGO_RUTA,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(loginLogoShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Easy",
            color = C.onPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
        Text(
            text = "Facturación e inventario",
            color = C.onPrimary.copy(alpha = 0.88f),
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = C.textPrimary,
    unfocusedTextColor = C.textPrimary,
    focusedLabelColor = C.accent,
    unfocusedLabelColor = C.textSecondary,
    focusedBorderColor = C.borderFocused,
    unfocusedBorderColor = C.border,
    cursorColor = C.accent,
    focusedLeadingIconColor = C.primary,
    unfocusedLeadingIconColor = C.textSecondary,
    focusedContainerColor = C.surfaceSoft,
    unfocusedContainerColor = C.surface,
)

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    EasyTheme(dynamicColor = false) {
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
