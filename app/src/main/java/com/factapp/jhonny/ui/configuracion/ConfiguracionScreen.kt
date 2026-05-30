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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.ui.components.ComprobanteEmitHeader
import com.factapp.jhonny.ui.components.scaffoldContentWithoutTopInset
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun ConfiguracionScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    onVolver: () -> Unit = {},
) {
    BackHandler(onBack = onVolver)

    val company = usuario?.company
    val nombreEmpresa = company?.nombre ?: "Empresa no vinculada"
    val ruc = company?.ruc ?: "Sin RUC"
    val email = usuario?.email ?: "Sin usuario"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = C.background,
        contentWindowInsets = scaffoldContentWithoutTopInset(),
        topBar = {
            ComprobanteEmitHeader(
                titulo = "Configuración",
                subtitulo = "Preferencias y datos de tu empresa",
                icono = Icons.Default.Settings,
                onVolver = onVolver,
            )
        },
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
                detalle = "FactApp Mobile · Versión demo · Contacto de soporte",
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
        }
    }
}

@Composable
private fun ConfiguracionEmpresaCard(
    nombreEmpresa: String,
    ruc: String,
    email: String,
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
