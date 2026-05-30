package com.factapp.jhonny.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

/** Padding del contenido del Scaffold sin reservar espacio arriba (lo cubre el top bar). */
@Composable
fun scaffoldContentWithoutTopInset(): WindowInsets =
    WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
    )

/** Modal con header propio: sin inset superior (evita franja clara bajo la barra de estado). */
@Composable
fun sheetContentWithoutTopInset(): WindowInsets =
    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
