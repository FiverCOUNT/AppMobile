package com.factapp.jhonny.ui.catalogo

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.ui.components.PartialOptionCard
import com.factapp.jhonny.ui.components.PartialOptionsBottomSheet
import com.factapp.jhonny.ui.components.PartialSheetTheme

private val Destructive = Color(0xFFC62828)

@Composable
fun CatalogItemActionSheet(
    item: CatalogItem?,
    onDismiss: () -> Unit,
    onToggleActivo: (CatalogItem) -> Unit,
    onEliminar: (CatalogItem) -> Unit,
) {
    if (item == null) return

    PartialOptionsBottomSheet(
        onDismiss = onDismiss,
        title = "Opciones del ítem",
        subtitle = item.nombre,
        theme = PartialSheetTheme.Emit,
    ) {
        if (item.activo) {
            PartialOptionCard(
                icon = Icons.Outlined.Block,
                titulo = "Desactivar ítem",
                detalle = "Dejará de aparecer en ventas e ingresos",
                iconTint = Destructive,
                tituloColor = Destructive,
                onClick = {
                    onToggleActivo(item)
                    onDismiss()
                },
            )
        } else {
            PartialOptionCard(
                icon = Icons.Outlined.CheckCircle,
                titulo = "Activar ítem",
                detalle = "Volverá a estar disponible en el catálogo",
                onClick = {
                    onToggleActivo(item)
                    onDismiss()
                },
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        PartialOptionCard(
            icon = Icons.Outlined.Delete,
            titulo = "Eliminar ítem",
            detalle = "Quita el producto del catálogo",
            iconTint = Destructive,
            tituloColor = Destructive,
            onClick = {
                onEliminar(item)
                onDismiss()
            },
        )
    }
}
