package com.marcosmejia.nexusclass.ui.screens.ajustes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.data.local.Preferencias
import com.marcosmejia.nexusclass.ui.components.DialogoConfirmar
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.util.Avisos

@Composable
fun AjustesScreen(
    onCargarHorario: () -> Unit,
    onNuevaClase: () -> Unit,
    vm: AjustesViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val nombre by Preferencias.nombre.collectAsStateWithLifecycle()
    val tema by Preferencias.tema.collectAsStateWithLifecycle()
    val notif by Preferencias.notificaciones.collectAsStateWithLifecycle()
    val horas by Preferencias.anticipacionHoras.collectAsStateWithLifecycle()
    val eliminando by vm.eliminando.collectAsStateWithLifecycle()

    var dialogoNombre by remember { mutableStateOf(false) }
    var dialogoAcerca by remember { mutableStateOf(false) }
    var dialogoBorrar by remember { mutableStateOf(false) }

    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!ok) Avisos.mostrar("Sin el permiso no podremos mostrarte las alertas")
    }

    fun alternarNotificaciones(activar: Boolean) {
        Preferencias.setNotificaciones(activar)
        if (activar && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val iniciales = nombre.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "UA" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)

        // ----- Perfil -----
        Panel {
            Row(
                Modifier.fillMaxWidth().clickable { dialogoNombre = true }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text(nombre.ifBlank { "Estudiante" }, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Uniautónoma del Cauca · toca para cambiar tu nombre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Outlined.Edit, contentDescription = null)
            }
        }

        // ----- Horario -----
        Seccion("Horario")
        Panel {
            FilaAccion(Icons.Outlined.UploadFile, "Actualizar horario", "Sube un nuevo PDF de Moodle", onClick = onCargarHorario)
            HorizontalDivider()
            FilaAccion(Icons.Outlined.Add, "Agregar clase manualmente", null, onClick = onNuevaClase)
        }

        // ----- Notificaciones -----
        Seccion("Notificaciones")
        Panel {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Alertas de entregas", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Te avisamos antes de que venza una tarea o parcial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = notif, onCheckedChange = ::alternarNotificaciones)
            }
            HorizontalDivider()
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Avisarme con anticipación de", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 12, 24, 48).forEach { h ->
                        FilterChip(
                            selected = horas == h,
                            enabled = notif,
                            onClick = { Preferencias.setAnticipacion(h) },
                            label = { Text("$h h") }
                        )
                    }
                }
            }
            // PASO 12: aquí va el botón «Revisar entregas ahora»
        }

        // ----- Apariencia -----
        Seccion("Apariencia")
        Panel {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tema", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SISTEMA" to "Sistema", "CLARO" to "Claro", "OSCURO" to "Oscuro").forEach { (valor, nombreTema) ->
                        FilterChip(
                            selected = tema == valor,
                            onClick = { Preferencias.setTema(valor) },
                            label = { Text(nombreTema) }
                        )
                    }
                }
            }
        }

        // ----- Otros -----
        Panel {
            FilaAccion(Icons.Outlined.Info, "Acerca de la app", null, onClick = { dialogoAcerca = true })
            HorizontalDivider()
            FilaAccion(
                Icons.Outlined.DeleteForever, "Eliminar todos mis datos",
                if (eliminando) "Eliminando…" else "Borra tus clases y tareas",
                color = RojoError,
                onClick = { if (!eliminando) dialogoBorrar = true }
            )
        }
    }

    if (dialogoNombre) {
        var texto by remember { mutableStateOf(nombre) }
        AlertDialog(
            onDismissRequest = { dialogoNombre = false },
            title = { Text("Tu nombre") },
            text = {
                OutlinedTextField(value = texto, onValueChange = { texto = it }, singleLine = true, label = { Text("Nombre") })
            },
            confirmButton = {
                TextButton(onClick = { Preferencias.setNombre(texto.trim()); dialogoNombre = false }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { dialogoNombre = false }) { Text("Cancelar") } }
        )
    }

    if (dialogoAcerca) {
        AlertDialog(
            onDismissRequest = { dialogoAcerca = false },
            title = { Text("Organizador UA") },
            text = {
                Text(
                    "Organizador Inteligente de Clases y Tareas\nVersión 1.0\n\n" +
                            "Desarrollado por Marcos David Mejía Escobar\nDocente: Zulema León\n" +
                            "Corporación Universitaria Autónoma del Cauca"
                )
            },
            confirmButton = { TextButton(onClick = { dialogoAcerca = false }) { Text("Cerrar") } }
        )
    }

    if (dialogoBorrar) {
        DialogoConfirmar(
            titulo = "¿Eliminar todos tus datos?",
            texto = "Se borrarán todas tus asignaturas y tareas. Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar todo",
            onConfirmar = { dialogoBorrar = false; vm.eliminarTodo() },
            onCancelar = { dialogoBorrar = false }
        )
    }
}

@Composable
private fun Seccion(texto: String) {
    Text(texto.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun Panel(contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = contenido
    )
}

@Composable
private fun FilaAccion(
    icono: ImageVector,
    titulo: String,
    subtitulo: String?,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icono, contentDescription = null, tint = color)
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = color)
            if (subtitulo != null) {
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}