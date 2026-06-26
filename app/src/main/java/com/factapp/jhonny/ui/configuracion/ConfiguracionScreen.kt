package com.factapp.jhonny.ui.configuracion

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.modelos.esAdmin
import com.factapp.jhonny.network.ConfiguracionRepository
import com.factapp.jhonny.network.dto.model.ActualizacionesAppApi
import com.factapp.jhonny.network.dto.model.ConfiguracionAppApi
import com.factapp.jhonny.network.dto.model.SoporteAppApi
import com.factapp.jhonny.network.dto.model.tieneDatos
import com.factapp.jhonny.network.dto.model.urlEfectiva
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.launch

private val C = ComprobanteEmitColors

@Composable
fun ConfiguracionScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmarCierre by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var configApp by remember { mutableStateOf<ConfiguracionAppApi?>(null) }

    LaunchedEffect(Unit) {
        cargando = true
        error = null
        ConfiguracionRepository.obtener(context, forzarRed = true)
            .onSuccess { configApp = it }
            .onFailure { error = it.message ?: "No se pudo cargar la configuración" }
        cargando = false
    }

    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            title = { Text("Cerrar sesión") },
            text = { Text("Se borrarán los tokens guardados en este dispositivo. ¿Continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarCierre = false
                        onCerrarSesion()
                    },
                ) {
                    Text("Cerrar sesión", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierre = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    val company = usuario?.company
    val nombreEmpresa = company?.nombre ?: "Empresa no vinculada"
    val ruc = company?.ruc ?: "Sin RUC"
    val email = usuario?.email ?: "Sin usuario"
    val almacenAsignado = remember(usuario) {
        val nombre = usuario?.almacenNombre?.takeIf { it.isNotBlank() }
        val id = usuario?.almacenId?.takeIf { it.isNotBlank() }
        when {
            nombre != null -> nombre
            id != null -> id
            usuario?.esAdmin() == true -> "Sin asignar (administrador)"
            else -> "Sin almacén asignado"
        }
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = "Configuración",
        subtitulo = "Soporte y actualizaciones",
        icono = Icons.Default.Settings,
        onVolver = onVolver,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 28.dp),
        ) {
            ConfiguracionEmpresaCard(
                nombreEmpresa = nombreEmpresa,
                ruc = ruc,
                email = email,
                almacenAsignado = almacenAsignado,
            )

            Spacer(Modifier.height(20.dp))

            when {
                cargando -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = C.accent)
                }
                error != null && configApp == null -> Text(
                    text = error!!,
                    color = Color(0xFFC62828),
                    fontSize = 14.sp,
                )
                else -> {
                    ConfiguracionSoporteSection(
                        soporte = configApp?.soporte,
                        onAbrirUrl = { url -> abrirEnNavegador(context, url) },
                    )
                    Spacer(Modifier.height(18.dp))
                    ConfiguracionActualizacionesSection(
                        actualizaciones = configApp?.actualizaciones,
                        onAbrirUrl = { url -> abrirEnNavegador(context, url) },
                    )
                    if (error != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Mostrando datos guardados. ${error!!}",
                            fontSize = 12.sp,
                            color = C.textSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            ConfiguracionMenuItem(
                icono = Icons.AutoMirrored.Filled.Logout,
                titulo = "Cerrar sesión",
                detalle = "Salir de la cuenta en este dispositivo",
                color = Color(0xFFC62828),
                onClick = { confirmarCierre = true },
            )

            TextButton(
                onClick = {
                    scope.launch {
                        cargando = true
                        error = null
                        ConfiguracionRepository.obtener(context, forzarRed = true)
                            .onSuccess { configApp = it }
                            .onFailure { error = it.message }
                        cargando = false
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Actualizar datos", color = C.accent)
            }
        }
    }
}

@Composable
private fun ConfiguracionSoporteSection(
    soporte: SoporteAppApi?,
    onAbrirUrl: (String) -> Unit,
) {
    Text(
        "Soporte",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = C.primary,
    )
    Spacer(Modifier.height(10.dp))

    if (soporte == null || !soporte.tieneDatos()) {
        ConfiguracionInfoVacia(
            icono = Icons.Default.SupportAgent,
            mensaje = "No hay datos de soporte configurados en el servidor.",
        )
        return
    }

    soporte.desarrollador?.takeIf { it.isNotBlank() }?.let { dev ->
        ConfiguracionDatoFila(
            icono = Icons.Default.SupportAgent,
            titulo = "Desarrollador",
            valor = dev,
        )
        Spacer(Modifier.height(8.dp))
    }

    soporte.telefonos.orEmpty().filter { it.isNotBlank() }.forEach { tel ->
        ConfiguracionDatoFila(
            icono = Icons.Default.Phone,
            titulo = "Teléfono",
            valor = tel,
            onClick = { onAbrirUrl("tel:${tel.filter { c -> c.isDigit() || c == '+' }}") },
        )
        Spacer(Modifier.height(8.dp))
    }

    soporte.whatsapp?.takeIf { it.isNotBlank() }?.let { wa ->
        val digits = wa.filter { it.isDigit() }
        ConfiguracionDatoFila(
            icono = Icons.Default.Phone,
            titulo = "WhatsApp",
            valor = wa,
            onClick = { if (digits.isNotEmpty()) onAbrirUrl("https://wa.me/$digits") },
        )
        Spacer(Modifier.height(8.dp))
    }

    soporte.email?.takeIf { it.isNotBlank() }?.let { mail ->
        ConfiguracionDatoFila(
            icono = Icons.Default.Email,
            titulo = "Correo",
            valor = mail,
            onClick = { onAbrirUrl("mailto:$mail") },
        )
        Spacer(Modifier.height(8.dp))
    }

    soporte.horario?.takeIf { it.isNotBlank() }?.let { horario ->
        ConfiguracionDatoFila(
            icono = Icons.Default.Schedule,
            titulo = "Horario",
            valor = horario,
        )
    }
}

@Composable
private fun ConfiguracionActualizacionesSection(
    actualizaciones: ActualizacionesAppApi?,
    onAbrirUrl: (String) -> Unit,
) {
    Text(
        "Actualizaciones",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = C.primary,
    )
    Spacer(Modifier.height(10.dp))

    val url = actualizaciones?.urlEfectiva()
    if (url.isNullOrBlank()) {
        ConfiguracionInfoVacia(
            icono = Icons.Default.SystemUpdate,
            mensaje = "No hay URL de actualización configurada.",
        )
        return
    }

    ConfiguracionDatoFila(
        icono = Icons.Default.SystemUpdate,
        titulo = "Descargar actualización",
        valor = url,
        onClick = { onAbrirUrl(url) },
    )

    val version = actualizaciones?.versionActual?.takeIf { it.isNotBlank() }
    val minima = actualizaciones?.versionMinima?.takeIf { it.isNotBlank() }
    if (version != null || minima != null) {
        Spacer(Modifier.height(8.dp))
        val detalle = buildList {
            version?.let { add("Versión actual: $it") }
            minima?.let { add("Versión mínima: $it") }
        }.joinToString(" · ")
        Text(detalle, fontSize = 12.sp, color = C.textSecondary, lineHeight = 17.sp)
    }
}

@Composable
private fun ConfiguracionInfoVacia(
    icono: ImageVector,
    mensaje: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = C.surfaceSoft,
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.35f)),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icono, null, tint = C.textSecondary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(mensaje, fontSize = 13.sp, color = C.textSecondary, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ConfiguracionDatoFila(
    icono: ImageVector,
    titulo: String,
    valor: String,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = C.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = C.accentSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, null, tint = C.accent, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 11.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(
                    valor,
                    fontSize = 14.sp,
                    color = if (onClick != null) C.accent else C.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun ConfiguracionEmpresaCard(
    nombreEmpresa: String,
    ruc: String,
    email: String,
    almacenAsignado: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = C.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = C.accentSoft,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Business, null, tint = C.accent, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        nombreEmpresa,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = C.textPrimary,
                        lineHeight = 21.sp,
                    )
                    Text(
                        "RUC $ruc",
                        fontSize = 13.sp,
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = C.surfaceSoft,
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Sesión iniciada", fontSize = 11.sp, color = C.textSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(3.dp))
                    Text(email, fontSize = 14.sp, color = C.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = C.surfaceSoft,
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = Color(0xFF00838F),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Almacén asignado",
                            fontSize = 11.sp,
                            color = C.textSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            almacenAsignado,
                            fontSize = 14.sp,
                            color = C.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfiguracionMenuItem(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    color: Color,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = C.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, C.border.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = color.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, color = C.textPrimary, fontSize = 15.sp)
                Text(detalle, color = C.textSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

private fun abrirEnNavegador(context: android.content.Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
