package com.marcosmejia.nexusclass.ui.screens.ajustes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.marcosmejia.nexusclass.data.local.Sesion
import com.marcosmejia.nexusclass.notifications.Alertas
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
    val usuario by Sesion.usuario.collectAsStateWithLifecycle()
    val tema by Preferencias.tema.collectAsStateWithLifecycle()
    val notif by Preferencias.notificaciones.collectAsStateWithLifecycle()
    val horas by Preferencias.anticipacionHoras.collectAsStateWithLifecycle()
    val eliminando by vm.eliminando.collectAsStateWithLifecycle()

    var dialogoAcerca by remember { mutableStateOf(false) }
    var dialogoSalir by remember { mutableStateOf(false) }
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

    val nombre = usuario?.nombre.orEmpty()
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

        // ----- Perfil (datos de la cuenta) -----
        Panel {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
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
                        usuario?.email.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
            HorizontalDivider()
            FilaAccion(
                Icons.Outlined.NotificationsActive,
                "Revisar entregas ahora",
                "Envía las alertas pendientes en este momento",
                onClick = { Alertas.revisarAhora(contexto) }
            )
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

        // ----- Cuenta -----
        Seccion("Cuenta")
        Panel {
            FilaAccion(Icons.Outlined.Info, "Acerca de la app", null, onClick = { dialogoAcerca = true })
            HorizontalDivider()
            FilaAccion(Icons.AutoMirrored.Outlined.Logout, "Cerrar sesión", null, onClick = { dialogoSalir = true })
            HorizontalDivider()
            FilaAccion(
                Icons.Outlined.DeleteForever, "Eliminar mi cuenta",
                if (eliminando) "Eliminando…" else "Borra tu cuenta, tus clases y tus tareas",
                color = RojoError,
                onClick = { if (!eliminando) dialogoBorrar = true }
            )
        }
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

    if (dialogoSalir) {
        AlertDialog(
            onDismissRequest = { dialogoSalir = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus datos siguen guardados. Podrás volver a entrar con tu correo y contraseña.") },
            confirmButton = { TextButton(onClick = { dialogoSalir = false; vm.cerrarSesion() }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { dialogoSalir = false }) { Text("Cancelar") } }
        )
    }

    if (dialogoBorrar) {
        DialogoConfirmar(
            titulo = "¿Eliminar tu cuenta?",
            texto = "Se borrarán tu cuenta, tus asignaturas y tus tareas. Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar cuenta",
            onConfirmar = { dialogoBorrar = false; vm.eliminarCuenta() },
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