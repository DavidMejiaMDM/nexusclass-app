package com.marcosmejia.nexusclass.ui.screens.hoy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.remote.SesionDto
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.data.remote.TodayResponse
import com.marcosmejia.nexusclass.ui.components.ChipTipo
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.components.TarjetaMensaje
import com.marcosmejia.nexusclass.ui.theme.Ambar
import com.marcosmejia.nexusclass.ui.theme.AmbarClaro
import com.marcosmejia.nexusclass.ui.theme.AzulUA
import com.marcosmejia.nexusclass.ui.theme.AzulUAOscuro
import com.marcosmejia.nexusclass.ui.theme.TextoPrincipal
import com.marcosmejia.nexusclass.ui.theme.TextoSecundario
import com.marcosmejia.nexusclass.ui.theme.VerdeExito
import com.marcosmejia.nexusclass.ui.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoyScreen(
    onClase: (String) -> Unit,
    onTarea: (String) -> Unit,
    onNuevaTarea: () -> Unit,
    onHorario: () -> Unit,
    onNotificaciones: () -> Unit,
    onCargarHorario: () -> Unit,
    vm: HoyViewModel = viewModel()
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val refrescando by vm.refrescando.collectAsStateWithLifecycle()

    // Al volver de otra pantalla (ej. después de editar una tarea) se actualiza solo
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.silencioso() }

    when (val e = estado) {
        HoyViewModel.Estado.Cargando -> PantallaCargando(
            "Conectando con el servidor…",
            "La primera vez puede tardar hasta 1 minuto"
        )
        is HoyViewModel.Estado.Error -> PantallaError(e.mensaje, onReintentar = vm::cargar)
        is HoyViewModel.Estado.Exito -> PullToRefreshBox(
            isRefreshing = refrescando,
            onRefresh = vm::refrescar,
            modifier = Modifier.fillMaxSize()
        ) {
            ContenidoHoy(
                d = e.datos,
                onClase = onClase,
                onTarea = onTarea,
                onNuevaTarea = onNuevaTarea,
                onHorario = onHorario,
                onNotificaciones = onNotificaciones,
                onCargarHorario = onCargarHorario,
                onCompletar = vm::completar
            )
        }
    }
}

@Composable
private fun ContenidoHoy(
    d: HoyViewModel.Datos,
    onClase: (String) -> Unit,
    onTarea: (String) -> Unit,
    onNuevaTarea: () -> Unit,
    onHorario: () -> Unit,
    onNotificaciones: () -> Unit,
    onCargarHorario: () -> Unit,
    onCompletar: (String) -> Unit
) {
    val hoy = d.hoy
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Encabezado(Formato.fechaLarga(hoy.ahora), d.urgentes.size, onNotificaciones) }

        item { BloqueClases(hoy, onClase, onCargarHorario) }

        item { TituloSeccion("Entregas en las próximas 24 horas", Icons.Outlined.Alarm) }
        if (d.urgentes.isEmpty()) {
            item {
                TarjetaMensaje(
                    Icons.Outlined.CheckCircle,
                    "Todo al día",
                    "No tienes entregas en las próximas 24 horas."
                )
            }
        } else {
            items(d.urgentes, key = { it.id }) { t -> TarjetaUrgente(t, onTarea, onCompletar) }
        }

        if (hoy.clasesHoy.isNotEmpty()) {
            item { TituloSeccion("Clases de hoy", Icons.Outlined.CalendarMonth) }
            items(hoy.clasesHoy, key = { "${it.claseId}-${it.horaInicio}" }) { s ->
                FilaClaseHoy(s, onClase)
            }
        }

        item { AccesosRapidos(onNuevaTarea, onHorario) }
    }
}

@Composable
private fun Encabezado(fecha: String, avisos: Int, onNotificaciones: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "UNIAUTÓNOMA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Hoy", style = MaterialTheme.typography.headlineMedium)
            Text(
                fecha,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onNotificaciones) {
            BadgedBox(badge = { if (avisos > 0) Badge { Text("$avisos") } }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
            }
        }
    }
}

