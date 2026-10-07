package com.marcosmejia.nexusclass.ui.screens.tareas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.components.DialogoEliminar
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.util.Formato
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTareaScreen(
    onVolver: () -> Unit,
    onEliminada: () -> Unit,
    vm: FormTareaViewModel = viewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(ui.fin) {
        when (ui.fin) {
            Fin.GUARDADA -> onVolver()
            Fin.ELIMINADA -> onEliminada()
            null -> Unit
        }
    }

    var dialogoFecha by remember { mutableStateOf(false) }
    var dialogoHora by remember { mutableStateOf(false) }
    var menuClase by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }

    val errorCarga = ui.errorCarga

    Column(Modifier.fillMaxSize()) {
        BarraSuperior(if (vm.editando) "Editar tarea" else "Nueva tarea", onVolver)

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
                Campo(
                    valor = ui.titulo,
                    onCambio = vm::setTitulo,
                    etiqueta = "Título *",
                    error = ui.errores["titulo"]
                )

                // Asignatura
                Box {
                    CampoSelector(
                        valor = ui.clases.firstOrNull { it.id == ui.claseId }?.nombreMateria ?: "",
                        etiqueta = "Asignatura *",
                        hayError = ui.errores.containsKey("claseId"),
                        mensaje = ui.errores["claseId"],
                        icono = Icons.Filled.ArrowDropDown,
                        onClick = { menuClase = true }
                    )
                    DropdownMenu(expanded = menuClase, onDismissRequest = { menuClase = false }) {
                        ui.clases.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.nombreMateria) },
                                onClick = { vm.setClase(c.id); menuClase = false }
                            )
                        }
                    }
                }

                // Tipo
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tipo", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TAREA" to "Tarea", "PARCIAL" to "Parcial", "TALLER" to "Taller")
                            .forEach { (valor, nombre) ->
                                FilterChip(
                                    selected = ui.tipo == valor,
                                    onClick = { vm.setTipo(valor) },
                                    label = { Text(nombre) }
                                )
                            }
                    }
                }

                // Fecha y hora de entrega
                val errorFecha = ui.errores["fechaEntrega"]
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CampoSelector(
                            valor = ui.fecha?.let { Formato.fechaCorta(it) } ?: "",
                            etiqueta = "Fecha de entrega *",
                            hayError = errorFecha != null,
                            mensaje = null,
                            icono = Icons.Outlined.CalendarMonth,
                            onClick = { dialogoFecha = true },
                            modifier = Modifier.weight(1.4f)
                        )
                        CampoSelector(
                            valor = ui.hora?.let { Formato.hora12(it.toString()) } ?: "",
                            etiqueta = "Hora *",
                            hayError = errorFecha != null,
                            mensaje = null,
                            icono = Icons.Outlined.Schedule,
                            onClick = { dialogoHora = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (errorFecha != null) {
                        Text(errorFecha, style = MaterialTheme.typography.bodySmall, color = RojoError)
                    }
                }

                Campo(
                    valor = ui.porcentaje,
                    onCambio = vm::setPorcentaje,
                    etiqueta = "Valor en la nota, % (opcional)",
                    error = ui.errores["porcentaje"],
                    teclado = KeyboardType.Number
                )
                Campo(valor = ui.modalidad, onCambio = vm::setModalidad, etiqueta = "Modalidad (opcional)")
                Campo(
                    valor = ui.notas,
                    onCambio = vm::setNotas,
                    etiqueta = "Notas (opcional)",
                    unaLinea = false
                )

                if (vm.editando) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Marcar como completada", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = ui.completada, onCheckedChange = vm::setCompletada)
                    }
                }

                ui.errorGeneral?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = RojoError)
                }

                Button(
                    onClick = vm::guardar,
                    enabled = !ui.guardando,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (ui.guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (vm.editando) "Guardar cambios" else "Guardar")
                    }
                }
                OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Cancelar")
                }
                if (vm.editando) {
                    TextButton(onClick = { confirmarEliminar = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Eliminar tarea", color = RojoError)
                    }
                }
            }
        }
    }

    if (dialogoFecha) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = ui.fecha?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { dialogoFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { ms ->
                        vm.setFecha(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    dialogoFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { dialogoFecha = false }) { Text("Cancelar") } }
        ) { DatePicker(state = estadoFecha) }
    }

    if (dialogoHora) {
        val inicial = ui.hora ?: LocalTime.of(8, 0)
        val estadoHora = rememberTimePickerState(
            initialHour = inicial.hour,
            initialMinute = inicial.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { dialogoHora = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.setHora(LocalTime.of(estadoHora.hour, estadoHora.minute))
                    dialogoHora = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { dialogoHora = false }) { Text("Cancelar") } },
            text = { TimePicker(state = estadoHora) }
        )
    }

    if (confirmarEliminar) {
        DialogoEliminar(
            onConfirmar = { confirmarEliminar = false; vm.eliminar() },
            onCancelar = { confirmarEliminar = false }
        )
    }
}

@Composable
private fun Campo(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String? = null,
    unaLinea: Boolean = true,
    teclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = if (error != null) ({ Text(error) }) else null,
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}

// Campo de solo lectura que abre un selector (lista, fecha u hora) al tocarlo
@Composable
private fun CampoSelector(
    valor: String,
    etiqueta: String,
    hayError: Boolean,
    mensaje: String?,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { Icon(icono, contentDescription = null) },
            isError = hayError,
            supportingText = if (mensaje != null) ({ Text(mensaje) }) else null,
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
    }
}