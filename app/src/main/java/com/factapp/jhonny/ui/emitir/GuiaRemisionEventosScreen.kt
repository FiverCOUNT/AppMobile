package com.factapp.jhonny.ui.emitir

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factapp.jhonny.GuiaRemisionEventoOpcion
import com.factapp.jhonny.modelos.Usuario
import com.factapp.jhonny.network.ComprobanteRepository
import com.factapp.jhonny.network.dto.companyRucParaCatalogo
import com.factapp.jhonny.network.dto.etiquetaTipo
import com.factapp.jhonny.network.dto.model.ComprobanteEstado
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.InvoiceTipoDoc
import com.factapp.jhonny.ui.components.AppEmitScaffold
import com.factapp.jhonny.ui.theme.ComprobanteEmitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val C = ComprobanteEmitColors

private enum class ModoGreEvento { REGISTRO, BAJA }

private data class EventoSunatOption(val codigo: String, val titulo: String)

private val EVENTOS_SUNAT = listOf(
    EventoSunatOption("01", "Inicio de traslado"),
    EventoSunatOption("02", "Llegada al punto"),
    EventoSunatOption("03", "Entrega de bienes"),
)

private fun Invoice.esGuiaParaEventos(): Boolean {
    val esGre = tipo == InvoiceTipoDoc.GUIA_EMISION ||
        tipo == InvoiceTipoDoc.GUIA_TRANSPORTISTA ||
        tipoDoc == InvoiceTipoDoc.COD_GUIA ||
        tipoDoc == InvoiceTipoDoc.COD_GUIA_TRANSPORTISTA
    val estadoOk = estado == ComprobanteEstado.ACEPTADO || estado == ComprobanteEstado.ENVIADO
    return esGre && estadoOk
}

@Composable
fun GuiaRemisionEventosScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    modoInicial: GuiaRemisionEventoOpcion = GuiaRemisionEventoOpcion.REGISTRO_EVENTOS,
    onVolver: () -> Unit,
) {
    BackHandler(onBack = onVolver)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val companyRuc = usuario.companyRucParaCatalogo().orEmpty()
    val token = usuario?.token

    var cargando by remember { mutableStateOf(true) }
    var guias by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var guiaSeleccionada by remember { mutableStateOf<Invoice?>(null) }
    var modo by remember {
        mutableStateOf(
            if (modoInicial == GuiaRemisionEventoOpcion.COMUNICACION_BAJA) {
                ModoGreEvento.BAJA
            } else {
                ModoGreEvento.REGISTRO
            },
        )
    }
    var codigoEvento by remember { mutableStateOf("01") }
    var detalleEvento by remember { mutableStateOf("") }
    var motivoBaja by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }

    LaunchedEffect(companyRuc, token) {
        cargando = true
        withContext(Dispatchers.IO) {
            ComprobanteRepository.listarEmitidos(companyRuc, token)
                .onSuccess { lista -> guias = lista.filter { it.esGuiaParaEventos() } }
        }
        cargando = false
    }

    AppEmitScaffold(
        modifier = modifier.fillMaxSize(),
        titulo = if (modo == ModoGreEvento.BAJA) "Comunicación de baja" else "Registro de eventos",
        subtitulo = "Guías aceptadas o enviadas a SUNAT",
        icono = if (modo == ModoGreEvento.BAJA) Icons.Default.Cancel else Icons.Default.Event,
        onVolver = onVolver,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = modo == ModoGreEvento.REGISTRO,
                    onClick = { modo = ModoGreEvento.REGISTRO },
                    label = { Text("Eventos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = C.accent,
                        selectedLabelColor = C.onPrimary,
                    ),
                )
                FilterChip(
                    selected = modo == ModoGreEvento.BAJA,
                    onClick = { modo = ModoGreEvento.BAJA },
                    label = { Text("Baja") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = C.accent,
                        selectedLabelColor = C.onPrimary,
                    ),
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = C.accent)
                }
                guias.isEmpty() -> Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No hay guías aceptadas para registrar eventos.",
                        color = C.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                else -> {
                    Text("Selecciona la guía", fontWeight = FontWeight.SemiBold, color = C.primary)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(guias, key = { it.id }) { guia ->
                            val seleccionada = guiaSeleccionada?.id == guia.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { guiaSeleccionada = guia },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (seleccionada) C.accentSoft else C.surface,
                                ),
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(
                                        guia.etiquetaCompleta,
                                        fontWeight = FontWeight.Bold,
                                        color = C.primary,
                                    )
                                    Text(
                                        "${guia.etiquetaTipo()} · ${guia.receptor.nombre}",
                                        fontSize = 12.sp,
                                        color = C.textSecondary,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = C.border.copy(alpha = 0.4f))
                    Spacer(Modifier.height(10.dp))

                    if (modo == ModoGreEvento.REGISTRO) {
                        Text("Tipo de evento SUNAT", fontWeight = FontWeight.SemiBold, color = C.primary)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EVENTOS_SUNAT.forEach { evento ->
                                FilterChip(
                                    selected = codigoEvento == evento.codigo,
                                    onClick = { codigoEvento = evento.codigo },
                                    label = { Text(evento.titulo, fontSize = 12.sp) },
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = detalleEvento,
                            onValueChange = { detalleEvento = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Detalle opcional") },
                            placeholder = { Text("Observación del evento") },
                            minLines = 2,
                        )
                    } else {
                        OutlinedTextField(
                            value = motivoBaja,
                            onValueChange = { motivoBaja = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Motivo de baja") },
                            placeholder = { Text("Describe el motivo de la anulación") },
                            minLines = 3,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        enabled = !enviando && guiaSeleccionada != null &&
                            (modo == ModoGreEvento.REGISTRO || motivoBaja.isNotBlank()),
                        onClick = {
                            val guia = guiaSeleccionada ?: return@TextButton
                            enviando = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    if (modo == ModoGreEvento.REGISTRO) {
                                        ComprobanteRepository.registrarGreEvento(
                                            companyRuc = companyRuc,
                                            token = token,
                                            comprobanteId = guia.id,
                                            codigoEvento = codigoEvento,
                                            detalle = detalleEvento.takeIf { it.isNotBlank() },
                                        )
                                    } else {
                                        ComprobanteRepository.comunicarGreBaja(
                                            companyRuc = companyRuc,
                                            token = token,
                                            comprobanteId = guia.id,
                                            motivo = motivoBaja.trim(),
                                        )
                                    }
                                }
                                enviando = false
                                result.fold(
                                    onSuccess = { resp ->
                                        android.widget.Toast.makeText(
                                            context,
                                            resp.message ?: if (resp.success) "Operación exitosa" else "No se completó",
                                            android.widget.Toast.LENGTH_LONG,
                                        ).show()
                                        if (resp.success) onVolver()
                                    },
                                    onFailure = {
                                        android.widget.Toast.makeText(
                                            context,
                                            it.message ?: "Error al comunicar con SUNAT",
                                            android.widget.Toast.LENGTH_LONG,
                                        ).show()
                                    },
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (enviando) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (modo == ModoGreEvento.BAJA) "Comunicar baja a SUNAT" else "Registrar evento",
                                fontWeight = FontWeight.Bold,
                                color = C.accent,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
