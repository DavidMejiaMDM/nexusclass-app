package com.marcosmejia.nexusclass.data.local

import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.SesionDto
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.data.remote.TodayResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/** Calcula la pantalla "Hoy" en el teléfono, con la misma lógica que GET /clases/today del servidor. */
object HoyLocal {
    private val ZONA: ZoneId = ZoneId.of("America/Bogota")
    private val DIAS = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")

    private class Sesion(val clase: ClaseDto, val inicio: String, val fin: String, val ini: Int, val fi: Int)

    private fun minutos(hhmm: String): Int {
        val p = hhmm.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    fun calcular(clases: List<ClaseDto>, tareas: List<TareaDto>): TodayResponse {
        val ahora = ZonedDateTime.now(ZONA)
        val dia = DIAS[ahora.dayOfWeek.value - 1]
        val minAhora = ahora.hour * 60 + ahora.minute

        val sesiones = clases.flatMap { c ->
            c.horarios.orEmpty()
                .filter { it.diaSemana == dia }
                .map { Sesion(c, it.horaInicio, it.horaFin, minutos(it.horaInicio), minutos(it.horaFin)) }
        }.sortedBy { it.ini }

        fun dto(
            s: Sesion,
            estado: String? = null,
            restantes: Int? = null,
            progreso: Int? = null,
            pendientes: Int? = null,
            paraInicio: Int? = null
        ) = SesionDto(
            claseId = s.clase.id,
            nombreMateria = s.clase.nombreMateria,
            docente = s.clase.docente,
            salon = s.clase.salon,
            edificio = s.clase.edificio,
            color = s.clase.color,
            diaSemana = dia,
            horaInicio = s.inicio,
            horaFin = s.fin,
            estado = estado,
            minutosRestantes = restantes,
            progreso = progreso,
            tareasPendientes = pendientes,
            minutosParaInicio = paraInicio
        )

        val clasesHoy = sesiones.map { s ->
            dto(s, estado = when {
                s.fi <= minAhora -> "PASADA"
                s.ini <= minAhora -> "EN_CURSO"
                else -> "PROXIMA"
            })
        }

        val actual = sesiones.firstOrNull { it.ini <= minAhora && minAhora < it.fi }
        val proxima = sesiones.firstOrNull { it.ini > minAhora }

        val claseActual = actual?.let { s ->
            dto(
                s,
                restantes = s.fi - minAhora,
                progreso = ((minAhora - s.ini) * 100.0 / (s.fi - s.ini)).roundToInt(),
                pendientes = tareas.count { it.claseId == s.clase.id && !it.completada }
            )
        }
        val proximaClase = proxima?.let { dto(it, paraInicio = it.ini - minAhora) }

        val estadoDia = when {
            claseActual != null -> "EN_CURSO"
            proximaClase != null -> "ENTRE_CLASES"
            clasesHoy.isNotEmpty() -> "TERMINADO"
            clases.isEmpty() -> "SIN_HORARIO"
            else -> "SIN_CLASES"
        }

        return TodayResponse(
            ahora = ahora.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            diaSemana = dia,
            estadoDia = estadoDia,
            claseActual = claseActual,
            proximaClase = proximaClase,
            clasesHoy = clasesHoy
        )
    }
}