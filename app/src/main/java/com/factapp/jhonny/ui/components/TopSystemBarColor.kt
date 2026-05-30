package com.factapp.jhonny.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

@Composable
fun ApplySystemBarsColor(
    statusBarColor: Color,
    navigationBarColor: Color = statusBarColor,
    lightStatusBarIcons: Boolean = false,
    lightNavigationBarIcons: Boolean = lightStatusBarIcons,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context.findActivity()
        val statusArgb = statusBarColor.copy(alpha = 1f).toArgb()
        val navArgb = navigationBarColor.copy(alpha = 1f).toArgb()
        SideEffect {
            activity?.window?.let { window ->
                @Suppress("DEPRECATION")
                window.statusBarColor = statusArgb
                @Suppress("DEPRECATION")
                window.navigationBarColor = navArgb
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = lightStatusBarIcons
                controller.isAppearanceLightNavigationBars = lightNavigationBarIcons
            }
        }
    }
}

/**
 * Mismo color opaco en la barra de estado del sistema que en [ComprobanteEmitHeader].
 * El header también pinta ese color detrás de la barra (edge-to-edge).
 */
@Composable
fun ApplyAppTopBarColor(
    color: Color = ComprobanteEmitColors.topBar,
) {
    ApplySystemBarsColor(
        statusBarColor = color,
        lightStatusBarIcons = false,
        lightNavigationBarIcons = false,
    )
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
