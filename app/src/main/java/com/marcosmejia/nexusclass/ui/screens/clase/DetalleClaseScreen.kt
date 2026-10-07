package com.marcosmejia.nexusclass.ui.screens.clase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.components.TarjetaMensaje
import com.marcosmejia.nexusclass.ui.components.TarjetaTarea
import com.marcosmejia.nexusclass.ui.theme.AzulUA
import com.marcosmejia.nexusclass.ui.theme.AzulUAOscuro
import com.marcosmejia.nexusclass.ui.util.Dias
import com.marcosmejia.nexusclass.ui.util.Formato

@Composable
fun DetalleClaseScreen(
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onNuevaTarea: (String) -> Unit,
    onTarea: (String) -> Unit,
    vm: DetalleClaseViewModel = viewModel()
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.silencioso() }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Asignatura", onVolver) {
            IconButton(onClick = { onEditar(vm.id) }) {
                Icon(Icons.Outlined.Edit, contentDescription = "Editar asignatura")
            }
        }
        when (val e = estado) {
            DetalleClaseViewModel.Estado.Cargando -> PantallaCargando("Cargando asignatura…")
            is DetalleClaseViewModel.Estado.Error -> PantallaError(e.mensaje, onReintentar = vm::cargar)
            is DetalleClaseViewModel.Estado.Exito ->
                Contenido(e.clase, onNuevaTarea, onTarea, vm::alternar)
        }
    }
}

@Composable
private fun Contenido(
    c: ClaseDto,
    onNuevaTarea: (String) -> Unit,
    onTarea: (String) -> Unit,
    onAlternar: (TareaDto) -> Unit
) {
    val tareas = c.tareas.orEmpty().sortedWith(compareBy({ it.completada }, { it.fechaEntrega }))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Encabezado(c) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Dato("Pendientes", "${c.tareasPendientes ?: 0}", Modifier.weight(1f))
                Dato("Total de tareas", "${c.totalTareas ?: tareas.size}", Modifier.weight(1f))
            }
        }

        item { Text("Tareas y parciales", style = MaterialTheme.typography.titleMedium) }

        if (tareas.isEmpty()) {
            item {
                TarjetaMensaje(
                    Icons.Outlined.Checklist,
                    "Sin tareas todavía",
                    "Agrega la primera tarea o parcial de esta asignatura."
                )
            }
        } else {
            items(tareas, key = { it.id }) { t ->
                TarjetaTarea(
                    t = t,
                    onClick = { onTarea(t.id) },
                    onToggle = { onAlternar(t) },
                    mostrarClase = false
                )
            }
        }

        item {
            Button(onClick = { onNuevaTarea(c.id) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar tarea")
            }
        }
    }
}

@Composable
private fun Encabezado(c: ClaseDto) {
    val extra = listOfNotNull(
        c.grupo?.takeIf { it.isNotBlank() }?.let { "Grupo $it" },
        c.jornada?.takeIf { it.isNotBlank() }?.lowercase()?.replaceFirstChar { it.uppercase() },
        c.periodo?.takeIf { it.isNotBlank() }?.let { "Periodo $it" }
    ).joinToString(" · ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(AzulUA, AzulUAOscuro)))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(c.nombreMateria, style = MaterialTheme.typography.titleLarge, color = Color.White)
        FilaInfo(Icons.Outlined.Person, c.docente?.takeIf { it.isNotBlank() } ?: "Docente por confirmar")
        FilaInfo(Icons.Outlined.LocationOn, Formato.ubicacion(c.salon, c.edificio))
        c.horarios.orEmpty().forEach { h ->
            FilaInfo(Icons.Outlined.Schedule, "${Dias.nombre(h.diaSemana)} · ${Formato.rango(h.horaInicio, h.horaFin)}")
        }
        if (extra.isNotEmpty()) {
            Text(extra, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icono, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(valor, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}