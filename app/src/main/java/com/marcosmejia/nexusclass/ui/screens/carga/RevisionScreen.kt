package com.marcosmejia.nexusclass.ui.screens.carga

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.components.ChipTexto
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.components.TarjetaMensaje
import com.marcosmejia.nexusclass.ui.screens.horario.HorarioViewModel
import com.marcosmejia.nexusclass.ui.theme.Ambar
import com.marcosmejia.nexusclass.ui.util.Dias
import com.marcosmejia.nexusclass.ui.util.Formato

// Una hora como 10:01 o 9:07 casi seguro es un error de digitación del PDF
private fun raro(hora: String): Boolean = hora.takeLast(2).toIntOrNull()?.let { it % 5 != 0 } == true

@Composable
fun RevisionScreen(
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onNueva: () -> Unit,
    onConfirmar: () -> Unit,
    vm: HorarioViewModel = viewModel()
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.silencioso() }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Revisar clases", onVolver)

        when (val e = estado) {
            HorarioViewModel.Estado.Cargando -> PantallaCargando("Cargando tus clases…")
            is HorarioViewModel.Estado.Error -> PantallaError(e.mensaje, onReintentar = vm::cargar)
            is HorarioViewModel.Estado.Exito -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${e.clases.size} clases detectadas", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                "Verifica salones, docentes y horas. Toca una clase para corregirla.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (e.clases.isEmpty()) {
                        item {
                            TarjetaMensaje(
                                Icons.Outlined.UploadFile,
                                "No hay clases todavía",
                                "Sube el PDF de Moodle o agrega una clase manualmente."
                            )
                        }
                    }
                    items(e.clases, key = { it.id }) { c -> TarjetaRevision(c, onEditar) }
                    item {
                        OutlinedButton(onClick = onNueva, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar clase manualmente")
                        }
                    }
                }
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = onConfirmar,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                    ) { Text("Confirmar y continuar") }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRevision(c: ClaseDto, onEditar: (String) -> Unit) {
    val horarios = c.horarios.orEmpty()
    val dudosa = horarios.any { raro(it.horaInicio) || raro(it.horaFin) }
    val sinSalon = c.salon.isNullOrBlank()

    Card(
        onClick = { onEditar(c.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (dudosa) Ambar else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(c.nombreMateria, style = MaterialTheme.typography.titleMedium)
                Text(
                    c.docente?.takeIf { it.isNotBlank() } ?: "Docente por confirmar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    Formato.ubicacion(c.salon, c.edificio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                horarios.forEach {
                    Text(
                        "${Dias.corto(it.diaSemana)} · ${Formato.rango(it.horaInicio, it.horaFin)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (dudosa || sinSalon) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (dudosa) ChipTexto("Revisa las horas", Color(0xFFB45309))
                        if (sinSalon) ChipTexto("Sin salón", Color(0xFFB45309))
                    }
                }
            }
            Icon(Icons.Outlined.Edit, contentDescription = "Editar")
        }
    }
}