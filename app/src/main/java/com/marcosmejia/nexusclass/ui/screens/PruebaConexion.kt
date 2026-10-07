package com.marcosmejia.nexusclass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import com.marcosmejia.nexusclass.data.repository.TareaRepository

@Composable
fun PruebaConexion() {
    var texto by remember {
        mutableStateOf("Conectando con la API…\n(la primera vez puede tardar hasta 1 minuto)")
    }

    LaunchedEffect(Unit) {
        val clases = ClaseRepository().listar()
        val hoy = ClaseRepository().hoy()
        val tareas = TareaRepository().listar()
        texto = buildString {
            when (clases) {
                is Resultado.Exito -> {
                    appendLine("✅ Clases: ${clases.datos.size}")
                    clases.datos.forEach { appendLine("• ${it.nombreMateria} (${it.docente})") }
                }
                is Resultado.Fallo -> appendLine("❌ Clases: ${clases.mensaje}")
            }
            when (hoy) {
                is Resultado.Exito ->
                    appendLine("\n✅ Hoy: ${hoy.datos.diaSemana} · ${hoy.datos.estadoDia} · ${hoy.datos.clasesHoy.size} clases")
                is Resultado.Fallo -> appendLine("\n❌ Hoy: ${hoy.mensaje}")
            }
            when (tareas) {
                is Resultado.Exito -> appendLine("\n✅ Tareas: ${tareas.datos.size}")
                is Resultado.Fallo -> appendLine("\n❌ Tareas: ${tareas.mensaje}")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Prueba de conexión", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge)
    }
}