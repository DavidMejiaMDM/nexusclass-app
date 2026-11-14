package com.marcosmejia.nexusclass.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marcosmejia.nexusclass.data.local.Preferencias
import com.marcosmejia.nexusclass.ui.screens.ajustes.AjustesScreen
import com.marcosmejia.nexusclass.ui.screens.auth.LoginScreen
import com.marcosmejia.nexusclass.ui.screens.auth.RecuperarScreen
import com.marcosmejia.nexusclass.ui.screens.auth.RegistroScreen
import com.marcosmejia.nexusclass.ui.screens.auth.RestablecerScreen
import com.marcosmejia.nexusclass.ui.screens.carga.CargaPdfScreen
import com.marcosmejia.nexusclass.ui.screens.carga.RevisionScreen
import com.marcosmejia.nexusclass.ui.screens.clase.ClaseFormScreen
import com.marcosmejia.nexusclass.ui.screens.clase.DetalleClaseScreen
import com.marcosmejia.nexusclass.ui.screens.horario.HorarioScreen
import com.marcosmejia.nexusclass.ui.screens.hoy.HoyScreen
import com.marcosmejia.nexusclass.ui.screens.inicio.OnboardingScreen
import com.marcosmejia.nexusclass.ui.screens.inicio.SplashScreen
import com.marcosmejia.nexusclass.ui.screens.notificaciones.NotificacionesScreen
import com.marcosmejia.nexusclass.ui.screens.perfil.PerfilScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.DetalleTareaScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.FormTareaScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.TareasScreen
import com.marcosmejia.nexusclass.ui.util.Avisos
import com.marcosmejia.nexusclass.ui.components.BannerSinConexion

private data class TabItem(
    val ruta: String,
    val titulo: String,
    val iconoActivo: ImageVector,
    val iconoInactivo: ImageVector
)

