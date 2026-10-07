package com.marcosmejia.nexusclass.ui.screens.tareas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.components.ChipTexto
import com.marcosmejia.nexusclass.ui.components.ChipTipo
import com.marcosmejia.nexusclass.ui.components.DialogoEliminar
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.theme.Ambar
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.theme.VerdeExito
import com.marcosmejia.nexusclass.ui.util.Formato

@Composable
fun DetalleTareaScreen(
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onClase: (String) -> Unit,
    vm: DetalleTareaViewModel = viewModel()
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val eliminada by vm.eliminada.collectAsStateWithLifecycle()
    var confirmar by remember { mutableStateOf(false) }

    LaunchedEffect(eliminada) { if (eliminada) onVolver() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.silencioso() }

    Column(Modifier.fillMaxSize()) {
        val tarea = (estado as? DetalleTareaViewModel.Estado.Exito)?.tarea
        BarraSuperior("Detalle de la tarea", onVolver) {
            if (tarea != null) {
                IconButton(onClick = { onEditar(tarea.id) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { confirmar = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = RojoError)
                }
            }
        }

        when (val e = estado) {
            DetalleTareaViewModel.Estado.Cargando -> PantallaCargando("Cargando tarea…")
            is DetalleTareaViewModel.Estado.Error -> PantallaError(e.mensaje, onReintentar = vm::cargar)
            is DetalleTareaViewModel.Estado.Exito -> Contenido(
                t = e.tarea,
                onAlternar = vm::alternar,
                onEditar = { onEditar(e.tarea.id) },
                onClase = onClase
            )
        }
    }

    if (confirmar) {
        DialogoEliminar(
            onConfirmar = { confirmar = false; vm.eliminar() },
            onCancelar = { confirmar = false }
        )
    }
}

@Composable
private fun Contenido(
    t: TareaDto,
    onAlternar: () -> Unit,
    onEditar: () -> Unit,
    onClase: (String) -> Unit
) {
    val min = Formato.minutosHasta(t.fechaEntrega) ?: Long.MAX_VALUE
    val vencida = !t.completada && min < 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipTipo(t.tipo)
            when {
                t.completada -> ChipTexto("Completada", VerdeExito)
                vencida -> ChipTexto("Vencida", RojoError)
                else -> ChipTexto("Pendiente", Color(0xFFB45309))
            }
        }

        Text(t.titulo, style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                FilaDato(
                    "Asignatura",
                    t.clase?.nombreMateria ?: "—",
                    onClick = { onClase(t.claseId) },
                    resaltado = true
                )
                Separador()
                FilaDato("Fecha de entrega", Formato.fechaCompleta(t.fechaEntrega))
                Separador()
                FilaDato(
                    "Tiempo restante",
                    if (t.completada) "Completada" else Formato.restante(t.fechaEntrega) ?: "—"
                )
                if (t.porcentaje != null) {
                    Separador()
                    FilaDato("Valor en la nota", "${t.porcentaje} %")
                }
                if (!t.modalidad.isNullOrBlank()) {
                    Separador()
                    FilaDato("Modalidad", t.modalidad)
                }
                if (!t.notas.isNullOrBlank()) {
                    Separador()
                    FilaDato("Notas", t.notas)
                }
            }
        }

        Button(onClick = onAlternar, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(if (t.completada) "Marcar como pendiente" else "Marcar como completada")
        }
        OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Editar")
        }
    }
}

@Composable
private fun FilaDato(
    etiqueta: String,
    valor: String,
    onClick: (() -> Unit)? = null,
    resaltado: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            color = if (resaltado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
private fun Separador() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}