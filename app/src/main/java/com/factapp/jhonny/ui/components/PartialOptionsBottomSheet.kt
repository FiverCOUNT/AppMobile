package com.factapp.jhonny.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

enum class PartialSheetTheme {
    Dashboard,
    Emit,
}

private data class PartialSheetColors(
    val container: Color,
    val title: Color,
    val subtitle: Color,
    val card: Color,
    val iconCircle: Color,
    val iconTint: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color?,
    val arrow: Color,
)

private fun colorsFor(theme: PartialSheetTheme): PartialSheetColors = when (theme) {
    PartialSheetTheme.Dashboard -> PartialSheetColors(
        container = Color(0xFFEBEBEB),
        title = Color(0xFF003B7A),
        subtitle = Color(0xFF5A6578),
        card = Color(0xFFFFFFFF),
        iconCircle = Color(0xFFEBEBEB),
        iconTint = Color(0xFF003B7A),
        textPrimary = Color(0xFF003B7A),
        textSecondary = Color(0xFF5A6578),
        border = null,
        arrow = Color(0xFF003B7A),
    )
    PartialSheetTheme.Emit -> PartialSheetColors(
        container = ComprobanteEmitColors.background,
        title = ComprobanteEmitColors.primary,
        subtitle = ComprobanteEmitColors.textSecondary,
        card = ComprobanteEmitColors.surface,
        iconCircle = ComprobanteEmitColors.surfaceSoft,
        iconTint = ComprobanteEmitColors.primary,
        textPrimary = ComprobanteEmitColors.primary,
        textSecondary = ComprobanteEmitColors.textSecondary,
        border = ComprobanteEmitColors.border,
        arrow = ComprobanteEmitColors.accent,
    )
}

/**
 * Modal inferior con todas las opciones visibles al abrir. Fondo = background del tema;
 * títulos en color primario de la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartialOptionsBottomSheet(
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    theme: PartialSheetTheme = PartialSheetTheme.Emit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = colorsFor(theme)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ApplySystemBarsColor(
        statusBarColor = palette.container,
        navigationBarColor = palette.container,
        lightStatusBarIcons = true,
        lightNavigationBarIcons = true,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.container,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.title,
                ),
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = palette.subtitle,
                    lineHeight = 20.sp,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun PartialOptionCard(
    icon: ImageVector,
    titulo: String,
    detalle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    theme: PartialSheetTheme = PartialSheetTheme.Emit,
    iconTint: Color? = null,
    iconBackground: Color? = null,
    tituloColor: Color? = null,
    showArrow: Boolean = true,
) {
    val palette = colorsFor(theme)
    val effectiveIconTint = iconTint ?: palette.iconTint
    val effectiveIconBackground = iconBackground ?: palette.iconCircle
    val effectiveTitleColor = tituloColor ?: palette.textPrimary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.card),
        border = palette.border?.let { BorderStroke(1.dp, it) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (theme == PartialSheetTheme.Dashboard) 1.dp else 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = effectiveIconBackground,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = titulo,
                        tint = effectiveIconTint,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.SemiBold,
                    color = effectiveTitleColor,
                )
                Text(
                    text = detalle,
                    fontSize = 13.sp,
                    color = palette.textSecondary,
                    lineHeight = 18.sp,
                )
            }
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = palette.arrow,
                )
            }
        }
    }
}
