package com.marcosmejia.nexusclass.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.ui.theme.Ambar
import com.marcosmejia.nexusclass.ui.theme.AmbarClaro
import com.marcosmejia.nexusclass.ui.theme.RojoClaro
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.theme.TextoPrincipal
import com.marcosmejia.nexusclass.ui.theme.TextoSecundario
import com.marcosmejia.nexusclass.ui.util.Formato

@Composable
fun BarraSuperior(
    titulo: String,
    onAtras: () -> Unit,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAtras) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
        }
        Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        acciones()
    }
}

@Composable
fun ChipTexto(texto: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun DialogoEliminar(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Eliminar esta tarea?") },
        text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Eliminar", color = RojoError) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun TarjetaTarea(
    t: TareaDto,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    mostrarClase: Boolean = true
) {
    val min = Formato.minutosHasta(t.fechaEntrega) ?: Long.MAX_VALUE
    val vencida = !t.completada && min < 0
    val urgente = !t.completada && min >= 0 && min <= 24 * 60L
    val resaltada = vencida || urgente

    val fondo = when {
        vencida -> RojoClaro
        urgente -> AmbarClaro
        else -> MaterialTheme.colorScheme.surface
    }
    val borde = when {
        vencida -> RojoError
        urgente -> Ambar
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val colorTexto = if (resaltada) TextoPrincipal else MaterialTheme.colorScheme.onSurface
    val colorSec = if (resaltada) TextoSecundario else MaterialTheme.colorScheme.onSurfaceVariant

    val fecha = Formato.fechaTarea(t.fechaEntrega)
    val subtitulo = if (mostrarClase && t.clase != null) "${t.clase.nombreMateria} · $fecha" else fecha

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(1.dp, borde)
    ) {
        Row(
            Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = t.completada, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChipTipo(t.tipo)
                    if (vencida) ChipTexto("Vencida", RojoError)
                    if (urgente) ChipTexto(Formato.restante(t.fechaEntrega) ?: "", Color(0xFFB45309))
                }
                Text(
                    t.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorTexto,
                    textDecoration = if (t.completada) TextDecoration.LineThrough else null
                )
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = colorSec)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colorSec)
        }
    }
}