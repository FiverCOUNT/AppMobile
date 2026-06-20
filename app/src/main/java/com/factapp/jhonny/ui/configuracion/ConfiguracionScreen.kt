package com.factapp.jhonny.ui.configuracion

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.modelos.esAdmin
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun ConfiguracionScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    var confirmarCierre by remember { mutableStateOf(false) }
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
        subtitulo = "Preferencias y datos de tu empresa",
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

            Spacer(Modifier.height(18.dp))

            Text(
                "Menú de configuración",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = C.primary,
            )
            Spacer(Modifier.height(10.dp))

            ConfiguracionMenuItem(
                icono = Icons.Default.Business,
                titulo = "Datos de empresa",
                detalle = "Razón social, RUC, dirección y contacto",
                color = C.accent,
            )
            Spacer(Modifier.height(10.dp))
            ConfiguracionMenuItem(
                icono = Icons.Default.Print,
                titulo = "Impresión y documentos",
                detalle = "PDF, constancias, firma y formato de salida",
                color = Color(0xFF1565C0),
            )
            Spacer(Modifier.height(10.dp))
            ConfiguracionMenuItem(
                icono = Icons.Default.Lock,
                titulo = "Seguridad",
                detalle = "Sesión, acceso biométrico y credenciales",
                color = Color(0xFFC62828),
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "Soporte",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = C.primary,
            )
            Spacer(Modifier.height(10.dp))
            ConfiguracionMenuItem(
                icono = Icons.Default.SupportAgent,
                titulo = "Soporte técnico",
                detalle = "Ayuda con configuración, inventario y comprobantes",
                color = Color(0xFF2E7D32),
            )
            Spacer(Modifier.height(10.dp))
            ConfiguracionMenuItem(
                icono = Icons.Default.Code,
                titulo = "Datos del desarrollador",
                detalle = "FactApp Mobile · Soporte técnico",
                color = Color(0xFF6A1B9A),
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "Cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = C.primary,
            )
            Spacer(Modifier.height(10.dp))
            ConfiguracionMenuItem(
                icono = Icons.Default.Person,
                titulo = "Usuario activo",
                detalle = email,
                color = C.textSecondary,
            )
            Spacer(Modifier.height(10.dp))
            ConfiguracionMenuItem(
                icono = Icons.Default.Warehouse,
                titulo = "Almacén asignado",
                detalle = almacenAsignado,
                color = Color(0xFF00838F),
            )
            Spacer(Modifier.height(10.dp))
            ConfiguracionMenuItem(
                icono = Icons.AutoMirrored.Filled.Logout,
                titulo = "Cerrar sesión",
                detalle = "Salir de la cuenta en este dispositivo",
                color = Color(0xFFC62828),
                onClick = { confirmarCierre = true },
            )
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
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = C.textSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
