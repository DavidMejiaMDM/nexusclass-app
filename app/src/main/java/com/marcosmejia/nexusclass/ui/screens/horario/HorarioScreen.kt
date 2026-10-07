package com.marcosmejia.nexusclass.ui.screens.horario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.HorarioDto
import com.marcosmejia.nexusclass.ui.components.ChipTexto
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.components.TarjetaMensaje
import com.marcosmejia.nexusclass.ui.util.Dias
import com.marcosmejia.nexusclass.ui.util.Formato
import java.time.LocalTime
import java.time.ZoneId

private data class Sesion(val clase: ClaseDto, val h: HorarioDto)

private fun sesionesDe(clases: List<ClaseDto>, dia: String): List<Sesion> =
    clases
        .flatMap { c -> c.horarios.orEmpty().filter { it.diaSemana == dia }.map { Sesion(c, it) } }
        .sortedBy { it.h.horaInicio }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(
    onClase: (String) -> Unit,
    onNuevaClase: () -> Unit,
    onCargarHorario: () -> Unit,
    vm: HorarioViewModel = viewModel()
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val refrescando by vm.refrescando.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.silencioso() }

    when (val e = estado) {
        HorarioViewModel.Estado.Cargando ->
            PantallaCargando("Cargando tu horario…", "La primera vez puede tardar hasta 1 minuto")
        is HorarioViewModel.Estado.Error -> PantallaError(e.mensaje, onReintentar = vm::cargar)
        is HorarioViewModel.Estado.Exito -> PullToRefreshBox(
            isRefreshing = refrescando,
            onRefresh = vm::refrescar,
            modifier = Modifier.fillMaxSize()
        ) {
            Contenido(e.clases, onClase, onNuevaClase, onCargarHorario)
        }
    }
}

@Composable
private fun Contenido(
    clases: List<ClaseDto>,
    onClase: (String) -> Unit,
    onNuevaClase: () -> Unit,
    onCargarHorario: () -> Unit
) {
    var vistaSemana by remember { mutableStateOf(false) }
    var dia by remember { mutableStateOf(Dias.hoy()) }
    val hoy = Dias.hoy()
    val ahora = remember { LocalTime.now(ZoneId.of("America/Bogota")).toString().take(5) }
    val conClases = Dias.CODIGOS
        .filter { d -> clases.any { c -> c.horarios.orEmpty().any { it.diaSemana == d } } }
        .toSet()
    val periodo = clases.firstNotNullOfOrNull { it.periodo }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Horario", style = MaterialTheme.typography.headlineMedium)
                Text(
                    (if (periodo != null) "Periodo $periodo · " else "") + "${clases.size} asignaturas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (clases.isEmpty()) {
            item {
                TarjetaMensaje(
                    Icons.Outlined.UploadFile,
                    "Aún no tienes horario",
                    "Sube el PDF de Moodle o agrega tus clases manualmente.",
                    "Cargar horario", onCargarHorario
                )
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !vistaSemana, onClick = { vistaSemana = false }, label = { Text("Día") })
                    FilterChip(selected = vistaSemana, onClick = { vistaSemana = true }, label = { Text("Semana") })
                }
            }

            if (!vistaSemana) {
                item { SelectorDia(dia, hoy, conClases) { dia = it } }
                val sesiones = sesionesDe(clases, dia)
                if (sesiones.isEmpty()) {
                    item {
                        TarjetaMensaje(
                            Icons.Outlined.EventAvailable,
                            "Sin clases el ${Dias.nombre(dia).lowercase()}",
                            "Es un buen día para adelantar tus tareas."
                        )
                    }
                } else {
                    items(sesiones, key = { "${it.clase.id}-${it.h.horaInicio}" }) { s ->
                        val enCurso = dia == hoy && s.h.horaInicio <= ahora && ahora < s.h.horaFin
                        FilaSesion(s, enCurso, onClase)
                    }
                }
            } else {
                Dias.CODIGOS.forEach { d ->
                    val sesiones = sesionesDe(clases, d)
                    if (sesiones.isNotEmpty()) {
                        item(key = "dia-$d") {
                            Text(
                                Dias.nombre(d) + if (d == hoy) " · hoy" else "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(sesiones, key = { "$d-${it.clase.id}-${it.h.horaInicio}" }) { s ->
                            FilaSesion(s, false, onClase)
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = onNuevaClase, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar clase manualmente")
            }
        }
    }
}

@Composable
private fun SelectorDia(
    seleccionado: String,
    hoy: String,
    conClases: Set<String>,
    onDia: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Dias.CODIGOS.forEach { d ->
            val activo = d == seleccionado
            val forma = RoundedCornerShape(16.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(forma)
                    .background(
                        if (activo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .then(
                        if (d == hoy && !activo)
                            Modifier.border(1.dp, MaterialTheme.colorScheme.primary, forma)
                        else Modifier
                    )
                    .clickable { onDia(d) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    Dias.inicial(d),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                d !in conClases -> Color.Transparent
                                activo -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun FilaSesion(s: Sesion, enCurso: Boolean, onClase: (String) -> Unit) {
    val color = Formato.colorHex(s.clase.color, MaterialTheme.colorScheme.primary)
    Card(
        onClick = { onClase(s.clase.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (enCurso) color else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Formato.rango(s.h.horaInicio, s.h.horaFin),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (enCurso) ChipTexto("En curso", color)
                }
                Text(s.clase.nombreMateria, style = MaterialTheme.typography.titleMedium)
                Text(
                    Formato.ubicacion(s.clase.salon, s.clase.edificio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!s.clase.docente.isNullOrBlank()) {
                    Text(
                        s.clase.docente,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
        }
    }
}