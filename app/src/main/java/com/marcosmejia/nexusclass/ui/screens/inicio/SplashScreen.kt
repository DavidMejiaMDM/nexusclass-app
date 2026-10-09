package com.marcosmejia.nexusclass.ui.screens.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.marcosmejia.nexusclass.R
import com.marcosmejia.nexusclass.data.local.Preferencias
import com.marcosmejia.nexusclass.ui.theme.AzulUA
import com.marcosmejia.nexusclass.ui.theme.AzulUAOscuro
import kotlinx.coroutines.delay

// true  = el logo va sobre un círculo blanco (úsalo si tu logo es oscuro o de colores)
// false = el logo se dibuja directo sobre el fondo azul (úsalo si tu logo es blanco o claro)
private const val CIRCULO_BLANCO = true

@Composable
fun SplashScreen(onListo: (primeraVez: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onListo(!Preferencias.onboardingVisto)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AzulUA, AzulUAOscuro))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (CIRCULO_BLANCO) {
                Box(
                    modifier = Modifier.size(140.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_app),
                        contentDescription = "Logo de Organizador UA",
                        modifier = Modifier.size(92.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.logo_app),
                    contentDescription = "Logo de Organizador UA",
                    modifier = Modifier.size(140.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Si tu logo ya incluye el nombre de la app, puedes borrar estas dos líneas
            Text("NexusClass", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "Tu mejor aliado",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
        }
    }
}