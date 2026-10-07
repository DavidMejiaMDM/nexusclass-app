package com.marcosmejia.nexusclass.data.repository


import com.marcosmejia.nexusclass.data.remote.ApiService
import com.marcosmejia.nexusclass.data.remote.ClaseRequest
import com.marcosmejia.nexusclass.data.remote.CompletarRequest
import com.marcosmejia.nexusclass.data.remote.Red
import com.marcosmejia.nexusclass.data.remote.TareaRequest
import com.marcosmejia.nexusclass.data.remote.llamarApi
import okhttp3.MultipartBody

class ClaseRepository(private val api: ApiService = Red.api) {
    suspend fun hoy(simular: String? = null) = llamarApi { api.hoy(simular) }
    suspend fun listar(dia: String? = null) = llamarApi { api.clases(dia) }
    suspend fun obtener(id: String) = llamarApi { api.clase(id) }
    suspend fun subirPdf(pdf: MultipartBody.Part, reemplazar: Boolean? = null) =
        llamarApi { api.subirHorario(pdf, reemplazar) }
    suspend fun crear(clase: ClaseRequest) = llamarApi { api.crearClase(clase) }
    suspend fun actualizar(id: String, clase: ClaseRequest) = llamarApi { api.actualizarClase(id, clase) }
    suspend fun eliminar(id: String) = llamarApi { api.eliminarClase(id) }
}

class TareaRepository(private val api: ApiService = Red.api) {
    suspend fun listar(
        classId: String? = null,
        estado: String? = null,
        tipo: String? = null,
        urgentes: Boolean? = null
    ) = llamarApi { api.tareas(classId, estado, tipo, urgentes) }

    suspend fun obtener(id: String) = llamarApi { api.tarea(id) }
    suspend fun crear(tarea: TareaRequest) = llamarApi { api.crearTarea(tarea) }
    suspend fun actualizar(id: String, tarea: TareaRequest) = llamarApi { api.actualizarTarea(id, tarea) }
    suspend fun completar(id: String, completada: Boolean = true) =
        llamarApi { api.completar(id, CompletarRequest(completada)) }
    suspend fun eliminar(id: String) = llamarApi { api.eliminarTarea(id) }
}