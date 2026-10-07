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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.marcosmejia.nexusclass.ui.screens.tareas.DetalleTareaScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.FormTareaScreen
import com.marcosmejia.nexusclass.ui.screens.tareas.TareasScreen
import com.marcosmejia.nexusclass.ui.util.Avisos

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marcosmejia.nexusclass.ui.components.Accion
import com.marcosmejia.nexusclass.ui.components.PantallaEnConstruccion
import com.marcosmejia.nexusclass.ui.screens.hoy.HoyScreen

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
                PantallaEnConstruccion("Splash", listOf(
                    Accion("Ir a Onboarding") { nav.navigate(Rutas.ONBOARDING) }
                ))
            }
            composable(Rutas.ONBOARDING) {
                PantallaEnConstruccion("Onboarding", listOf(
                    Accion("Siguiente: cargar horario") { nav.navigate(Rutas.CARGA_PDF) }
                ))
            }
            composable(Rutas.CARGA_PDF) {
                PantallaEnConstruccion("Carga de horario PDF", listOf(
                    Accion("Procesar y confirmar clases") { nav.navigate(Rutas.REVISION) },
                    Accion("Volver") { nav.popBackStack() }
                ))
            }
            composable(Rutas.REVISION) {
                PantallaEnConstruccion("Revisión de clases", listOf(
                    Accion("Confirmar e ir a Hoy") { nav.navigate(Rutas.HOY) },
                    Accion("Volver") { nav.popBackStack() }
                ))
            }

            // ---------- Pestañas principales ----------
            composable(Rutas.HOY) {
                HoyScreen(
                    onClase = { nav.navigate(Rutas.detalleClase(it)) },
                    onTarea = { nav.navigate(Rutas.detalleTarea(it)) },
                    onNuevaTarea = { nav.navigate(Rutas.formTarea()) },
                    onHorario = {
                        nav.navigate(Rutas.HORARIO) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNotificaciones = { nav.navigate(Rutas.NOTIFICACIONES) },
                    onCargarHorario = { nav.navigate(Rutas.CARGA_PDF) }
                )
            }
            composable(Rutas.HORARIO) {
                PantallaEnConstruccion("Horario semanal", listOf(
                    Accion("Detalle de asignatura (prueba)") { nav.navigate(Rutas.detalleClase("abc123")) }
                ))
            }
            composable(Rutas.TAREAS) {
                TareasScreen(
                    onTarea = { nav.navigate(Rutas.detalleTarea(it)) },
                    onNueva = { nav.navigate(Rutas.formTarea()) }
                )
            }

            composable(Rutas.AJUSTES) {
                PantallaEnConstruccion("Ajustes", listOf(
                    Accion("Actualizar horario (PDF)") { nav.navigate(Rutas.CARGA_PDF) }
                ))
            }

            // ---------- Pantallas secundarias ----------
            composable(Rutas.NOTIFICACIONES) {
                PantallaEnConstruccion("Notificaciones", listOf(
                    Accion("Volver") { nav.popBackStack() }
                ))
            }
            composable(
                Rutas.DETALLE_CLASE,
                arguments = listOf(navArgument("claseId") { type = NavType.StringType })
            ) { entrada ->
                val id = entrada.arguments?.getString("claseId").orEmpty()
                PantallaEnConstruccion("Detalle de asignatura\nid = $id", listOf(
                    Accion("Agregar tarea a esta asignatura") { nav.navigate(Rutas.formTarea(claseId = id)) },
                    Accion("Volver") { nav.popBackStack() }
                ))
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
                    // Si se elimina desde el formulario, se cierra también el detalle (ya no existe)
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
                onClick = {
                    nav.navigate(tab.ruta) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
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