package com.marcosmejia.nexusclass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcosmejia.nexusclass.data.local.Preferencias
import com.marcosmejia.nexusclass.data.local.Sesion
import com.marcosmejia.nexusclass.notifications.Alertas
import com.marcosmejia.nexusclass.notifications.Notificador
import com.marcosmejia.nexusclass.ui.navigation.AppNavigation
import com.marcosmejia.nexusclass.ui.theme.NexusClassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Preferencias.iniciar(this)
        Sesion.iniciar(this)
        Notificador.crearCanal(this)

        setContent {
            val tema by Preferencias.tema.collectAsStateWithLifecycle()
            val alertas by Preferencias.notificaciones.collectAsStateWithLifecycle()
            val usuario by Sesion.usuario.collectAsStateWithLifecycle()
            val oscuro = when (tema) {
                "OSCURO" -> true
                "CLARO" -> false
                else -> isSystemInDarkTheme()
            }

            // Las alertas solo existen mientras haya una sesión abierta
            LaunchedEffect(alertas, usuario?.id) {
                if (alertas && usuario != null) Alertas.programar(applicationContext)
                else Alertas.cancelar(applicationContext)
            }

            NexusClassTheme(darkTheme = oscuro) {
                key(usuario?.id) { AppNavigation(haySesion = usuario != null) }
            }
        }
    }
}