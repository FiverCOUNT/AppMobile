package com.factapp.jhonny.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Paleta exclusiva para menús y pantallas de emisión de comprobantes. */
object ComprobanteEmitColors {
    val background = Color(0xFFE8EFF8)
    val surface = Color(0xFFFFFFFF)
    val surfaceSoft = Color(0xFFF3F7FC)
    val primary = Color(0xFF1A4570)
    val primaryDeep = Color(0xFF123456)
    val accent = Color(0xFF3D7EC4)
    val accentBright = Color(0xFF6BA3E0)
    val accentSoft = Color(0xFFD4E5F5)
    val onPrimary = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF1A3654)
    val textSecondary = Color(0xFF516B82)
    val border = Color(0xFFACC4DB)
    val borderFocused = Color(0xFF3D7EC4)
    /** Color único para barra de estado del sistema y top bar. */
    val topBar = Color(0xFF0B2341)
    val headerStart = topBar
    val headerEnd = Color(0xFF1C4678)
    val headerBottom = Color(0xFF2F6BA7)
    val badge = Color(0xFF2F6494)

    val headerBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                headerStart,
                headerEnd,
                headerBottom,
            ),
        )
}
