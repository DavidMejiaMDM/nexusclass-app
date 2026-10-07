package com.marcosmejia.nexusclass.ui.screens.carga

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcosmejia.nexusclass.ui.components.BarraSuperior
import com.marcosmejia.nexusclass.ui.screens.carga.CargaPdfViewModel.Fase
import com.marcosmejia.nexusclass.ui.theme.RojoClaro
import com.marcosmejia.nexusclass.ui.theme.RojoError
import com.marcosmejia.nexusclass.ui.theme.TextoPrincipal
import com.marcosmejia.nexusclass.ui.theme.VerdeClaro
import com.marcosmejia.nexusclass.ui.theme.VerdeExito

private fun Modifier.bordeDiscontinuo(color: Color): Modifier = this.drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(20.dp.toPx()),
        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f)))
    )
}

@Composable
fun CargaPdfScreen(
    onVolver: () -> Unit,
    onOmitir: () -> Unit,
    onContinuar: () -> Unit,
    vm: CargaPdfViewModel = viewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val fase = ui.fase

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.elegir(uri)
    }
    val abrir = { selector.launch(arrayOf("application/pdf")) }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Cargar horario", onVolver) {
            TextButton(onClick = onOmitir) { Text("Omitir") }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Sube tu horario", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Descarga el PDF de tu horario académico desde Moodle y la app extraerá tus asignaturas, días, horas y salones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (fase !is Fase.Procesando && fase !is Fase.Exito) ZonaArchivo(fase, abrir)

            when (fase) {
                Fase.Vacio -> Button(onClick = { abrir() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Seleccionar PDF de Moodle")
                }
                is Fase.Seleccionado -> {
                    Button(onClick = vm::subir, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Subir y procesar")
                    }
                    OutlinedButton(onClick = { abrir() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Cambiar archivo")
                    }
                }
                Fase.Procesando -> Tarjeta(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outlineVariant) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Extrayendo asignaturas, días, horas y salones…", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "La primera vez puede tardar hasta 1 minuto mientras el servidor despierta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is Fase.Exito -> {
                    Tarjeta(VerdeClaro, VerdeExito) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = VerdeExito)
                        Text("¡Listo! Se encontraron ${fase.total} clases", style = MaterialTheme.typography.titleMedium, color = TextoPrincipal)
                        Text(
                            "${fase.creadas} nuevas · ${fase.omitidas} ya estaban en tu horario",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoPrincipal
                        )
                    }
                    Button(onClick = onContinuar, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Revisar clases")
                    }
                }
                is Fase.Error -> {
                    Tarjeta(RojoClaro, RojoError) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = RojoError)
                        Text(fase.mensaje, style = MaterialTheme.typography.bodyMedium, color = TextoPrincipal)
                    }
                    Button(
                        onClick = { if (fase.hayArchivo) vm.subir() else abrir() },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) { Text("Reintentar") }
                    OutlinedButton(onClick = { abrir() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Elegir otro archivo")
                    }
                }
            }

            if (fase !is Fase.Procesando && fase !is Fase.Exito) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Reemplazar mi horario actual", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Borra las clases y tareas actuales. Úsalo al empezar un semestre nuevo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = ui.reemplazar, onCheckedChange = vm::setReemplazar)
                }
            }

            GuiaRapida()
        }
    }
}

@Composable
private fun ZonaArchivo(fase: Fase, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    val (icono, titulo, sub) = when (fase) {
        is Fase.Seleccionado -> Triple(Icons.Outlined.PictureAsPdf, fase.nombre, "${fase.kb} KB · Listo para subir")
        else -> Triple(Icons.Outlined.UploadFile, "Toca aquí para seleccionar tu horario", "Archivo PDF descargado de Moodle (máx. 5 MB)")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            .bordeDiscontinuo(color)
            .clickable(onClick = onClick)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(40.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            sub,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Tarjeta(fondo: Color, borde: Color, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(1.dp, borde)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = contenido)
    }
}

@Composable
private fun GuiaRapida() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Guía rápida", style = MaterialTheme.typography.titleMedium)
            listOf(
                "Descarga tu horario académico en PDF desde Moodle.",
                "Selecciónalo aquí y pulsa «Subir y procesar».",
                "Revisa las clases detectadas y corrige lo que haga falta."
            ).forEachIndexed { i, texto ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Text("${i + 1}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(texto, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}