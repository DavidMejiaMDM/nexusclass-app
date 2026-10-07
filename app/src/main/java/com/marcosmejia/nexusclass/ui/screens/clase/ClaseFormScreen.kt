package com.marcosmejia.nexusclass.ui.screens.clase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.components.CampoForm
import com.marcosmejia.nexusclass.ui.components.DialogoConfirmar
import com.marcosmejia.nexusclass.ui.components.DialogoHora
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.util.Dias
import com.marcosmejia.nexusclass.ui.util.Formato
import java.time.LocalTime

@Composable
fun ClaseFormScreen(
    onVolver: () -> Unit,
    onEliminada: () -> Unit,
    vm: ClaseFormViewModel = viewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(ui.fin) {
        when (ui.fin) {
            FinClase.GUARDADA -> onVolver()
            FinClase.ELIMINADA -> onEliminada()
            null -> Unit
        }
    }

    var picker by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }   // (key del horario, ¿es inicio?)
    var confirmar by remember { mutableStateOf(false) }
    val errorCarga = ui.errorCarga

    Column(Modifier.fillMaxSize()) {
        BarraSuperior(if (vm.editando) "Editar asignatura" else "Nueva asignatura", onVolver)

        when {
            ui.cargando -> PantallaCargando("Cargando…")
            errorCarga != null -> PantallaError(errorCarga, onReintentar = vm::cargar)
            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CampoForm(ui.nombre, vm::setNombre, "Nombre de la materia *", ui.errores["nombre"])
                CampoForm(ui.docente, vm::setDocente, "Docente (opcional)")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { CampoForm(ui.salon, vm::setSalon, "Salón") }
                    Box(Modifier.weight(1f)) { CampoForm(ui.edificio, vm::setEdificio, "Sede / edificio") }
                }

                Text("Horarios *", style = MaterialTheme.typography.titleMedium)
                ui.horarios.forEach { h ->
                    key(h.key) {
                        FilaHorario(
                            h = h,
                            puedeQuitar = ui.horarios.size > 1,
                            onDia = { vm.setDia(h.key, it) },
                            onInicio = { picker = h.key to true },
                            onFin = { picker = h.key to false },
                            onQuitar = { vm.quitarHorario(h.key) }
                        )
                    }
                }
                ui.errores["horarios"]?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = RojoError)
                }
                OutlinedButton(onClick = vm::agregarHorario, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar otro horario")
                }

                ui.errorGeneral?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = RojoError)
                }

                Button(
                    onClick = vm::guardar,
                    enabled = !ui.guardando,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (ui.guardando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Text(if (vm.editando) "Guardar cambios" else "Guardar")
                }
                OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Cancelar")
                }
                if (vm.editando) {
                    TextButton(onClick = { confirmar = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Eliminar asignatura", color = RojoError)
                    }
                }
            }
        }
    }

    picker?.let { (id, esInicio) ->
        val h = ui.horarios.firstOrNull { it.key == id }
        DialogoHora(
            inicial = (if (esInicio) h?.inicio else h?.fin) ?: LocalTime.of(8, 0),
            onAceptar = { t ->
                if (esInicio) vm.setInicio(id, t) else vm.setFin(id, t)
                picker = null
            },
            onCancelar = { picker = null }
        )
    }

    if (confirmar) {
        DialogoConfirmar(
            titulo = "¿Eliminar esta asignatura?",
            texto = "También se eliminarán todas sus tareas. Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            onConfirmar = { confirmar = false; vm.eliminar() },
            onCancelar = { confirmar = false }
        )
    }
}

@Composable
private fun FilaHorario(
    h: HorarioEdit,
    puedeQuitar: Boolean,
    onDia: (String) -> Unit,
    onInicio: () -> Unit,
    onFin: () -> Unit,
    onQuitar: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    OutlinedButton(onClick = { menu = true }) {
                        Text(Dias.nombre(h.dia))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        Dias.CODIGOS.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(Dias.nombre(d)) },
                                onClick = { onDia(d); menu = false }
                            )
                        }
                    }
                }
                if (puedeQuitar) {
                    IconButton(onClick = onQuitar) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Quitar horario", tint = RojoError)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onInicio, modifier = Modifier.weight(1f)) {
                    Text(h.inicio?.let { Formato.hora12(it.toString()) } ?: "Inicio *")
                }
                OutlinedButton(onClick = onFin, modifier = Modifier.weight(1f)) {
                    Text(h.fin?.let { Formato.hora12(it.toString()) } ?: "Fin *")
                }
            }
        }
    }
}