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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marcosmejia.nexusclass.ui.components.Accion
import com.marcosmejia.nexusclass.ui.components.PantallaEnConstruccion
import com.marcosmejia.nexusclass.ui.screens.ajustes.AjustesScreen
import com.marcosmejia.nexusclass.ui.screens.carga.CargaPdfScreen
import com.marcosmejia.nexusclass.ui.screens.carga.RevisionScreen
import com.marcosmejia.nexusclass.ui.screens.clase.ClaseFormScreen
import com.marcosmejia.nexusclass.ui.screens.clase.DetalleClaseScreen
import com.marcosmejia.nexusclass.ui.screens.horario.HorarioScreen
import com.marcosmejia.nexusclass.ui.screens.hoy.HoyScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.DetalleTareaScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.FormTareaScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.TareasScreen
import com.marcosmejia.nexusclass.ui.util.Avisos

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
fun AppNavigation() {
    val nav = rememberNavController()
    val entradaActual by nav.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val mostrarBarra = tabs.any { it.ruta == rutaActual }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { Avisos.flujo.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { if (mostrarBarra) BarraInferior(nav, rutaActual) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Rutas.HOY,
            modifier = Modifier.padding(padding)
        ) {
            // ---------- Flujo inicial ----------
            composable(Rutas.SPLASH) {
                PantallaEnConstruccion("Splash", listOf(Accion("Ir a Hoy") { nav.irAHoy() }))
            }
            composable(Rutas.ONBOARDING) {
                PantallaEnConstruccion("Onboarding", listOf(Accion("Ir a cargar horario") { nav.navigate(Rutas.CARGA_PDF) }))
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
                    onCargarHorario = { nav.navigate(Rutas.CARGA_PDF) },
                    onNuevaClase = { nav.navigate(Rutas.formClase()) }
                )
            }

            // ---------- Pantallas secundarias ----------
            composable(Rutas.NOTIFICACIONES) {
                PantallaEnConstruccion("Notificaciones", listOf(Accion("Volver") { nav.popBackStack() }))
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