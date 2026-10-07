package com.marcosmejia.nexusclass.data.remote

import com.google.gson.annotations.SerializedName

// ---------- Clases / asignaturas ----------
data class HorarioDto(
    val diaSemana: String,   // LUNES ... DOMINGO
    val horaInicio: String,  // "10:00"
    val horaFin: String
)

data class ClaseDto(
    @SerializedName("_id") val id: String,
    val nombreMateria: String,
    val docente: String? = null,
    val salon: String? = null,
    val edificio: String? = null,
    val periodo: String? = null,
    val color: String? = null,
    val grupo: String? = null,
    val jornada: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val horarios: List<HorarioDto>? = null,
    // Solo vienen en algunos endpoints:
    val sesion: HorarioDto? = null,          // GET /clases?dia=
    val tareas: List<TareaDto>? = null,      // GET /clases/{id}
    val totalTareas: Int? = null,
    val tareasPendientes: Int? = null
)

// ---------- Tareas ----------
data class ClaseResumenDto(
    @SerializedName("_id") val id: String,
    val nombreMateria: String,
    val color: String? = null,
    val docente: String? = null,
    val salon: String? = null,
    val edificio: String? = null
)

data class TareaDto(
    @SerializedName("_id") val id: String,
    val claseId: String,
    val titulo: String,
    val tipo: String,              // TAREA | PARCIAL | TALLER
    val fechaEntrega: String,      // ISO 8601 en UTC
    val completada: Boolean,
    val porcentaje: Int? = null,
    val modalidad: String? = null,
    val notas: String? = null,
    val clase: ClaseResumenDto? = null,
    val horasRestantes: Double? = null,
    val urgente: Boolean? = null
)

// ---------- Pantalla Hoy ----------
data class SesionDto(
    val claseId: String,
    val nombreMateria: String,
    val docente: String? = null,
    val salon: String? = null,
    val edificio: String? = null,
    val color: String? = null,
    val diaSemana: String? = null,
    val horaInicio: String,
    val horaFin: String,
    val estado: String? = null,            // en clasesHoy: PASADA | EN_CURSO | PROXIMA
    val minutosRestantes: Int? = null,     // solo claseActual
    val progreso: Int? = null,             // solo claseActual (0-100)
    val tareasPendientes: Int? = null,     // solo claseActual
    val minutosParaInicio: Int? = null     // solo proximaClase
)

data class TodayResponse(
    val ahora: String,
    val diaSemana: String,
    val estadoDia: String,   // EN_CURSO | ENTRE_CLASES | TERMINADO | SIN_CLASES | SIN_HORARIO
    val claseActual: SesionDto?,
    val proximaClase: SesionDto?,
    val clasesHoy: List<SesionDto>
)

// ---------- Respuestas varias ----------
data class UploadResponse(
    val guardado: Boolean,
    val total: Int,
    val creadas: Int? = null,
    val omitidas: Int? = null,
    val clases: List<ClaseDto>
)

data class EliminarClaseResponse(val mensaje: String, val tareasEliminadas: Int)

data class ErrorApi(val error: String?, val mensaje: String?, val campo: String?)

// ---------- Cuerpos que enviamos ----------
// Los campos null NO se envían, así sirve para crear y para editar parcialmente
data class TareaRequest(
    val claseId: String? = null,
    val titulo: String? = null,
    val tipo: String? = null,
    val fechaEntrega: String? = null,
    val completada: Boolean? = null,
    val porcentaje: Int? = null,
    val modalidad: String? = null,
    val notas: String? = null
)

data class ClaseRequest(
    val nombreMateria: String? = null,
    val docente: String? = null,
    val salon: String? = null,
    val edificio: String? = null,
    val periodo: String? = null,
    val color: String? = null,
    val horarios: List<HorarioDto>? = null
)

data class CompletarRequest(val completada: Boolean)