private val tabs = listOf(
    TabItem(Rutas.HOY, "Hoy", Icons.Filled.Today, Icons.Outlined.Today),
    TabItem(Rutas.HORARIO, "Horario", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    TabItem(Rutas.TAREAS, "Tareas", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    TabItem(Rutas.AJUSTES, "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings),
)

// Limpia todo el historial y deja "Hoy" como única pantalla
private fun NavHostController.irAHoy() {
    navigate(Rutas.HOY) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

// Cambio entre pestañas de la barra inferior
private fun NavHostController.irATab(ruta: String) {
    navigate(ruta) {
        popUpTo(Rutas.HOY) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation(haySesion: Boolean) {
    val nav = rememberNavController()
    val entradaActual by nav.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val mostrarBarra = tabs.any { it.ruta == rutaActual }

    // En las pantallas de acceso el degradado ocupa toda la pantalla (incluida la barra de estado)
    val esAuth = rutaActual == Rutas.LOGIN || rutaActual == Rutas.REGISTRO ||
            rutaActual == Rutas.RECUPERAR || rutaActual == Rutas.RESTABLECER ||
            (rutaActual == null && !haySesion)

    // Correo al que se envió el código de recuperación (sobrevive a la rotación de pantalla)
    var emailRecuperacion by rememberSaveable { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { Avisos.flujo.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = { BannerSinConexion() },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { if (mostrarBarra) BarraInferior(nav, rutaActual) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = if (haySesion) Rutas.SPLASH else Rutas.LOGIN,
            modifier = if (esAuth) Modifier else Modifier.padding(padding)
        ) {
            // ---------- Cuenta ----------
            composable(Rutas.LOGIN) {
                LoginScreen(
                    onRegistro = { nav.navigate(Rutas.REGISTRO) },
                    onOlvide = { nav.navigate(Rutas.RECUPERAR) }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(onLogin = { nav.popBackStack() })
            }
            composable(Rutas.RECUPERAR) {
                RecuperarScreen(
                    onVolver = { nav.popBackStack() },
                    onCodigoEnviado = { correo ->
                        emailRecuperacion = correo
                        nav.navigate(Rutas.RESTABLECER)
                    }
                )
            }
            composable(Rutas.RESTABLECER) {
                RestablecerScreen(
                    email = emailRecuperacion,
                    onVolver = { nav.popBackStack() }
                )
            }

            // ---------- Flujo inicial ----------
            composable(Rutas.SPLASH) {
                SplashScreen(onListo = { primeraVez ->
                    if (primeraVez) {
                        nav.navigate(Rutas.ONBOARDING) { popUpTo(nav.graph.id) { inclusive = true } }
                    } else {
                        nav.irAHoy()
                    }
                })
            }
            composable(Rutas.ONBOARDING) {
                OnboardingScreen(onTerminar = {
                    Preferencias.onboardingVisto = true
                    nav.navigate(Rutas.CARGA_PDF) { popUpTo(nav.graph.id) { inclusive = true } }
                })
            }
            composable(Rutas.CARGA_PDF) {
                CargaPdfScreen(
                    onVolver = { if (nav.previousBackStackEntry != null) nav.popBackStack() else nav.irAHoy() },
                    onOmitir = { nav.irAHoy() },
                    onContinuar = { nav.navigate(Rutas.REVISION) }
                )
            }
            composable(Rutas.REVISION) {
                RevisionScreen(
                    onVolver = { nav.popBackStack() },
                    onEditar = { nav.navigate(Rutas.formClase(it)) },
                    onNueva = { nav.navigate(Rutas.formClase()) },
                    onConfirmar = { nav.irAHoy() }
                )
            }

            // ---------- Pestañas principales ----------
            composable(Rutas.HOY) {
                HoyScreen(
                    onClase = { nav.navigate(Rutas.detalleClase(it)) },
                    onTarea = { nav.navigate(Rutas.detalleTarea(it)) },
                    onNuevaTarea = { nav.navigate(Rutas.formTarea()) },
                    onHorario = { nav.irATab(Rutas.HORARIO) },
                    onNotificaciones = { nav.navigate(Rutas.NOTIFICACIONES) },
                    onCargarHorario = { nav.navigate(Rutas.CARGA_PDF) }
                )
            }
            composable(Rutas.HORARIO) {
                HorarioScreen(
                    onClase = { nav.navigate(Rutas.detalleClase(it)) },
                    onNuevaClase = { nav.navigate(Rutas.formClase()) },
                    onCargarHorario = { nav.navigate(Rutas.CARGA_PDF) }
                )
            }
            composable(Rutas.TAREAS) {
                TareasScreen(
                    onTarea = { nav.navigate(Rutas.detalleTarea(it)) },
                    onNueva = { nav.navigate(Rutas.formTarea()) }
                )
            }
            composable(Rutas.AJUSTES) {
                AjustesScreen(
                    onPerfil = { nav.navigate(Rutas.PERFIL) },
                    onCargarHorario = { nav.navigate(Rutas.CARGA_PDF) },
                    onNuevaClase = { nav.navigate(Rutas.formClase()) }
                )
            }

            // ---------- Pantallas secundarias ----------
            composable(Rutas.PERFIL) {
                PerfilScreen(onVolver = { nav.popBackStack() })
            }
            composable(Rutas.NOTIFICACIONES) {
                NotificacionesScreen(
                    onVolver = { nav.popBackStack() },
                    onTarea = { nav.navigate(Rutas.detalleTarea(it)) }
                )
            }
            composable(
                Rutas.DETALLE_CLASE,
                arguments = listOf(navArgument("claseId") { type = NavType.StringType })
            ) {
                DetalleClaseScreen(
                    onVolver = { nav.popBackStack() },
                    onEditar = { nav.navigate(Rutas.formClase(it)) },
                    onNuevaTarea = { nav.navigate(Rutas.formTarea(claseId = it)) },
                    onTarea = { nav.navigate(Rutas.detalleTarea(it)) }
                )
            }
            composable(
                Rutas.EDITAR_CLASE,
                arguments = listOf(
                    navArgument("claseId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) {
                ClaseFormScreen(
                    onVolver = { nav.popBackStack() },
                    // Si venía del detalle, se cierra también (la asignatura ya no existe)
                    onEliminada = { if (!nav.popBackStack(Rutas.DETALLE_CLASE, inclusive = true)) nav.popBackStack() }
                )
            }
            composable(
                Rutas.DETALLE_TAREA,
                arguments = listOf(navArgument("tareaId") { type = NavType.StringType })
            ) {
                DetalleTareaScreen(
                    onVolver = { nav.popBackStack() },
                    onEditar = { nav.navigate(Rutas.formTarea(tareaId = it)) },
                    onClase = { nav.navigate(Rutas.detalleClase(it)) }
                )
            }
            composable(
                Rutas.FORM_TAREA,
                arguments = listOf(
                    navArgument("claseId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("tareaId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) {
                FormTareaScreen(
                    onVolver = { nav.popBackStack() },
                    onEliminada = { nav.popBackStack(Rutas.DETALLE_TAREA, inclusive = true) }
                )
            }
        }
    }
}

@Composable
private fun BarraInferior(nav: NavHostController, rutaActual: String?) {
    NavigationBar {
        tabs.forEach { tab ->
            val seleccionado = rutaActual == tab.ruta
            NavigationBarItem(
                selected = seleccionado,
                onClick = { nav.irATab(tab.ruta) },
                icon = {
                    Icon(
                        imageVector = if (seleccionado) tab.iconoActivo else tab.iconoInactivo,
                        contentDescription = tab.titulo
                    )
                },
                label = { Text(tab.titulo) }
            )
        }
    }
}