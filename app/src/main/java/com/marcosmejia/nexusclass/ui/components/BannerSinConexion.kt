package com.marcosmejia.nexusclass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcosmejia.nexusclass.data.local.CacheLocal
import com.marcosmejia.nexusclass.ui.theme.AmbarClaro
import com.marcosmejia.nexusclass.ui.theme.TextoPrincipal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Franja amarilla que aparece (solo) cuando la app muestra datos guardados por no poder conectarse. */
@Composable
fun BannerSinConexion() {
    val desde by CacheLocal.datosGuardadosDesde.collectAsStateWithLifecycle()
    val marca = desde ?: return
    val fecha = remember(marca) {
        Instant.ofEpochMilli(marca).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.forLanguageTag("es-CO")))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AmbarClaro)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = Color(0xFFB45309))
        Text(
            "No se pudo conectar con el servidor. Mostrando los datos guardados el $fecha. Desliza hacia abajo para actualizar.",
            style = MaterialTheme.typography.bodySmall,
            color = TextoPrincipal
        )
    }
}