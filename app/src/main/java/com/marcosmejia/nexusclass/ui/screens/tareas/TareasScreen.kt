package com.marcosmejia.nexusclass.ui.screens.tareas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.components.TarjetaMensaje
import com.marcosmejia.nexusclass.ui.components.TarjetaTarea
import com.marcosmejia.nexusclass.ui.theme.RojoError
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val ZONA: ZoneId = ZoneId.of("America/Bogota")
private val ORDEN = listOf("Vencidas", "Hoy", "Mañana", "Esta semana", "Más adelante", "Completadas")

private val OPCIONES_ESTADO = listOf(
    "PENDIENTES" to "Pendientes",
    "COMPLETADAS" to "Completadas",
    "TODAS" to "Todas"
)
private val OPCIONES_TIPO = listOf<Pair<String?, String>>(
    null to "Todos",
    "TAREA" to "Tareas",
    "PARCIAL" to "Parciales",
    "TALLER" to "Talleres"
)

// En qué grupo de la lista va cada tarea
private fun seccion(t: TareaDto): String {
    if (t.completada) return "Completadas"
    val fecha = runCatching { Instant.parse(t.fechaEntrega) }.getOrNull() ?: return "Más adelante"
    if (fecha.isBefore(Instant.now())) return "Vencidas"
    val dia = fecha.atZone(ZONA).toLocalDate()
    val hoy = LocalDate.now(ZONA)
    return when {
        dia == hoy -> "Hoy"
        dia == hoy.plusDays(1) -> "Mañana"
        !dia.isAfter(hoy.plusDays(7)) -> "Esta semana"
        else -> "Más adelante"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(
    onTarea: (String) -> Unit,
    onNueva: () -> Unit,
    vm: TareasViewModel = viewModel()
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val filtros by vm.filtros.collectAsStateWithLifecycle()
    val refrescando by vm.refrescando.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.silencioso() }

    Box(Modifier.fillMaxSize()) {
        when (val e = estado) {
            TareasViewModel.Estado.Cargando ->
                PantallaCargando("Cargando tareas…", "La primera vez puede tardar hasta 1 minuto")
            is TareasViewModel.Estado.Error -> PantallaError(e.mensaje, onReintentar = vm::cargar)
            is TareasViewModel.Estado.Exito -> {
                PullToRefreshBox(
                    isRefreshing = refrescando,
                    onRefresh = vm::refrescar,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ContenidoTareas(e.datos, filtros, vm, onTarea)
                }
                ExtendedFloatingActionButton(
                    onClick = onNueva,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Nueva tarea") }
                )
            }
        }
    }
}

@Composable
private fun ContenidoTareas(
    datos: TareasViewModel.Datos,
    f: TareasViewModel.Filtros,
    vm: TareasViewModel,
    onTarea: (String) -> Unit
) {
    val visibles = remember(datos, f) { vm.filtrar(datos.tareas, f) }
    val grupos = remember(visibles) {
        visibles.groupBy { seccion(it) }.toList().sortedBy { ORDEN.indexOf(it.first) }
    }
    val pendientes = datos.tareas.count { !it.completada }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Tareas", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "$pendientes pendientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { BarraFiltros(f, datos.clases, vm) }

        if (visibles.isEmpty()) {
            item {
                if (datos.tareas.isEmpty()) {
                    TarjetaMensaje(
                        Icons.Outlined.Checklist,
                        "Aún no tienes tareas",
                        "¡Crea la primera con el botón «Nueva tarea»!"
                    )
                } else {
                    TarjetaMensaje(
                        Icons.Outlined.Search,
                        "Sin resultados",
                        "No hay tareas con estos filtros."
                    )
                }
            }
        }

        grupos.forEach { (titulo, lista) ->
            item(key = "h-$titulo") {
                Text(
                    "$titulo (${lista.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (titulo == "Vencidas") RojoError else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(lista, key = { it.id }) { t ->
                TarjetaTarea(
                    t = t,
                    onClick = { onTarea(t.id) },
                    onToggle = { vm.alternar(t) }
                )
            }
        }
    }
}

@Composable
private fun BarraFiltros(
    f: TareasViewModel.Filtros,
    clases: List<ClaseDto>,
    vm: TareasViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = f.texto,
            onValueChange = vm::setTexto,
            singleLine = true,
            placeholder = { Text("Buscar tarea o materia") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = if (f.texto.isNotEmpty()) ({
                IconButton(onClick = { vm.setTexto("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Borrar búsqueda")
                }
            }) else null,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(OPCIONES_ESTADO) { (valor, nombre) ->
                FilterChip(
                    selected = f.estado == valor,
                    onClick = { vm.setEstado(valor) },
                    label = { Text(nombre) }
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(OPCIONES_TIPO) { (valor, nombre) ->
                FilterChip(
                    selected = f.tipo == valor,
                    onClick = { vm.setTipo(valor) },
                    label = { Text(nombre) }
                )
            }
        }

        var abierto by remember { mutableStateOf(false) }
        val nombreClase = clases.firstOrNull { it.id == f.claseId }?.nombreMateria ?: "Todas las asignaturas"
        Box {
            OutlinedButton(onClick = { abierto = true }) {
                Text(nombreClase, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                DropdownMenuItem(
                    text = { Text("Todas las asignaturas") },
                    onClick = { vm.setClase(null); abierto = false }
                )
                clases.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.nombreMateria) },
                        onClick = { vm.setClase(c.id); abierto = false }
                    )
                }
            }
        }
    }
}