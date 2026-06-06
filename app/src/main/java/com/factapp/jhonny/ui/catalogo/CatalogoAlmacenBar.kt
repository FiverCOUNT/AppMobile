package com.factapp.jhonny.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.ui.inventario.AlmacenSelectorSection
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors

private val C = ComprobanteEmitColors

@Composable
fun CatalogoAlmacenBar(
    esAdmin: Boolean,
    almacenNombre: String?,
    almacenes: List<Almacen>,
    almacenSeleccionadoId: String?,
    onSeleccionarAlmacen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        if (esAdmin) {
            AlmacenSelectorSection(
                titulo = "Almacén",
                subtitulo = "Filtra el catálogo por almacén",
                almacenes = almacenes,
                seleccionadoId = almacenSeleccionadoId,
                onSeleccionar = onSeleccionarAlmacen,
            )
        } else {
            AlmacenEtiquetaFija(nombre = almacenNombre)
        }
    }
}

@Composable
private fun AlmacenEtiquetaFija(nombre: String?) {
    val texto = nombre?.takeIf { it.isNotBlank() } ?: "Sin almacén asignado"
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = C.surfaceSoft,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Warehouse,
                contentDescription = null,
                tint = C.accent,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Almacén: $texto",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = C.textPrimary,
            )
        }
    }
}
