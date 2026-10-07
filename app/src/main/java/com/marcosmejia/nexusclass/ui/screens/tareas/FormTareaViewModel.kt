package com.marcosmejia.nexusclass.ui.screens.tareas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.remote.TareaRequest
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import com.marcosmejia.nexusclass.data.repository.TareaRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class Fin { GUARDADA, ELIMINADA }

class FormTareaViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val tareaRepo = TareaRepository()
    private val claseRepo = ClaseRepository()
    private val zona = ZoneId.of("America/Bogota")

    // Vienen de la ruta: form_tarea?claseId=...&tareaId=...
    private val tareaId: String? = savedStateHandle.get<String>("tareaId")
    private val claseIdInicial: String? = savedStateHandle.get<String>("claseId")
    val editando: Boolean = tareaId != null

    // Fecha original de la tarea: si el usuario no la toca, se reenvía tal cual
    private var original: Triple<String, LocalDate?, LocalTime?>? = null

    data class Ui(
        val cargando: Boolean = true,
        val errorCarga: String? = null,
        val guardando: Boolean = false,
        val clases: List<ClaseDto> = emptyList(),
        val titulo: String = "",
        val tipo: String = "TAREA",
        val claseId: String? = null,
        val fecha: LocalDate? = null,
        val hora: LocalTime? = null,
        val porcentaje: String = "",
        val modalidad: String = "",
        val notas: String = "",
        val completada: Boolean = false,
        val errores: Map<String, String> = emptyMap(),   // campo -> mensaje
        val errorGeneral: String? = null,
        val fin: Fin? = null
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    init { cargar() }

    fun cargar() {
        _ui.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val clases = when (val rc = claseRepo.listar()) {
                is Resultado.Exito -> rc.datos
                is Resultado.Fallo -> {
                    _ui.update { it.copy(cargando = false, errorCarga = rc.mensaje) }
                    return@launch
                }
            }
            val id = tareaId
            if (id == null) {
                _ui.update { it.copy(cargando = false, clases = clases, claseId = claseIdInicial) }
                return@launch
            }
            when (val rt = tareaRepo.obtener(id)) {
                is Resultado.Fallo -> _ui.update { it.copy(cargando = false, errorCarga = rt.mensaje) }
                is Resultado.Exito -> {
                    val t = rt.datos
                    val z = runCatching { Instant.parse(t.fechaEntrega).atZone(zona) }.getOrNull()
                    val hora = z?.toLocalTime()?.withSecond(0)?.withNano(0)
                    original = Triple(t.fechaEntrega, z?.toLocalDate(), hora)
                    _ui.update {
                        it.copy(
                            cargando = false,
                            clases = clases,
                            titulo = t.titulo,
                            tipo = t.tipo.uppercase(),
                            claseId = t.claseId,
                            fecha = z?.toLocalDate(),
                            hora = hora,
                            porcentaje = t.porcentaje?.toString() ?: "",
                            modalidad = t.modalidad.orEmpty(),
                            notas = t.notas.orEmpty(),
                            completada = t.completada
                        )
                    }
                }
            }
        }
    }

    // ----- Cambios en los campos (al editar uno se le quita su error) -----
    fun setTitulo(v: String) = _ui.update { it.copy(titulo = v, errores = it.errores - "titulo") }
    fun setTipo(v: String) = _ui.update { it.copy(tipo = v) }
    fun setClase(v: String) = _ui.update { it.copy(claseId = v, errores = it.errores - "claseId") }
    fun setFecha(v: LocalDate) = _ui.update { it.copy(fecha = v, errores = it.errores - "fechaEntrega") }
    fun setHora(v: LocalTime) = _ui.update { it.copy(hora = v, errores = it.errores - "fechaEntrega") }
    fun setPorcentaje(v: String) =
        _ui.update { it.copy(porcentaje = v.filter(Char::isDigit).take(3), errores = it.errores - "porcentaje") }
    fun setModalidad(v: String) = _ui.update { it.copy(modalidad = v) }
    fun setNotas(v: String) = _ui.update { it.copy(notas = v) }
    fun setCompletada(v: Boolean) = _ui.update { it.copy(completada = v) }

    fun guardar() {
        val s = _ui.value
        if (s.guardando) return

        val fecha = s.fecha
        val hora = s.hora
        val claseId = s.claseId
        val porc = s.porcentaje.toIntOrNull()

        // Solo validamos lo obligatorio. La regla de "fecha pasada" la aplica el servidor (HTTP 400).
        val errores = buildMap {
            if (s.titulo.isBlank()) put("titulo", "El título es obligatorio")
            if (claseId == null) put("claseId", "Selecciona una asignatura")
            if (fecha == null || hora == null) put("fechaEntrega", "Elige la fecha y la hora de entrega")
            if (s.porcentaje.isNotEmpty() && (porc == null || porc > 100)) put("porcentaje", "Debe estar entre 0 y 100")
        }
        if (errores.isNotEmpty() || fecha == null || hora == null || claseId == null) {
            _ui.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }

        val o = original
        val fechaIso = if (o != null && o.second == fecha && o.third == hora) o.first
        else ZonedDateTime.of(fecha, hora, zona).toInstant().toString()

        val req = TareaRequest(
            claseId = claseId,
            titulo = s.titulo.trim(),
            tipo = s.tipo,
            fechaEntrega = fechaIso,
            completada = if (editando) s.completada else null,
            porcentaje = porc,
            modalidad = s.modalidad.trim(),
            notas = s.notas.trim()
        )

        _ui.update { it.copy(guardando = true, errores = emptyMap(), errorGeneral = null) }
        viewModelScope.launch {
            val id = tareaId
            val r = if (id == null) tareaRepo.crear(req) else tareaRepo.actualizar(id, req)
            when (r) {
                is Resultado.Exito -> {
                    Avisos.mostrar(if (id == null) "Tarea creada" else "Cambios guardados")
                    _ui.update { it.copy(guardando = false, fin = Fin.GUARDADA) }
                }
                is Resultado.Fallo -> {
                    val campo = r.campo
                    _ui.update {
                        if (campo != null && campo in CAMPOS) {
                            // Ej. 400 FECHA_PASADA -> se marca el campo fechaEntrega en rojo
                            it.copy(guardando = false, errores = mapOf(campo to r.mensaje))
                        } else {
                            it.copy(guardando = false, errorGeneral = r.mensaje)
                        }
                    }
                }
            }
        }
    }

    fun eliminar() {
        val id = tareaId ?: return
        viewModelScope.launch {
            when (val r = tareaRepo.eliminar(id)) {
                is Resultado.Exito -> {
                    Avisos.mostrar("Tarea eliminada")
                    _ui.update { it.copy(fin = Fin.ELIMINADA) }
                }
                is Resultado.Fallo -> _ui.update { it.copy(errorGeneral = r.mensaje) }
            }
        }
    }

    private companion object {
        val CAMPOS = setOf("titulo", "claseId", "fechaEntrega", "porcentaje")
    }
}