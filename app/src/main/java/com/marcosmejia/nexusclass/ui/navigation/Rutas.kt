package com.marcosmejia.nexusclass.ui.navigation

object Rutas {
    const val SPLASH = "splash"

    const val LOGIN = "login"

    const val REGISTRO = "registro"
    const val ONBOARDING = "onboarding"
    const val CARGA_PDF = "carga_pdf"
    const val REVISION = "revision"

    const val HOY = "hoy"
    const val HORARIO = "horario"
    const val TAREAS = "tareas"
    const val AJUSTES = "ajustes"
    const val NOTIFICACIONES = "notificaciones"

    // Rutas con parámetros
    const val DETALLE_CLASE = "clase/{claseId}"
    const val DETALLE_TAREA = "tarea/{tareaId}"
    const val FORM_TAREA = "form_tarea?claseId={claseId}&tareaId={tareaId}"
    const val EDITAR_CLASE = "editar_clase?claseId={claseId}"

    fun detalleClase(id: String) = "clase/$id"
    fun detalleTarea(id: String) = "tarea/$id"

    // Sin id = crear asignatura a mano; con id = editar
    fun formClase(claseId: String? = null) =
        if (claseId == null) "editar_clase" else "editar_clase?claseId=$claseId"

    // Sin parámetros = crear tarea libre; con claseId = crear para una asignatura; con tareaId = editar
    fun formTarea(claseId: String? = null, tareaId: String? = null): String {
        val params = listOfNotNull(
            claseId?.let { "claseId=$it" },
            tareaId?.let { "tareaId=$it" }
        )
        return if (params.isEmpty()) "form_tarea" else "form_tarea?" + params.joinToString("&")
    }
}