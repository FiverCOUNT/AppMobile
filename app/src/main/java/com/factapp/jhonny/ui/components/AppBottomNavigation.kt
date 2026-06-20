package com.factapp.jhonny.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NavBg = Color(0xFFFFFFFF)
private val NavNavy = Color(0xFF003B7A)

/** Índices de la barra inferior principal (Inicio = Dashboard). */
object AppBottomNavTabs {
    const val INICIO = 0
    const val EMITIR = 1
    const val NUEVO = 2
    const val COMPROBANTES = 3
    const val AJUSTES = 4
}

private data class AppBottomNavTab(
    val label: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector,
)

private val appBottomNavTabs = listOf(
    AppBottomNavTab("Inicio", Icons.Outlined.Home, Icons.Default.Home),
    AppBottomNavTab("Emitir", Icons.Outlined.SwapHoriz, Icons.Default.Receipt),
    AppBottomNavTab("Nuevo", Icons.Outlined.AddCircleOutline, Icons.Default.Add),
    AppBottomNavTab("Comprob.", Icons.Outlined.ReceiptLong, Icons.Default.ReceiptLong),
    AppBottomNavTab("Ajustes", Icons.Outlined.Settings, Icons.Default.Settings),
)

@Composable
fun AppBottomNavigationBar(
    tabSeleccionado: Int,
    onInicioClick: () -> Unit,
    onEmitirClick: () -> Unit,
    onNuevoClick: () -> Unit,
    onComprobantesClick: () -> Unit,
    onAjustesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavBg,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            appBottomNavTabs.forEachIndexed { index, tab ->
                AppBottomNavItem(
                    tab = tab,
                    selected = tabSeleccionado == index,
                    onClick = {
                        when (index) {
                            AppBottomNavTabs.INICIO -> onInicioClick()
                            AppBottomNavTabs.EMITIR -> onEmitirClick()
                            AppBottomNavTabs.NUEVO -> onNuevoClick()
                            AppBottomNavTabs.COMPROBANTES -> onComprobantesClick()
                            AppBottomNavTabs.AJUSTES -> onAjustesClick()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavItem(
    tab: AppBottomNavTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NavNavy),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tab.iconFilled,
                    contentDescription = tab.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            Icon(
                imageVector = tab.iconOutlined,
                contentDescription = tab.label,
                tint = NavNavy,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = tab.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = NavNavy,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
