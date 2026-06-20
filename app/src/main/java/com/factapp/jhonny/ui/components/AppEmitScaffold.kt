package com.factapp.jhonny.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

/**
 * Layout estándar: header azul desde y=0 (incluye barra de estado) y contenido debajo.
 */
@Composable
fun AppEmitScaffold(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    detalle: String? = null,
    icono: ImageVector? = null,
    onVolver: (() -> Unit)? = null,
    mostrarDragHandle: Boolean = false,
    containerColor: Color = ComprobanteEmitColors.background,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    ApplyAppTopBarColor(ComprobanteEmitColors.topBar)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor),
    ) {
        Column(Modifier.fillMaxSize()) {
            ComprobanteEmitHeader(
                titulo = titulo,
                subtitulo = subtitulo,
                detalle = detalle,
                icono = icono,
                onVolver = onVolver,
                mostrarDragHandle = mostrarDragHandle,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                content(PaddingValues(0.dp))
            }
            bottomBar()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            floatingActionButton()
        }
    }
}