@Composable
private fun BloqueClases(
    hoy: TodayResponse,
    onClase: (String) -> Unit,
    onCargarHorario: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (hoy.estadoDia) {
            "SIN_HORARIO" -> TarjetaMensaje(
                Icons.Outlined.UploadFile,
                "Aún no tienes horario",
                "Sube el PDF de Moodle para ver aquí tus clases.",
                "Cargar horario", onCargarHorario
            )
            "SIN_CLASES" -> TarjetaMensaje(
                Icons.Outlined.EventAvailable,
                "Hoy no tienes clases",
                "Aprovecha para adelantar tus tareas."
            )
            "TERMINADO" -> TarjetaMensaje(
                Icons.Outlined.EmojiEvents,
                "¡Terminaste tus clases de hoy!",
                "Buen trabajo. Revisa tus entregas pendientes."
            )
            "ENTRE_CLASES" -> TarjetaMensaje(
                Icons.Outlined.Schedule,
                "No tienes clase en este momento",
                "Tu próxima clase está justo debajo."
            )
        }
        hoy.claseActual?.let { TarjetaClaseActual(it, onClase) }
        hoy.proximaClase?.let { TarjetaProximaClase(it, onClase) }
    }
}

@Composable
private fun TarjetaClaseActual(c: SesionDto, onClase: (String) -> Unit) {
    val fondo = Brush.linearGradient(listOf(AzulUA, AzulUAOscuro))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(fondo)
            .clickable { onClase(c.claseId) }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "EN CURSO AHORA · TERMINA EN ${Formato.duracion(c.minutosRestantes ?: 0).uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
        Text(c.nombreMateria, style = MaterialTheme.typography.titleLarge, color = Color.White)
        FilaInfo(Icons.Outlined.LocationOn, Formato.ubicacion(c.salon, c.edificio), Color.White)
        FilaInfo(Icons.Outlined.Schedule, Formato.rango(c.horaInicio, c.horaFin), Color.White)
        if (!c.docente.isNullOrBlank()) FilaInfo(Icons.Outlined.Person, c.docente, Color.White)
        LinearProgressIndicator(
            progress = { (c.progreso ?: 0) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
        Button(
            onClick = { onClase(c.claseId) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AzulUA)
        ) {
            Text("Ver tareas (${c.tareasPendientes ?: 0})")
        }
    }
}

@Composable
private fun TarjetaProximaClase(c: SesionDto, onClase: (String) -> Unit) {
    val color = Formato.colorHex(c.color, MaterialTheme.colorScheme.primary)
    Card(
        onClick = { onClase(c.claseId) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "PRÓXIMA CLASE · ${Formato.hora12(c.horaInicio)} " +
                            "(EN ${Formato.duracion(c.minutosParaInicio ?: 0).uppercase()})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(c.nombreMateria, style = MaterialTheme.typography.titleMedium)
                Text(
                    Formato.ubicacion(c.salon, c.edificio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun TarjetaUrgente(
    t: TareaDto,
    onTarea: (String) -> Unit,
    onCompletar: (String) -> Unit
) {
    Card(
        onClick = { onTarea(t.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AmbarClaro),
        border = BorderStroke(1.dp, Ambar)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChipTipo(t.tipo)
                    Text(
                        Formato.restante(t.fechaEntrega) ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309)
                    )
                }
                Text(t.titulo, style = MaterialTheme.typography.titleMedium, color = TextoPrincipal)
                Text(
                    "${t.clase?.nombreMateria ?: ""} · ${Formato.fechaTarea(t.fechaEntrega)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
            IconButton(onClick = { onCompletar(t.id) }) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Marcar como completada",
                    tint = VerdeExito
                )
            }
        }
    }
}

@Composable
private fun FilaClaseHoy(s: SesionDto, onClase: (String) -> Unit) {
    val color = Formato.colorHex(s.color, MaterialTheme.colorScheme.primary)
    val pasada = s.estado == "PASADA"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClase(s.claseId) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (pasada) MaterialTheme.colorScheme.outline else color)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                s.nombreMateria,
                style = MaterialTheme.typography.titleMedium,
                color = if (pasada) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (pasada) TextDecoration.LineThrough else null
            )
            Text(
                "${Formato.rango(s.horaInicio, s.horaFin)} · ${Formato.ubicacion(s.salon, s.edificio)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (s.estado == "EN_CURSO") {
            Text("AHORA", style = MaterialTheme.typography.labelSmall, color = AzulUA)
        }
    }
}

@Composable
private fun AccesosRapidos(onNuevaTarea: () -> Unit, onHorario: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AccesoRapido(Icons.Outlined.Add, "Nueva tarea", Modifier.weight(1f), onNuevaTarea)
        AccesoRapido(Icons.Outlined.CalendarMonth, "Horario semanal", Modifier.weight(1f), onHorario)
    }
}

@Composable
private fun AccesoRapido(icono: ImageVector, texto: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(texto, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TituloSeccion(texto: String, icono: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(texto, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icono, contentDescription = null, tint = color.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}