package com.marcosmejia.nexusclass.ui.screens.notificaciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.local.Preferencias
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.components.PantallaCargando
import com.marcosmejia.nexusclass.ui.components.PantallaError
import com.marcosmejia.nexusclass.ui.components.TarjetaMensaje
import com.marcosmejia.nexusclass.ui.components.TarjetaTarea
import com.marcosmejia.nexusclass.ui.screens.tareas.TareasViewModel
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    onVolver: () -> Unit,
    onTarea: (String) -> Unit,
    vm: TareasViewModel = viewModel()
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val refrescando by vm.refrescando.collectAsStateWithLifecycle()
    val horas by Preferencias.anticipacionHoras.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.silencioso() }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Notificaciones", onVolver)

        when (val e = estado) {
            TareasViewModel.Estado.Cargando -> PantallaCargando("Buscando alertas…")
            is TareasViewModel.Estado.Error -> PantallaError(e.mensaje, onReintentar = vm::cargar)
            is TareasViewModel.Estado.Exito -> {
                val pendientes = e.datos.tareas.filter { !it.completada }
                val vencidas = pendientes
                    .filter { (Formato.minutosHasta(it.fechaEntrega) ?: 1L) < 0 }
                    .sortedBy { it.fechaEntrega }
                val proximas = pendientes
                    .filter {
                        val m = Formato.minutosHasta(it.fechaEntrega)
                        m != null && m >= 0 && m <= horas * 60L
                    }
                    .sortedBy { it.fechaEntrega }

                PullToRefreshBox(
                    isRefreshing = refrescando,
                    onRefresh = vm::refrescar,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Column {
                                Text("Alertas", style = MaterialTheme.typography.headlineMedium)
                                Text(
                                    "Entregas que vencen en las próximas $horas horas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (vencidas.isEmpty() && proximas.isEmpty()) {
                            item {
                                TarjetaMensaje(
                                    Icons.Outlined.NotificationsNone,
                                    "Sin alertas",
                                    "No tienes entregas por vencer en las próximas $horas horas."
                                )
                            }
                        }

                        if (vencidas.isNotEmpty()) {
                            item {
                                Text("Vencidas (${vencidas.size})", style = MaterialTheme.typography.titleMedium, color = RojoError)
                            }
                            items(vencidas, key = { "v-${it.id}" }) { t ->
                                TarjetaTarea(t, onClick = { onTarea(t.id) }, onToggle = { vm.alternar(t) })
                            }
                        }
                        if (proximas.isNotEmpty()) {
                            item {
                                Text("Por vencer (${proximas.size})", style = MaterialTheme.typography.titleMedium)
                            }
                            items(proximas, key = { "p-${it.id}" }) { t ->
                                TarjetaTarea(t, onClick = { onTarea(t.id) }, onToggle = { vm.alternar(t) })
                            }
                        }
                    }
                }
            }
        }
    }
}