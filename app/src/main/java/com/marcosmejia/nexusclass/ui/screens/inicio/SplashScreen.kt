package com.marcosmejia.nexusclass.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.marcosmejia.nexusclass.data.local.Preferencias
import com.marcosmejia.nexusclass.ui.theme.AzulUA
import com.marcosmejia.nexusclass.ui.theme.AzulUAOscuro
import kotlinx.coroutines.delay

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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("UA", style = MaterialTheme.typography.headlineMedium, color = AzulUA)
            }
            Text("Organizador UA", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Uniautónoma del Cauca", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
        }
    }
}