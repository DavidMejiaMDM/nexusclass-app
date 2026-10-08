package com.marcosmejia.nexusclass.ui.screens.inicio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

private data class Pagina(val icono: ImageVector, val titulo: String, val texto: String)

private val PAGINAS = listOf(
    Pagina(
        Icons.Outlined.UploadFile,
        "Sube tu horario de Moodle",
        "Olvídate de revisar PDFs confusos. Sube tu horario oficial y la app organizará tus clases, salones y horarios al instante."
    ),
    Pagina(
        Icons.Outlined.Schedule,
        "Mira tu clase actual y la próxima",
        "Sabe en todo momento dónde debes estar: la clase en curso, cuánto falta para que termine y cuál sigue."
    ),
    Pagina(
        Icons.Outlined.NotificationsActive,
        "Nunca olvides una entrega",
        "Registra tareas, talleres y parciales por asignatura y recibe alertas antes de que venzan."
    )
)

@Composable
fun OnboardingScreen(onTerminar: () -> Unit) {
    val contexto = LocalContext.current
    val estado = rememberPagerState(pageCount = { PAGINAS.size })
    val scope = rememberCoroutineScope()
    val ultima = estado.currentPage == PAGINAS.size - 1

    // Al terminar pedimos el permiso de notificaciones (Android 13+) y seguimos pase lo que pase
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onTerminar() }
    fun comenzar() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onTerminar()
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onTerminar) { Text("Omitir") }
        }

        HorizontalPager(state = estado, modifier = Modifier.weight(1f)) { i ->
            val p = PAGINAS[i]
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier.size(160.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(p.icono, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(32.dp))
                Text(p.titulo, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    p.texto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
            repeat(PAGINAS.size) { i ->
                val activa = i == estado.currentPage
                Box(
                    Modifier
                        .padding(4.dp)
                        .height(8.dp)
                        .width(if (activa) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Button(
            onClick = {
                if (ultima) comenzar() else scope.launch { estado.animateScrollToPage(estado.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text(if (ultima) "Comenzar" else "Siguiente") }
    }
}