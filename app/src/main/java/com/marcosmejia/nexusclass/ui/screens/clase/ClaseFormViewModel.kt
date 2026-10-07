package com.marcosmejia.nexusclass.ui.screens.clase

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.ClaseRequest
import com.marcosmejia.nexusclass.data.remote.HorarioDto
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

enum class FinClase { GUARDADA, ELIMINADA }

data class HorarioEdit(
    val key: Int,
    val dia: String = "LUNES",
    val inicio: LocalTime? = null,
    val fin: LocalTime? = null
)

class ClaseFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repo = ClaseRepository()
    private val claseId: String? = savedStateHandle.get<String>("claseId")
    val editando: Boolean = claseId != null

    private var colorOriginal: String? = null
    private var contador = 1

    data class Ui(
        val cargando: Boolean = false,
        val errorCarga: String? = null,
        val guardando: Boolean = false,
        val nombre: String = "",
        val docente: String = "",
        val salon: String = "",
        val edificio: String = "",
        val horarios: List<HorarioEdit> = emptyList(),
        val errores: Map<String, String> = emptyMap(),
        val errorGeneral: String? = null,
        val fin: FinClase? = null
    )

    private val _ui = MutableStateFlow(
        Ui(
            cargando = claseId != null,
            horarios = if (claseId == null) listOf(HorarioEdit(0)) else emptyList()
        )
    )
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    init { if (claseId != null) cargar() }

    private fun hora(texto: String): LocalTime? = runCatching { LocalTime.parse(texto) }.getOrNull()

    fun cargar() {
        val id = claseId ?: return
        _ui.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            when (val r = repo.obtener(id)) {
                is Resultado.Fallo -> _ui.update { it.copy(cargando = false, errorCarga = r.mensaje) }
                is Resultado.Exito -> {
                    val c = r.datos
                    colorOriginal = c.color
                    _ui.update {
                        it.copy(
                            cargando = false,
                            nombre = c.nombreMateria,
                            docente = c.docente.orEmpty(),
                            salon = c.salon.orEmpty(),
                            edificio = c.edificio.orEmpty(),
                            horarios = c.horarios.orEmpty().map { h ->
                                HorarioEdit(contador++, h.diaSemana, hora(h.horaInicio), hora(h.horaFin))
                            }
                        )
                    }
                }
            }
        }
    }

    fun setNombre(v: String) = _ui.update { it.copy(nombre = v, errores = it.errores - "nombre") }
    fun setDocente(v: String) = _ui.update { it.copy(docente = v) }
    fun setSalon(v: String) = _ui.update { it.copy(salon = v) }
    fun setEdificio(v: String) = _ui.update { it.copy(edificio = v) }

    fun agregarHorario() = _ui.update { it.copy(horarios = it.horarios + HorarioEdit(contador++)) }
    fun quitarHorario(key: Int) = _ui.update { it.copy(horarios = it.horarios.filter { h -> h.key != key }) }
    fun setDia(key: Int, d: String) = cambiar(key) { it.copy(dia = d) }
    fun setInicio(key: Int, t: LocalTime) = cambiar(key) { it.copy(inicio = t) }
    fun setFin(key: Int, t: LocalTime) = cambiar(key) { it.copy(fin = t) }

    private fun cambiar(key: Int, f: (HorarioEdit) -> HorarioEdit) = _ui.update { s ->
        s.copy(horarios = s.horarios.map { if (it.key == key) f(it) else it }, errores = s.errores - "horarios")
    }

    fun guardar() {
        val s = _ui.value
        if (s.guardando) return

        val completos = s.horarios.mapNotNull { h ->
            val i = h.inicio
            val f = h.fin
            if (i != null && f != null) Triple(h.dia, i, f) else null
        }
        val errores = buildMap {
            if (s.nombre.isBlank()) put("nombre", "El nombre de la materia es obligatorio")
            if (s.horarios.isEmpty()) put("horarios", "Agrega al menos un horario")
            else if (completos.size != s.horarios.size) put("horarios", "Completa la hora de inicio y de fin de cada horario")
            else if (completos.any { !it.third.isAfter(it.second) }) put("horarios", "La hora de fin debe ser posterior a la de inicio")
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }

        val req = ClaseRequest(
            nombreMateria = s.nombre.trim(),
            docente = s.docente.trim(),
            salon = s.salon.trim(),
            edificio = s.edificio.trim(),
            color = colorOriginal,
            horarios = completos.map { HorarioDto(it.first, it.second.toString(), it.third.toString()) }
        )

        _ui.update { it.copy(guardando = true, errores = emptyMap(), errorGeneral = null) }
        viewModelScope.launch {
            val id = claseId
            val r = if (id == null) repo.crear(req) else repo.actualizar(id, req)
            when (r) {
                is Resultado.Exito -> {
                    Avisos.mostrar(if (id == null) "Asignatura creada" else "Cambios guardados")
                    _ui.update { it.copy(guardando = false, fin = FinClase.GUARDADA) }
                }
                is Resultado.Fallo -> _ui.update {
                    if (r.campo == "nombreMateria") it.copy(guardando = false, errores = mapOf("nombre" to r.mensaje))
                    else it.copy(guardando = false, errorGeneral = r.mensaje)
                }
            }
        }
    }

    fun eliminar() {
        val id = claseId ?: return
        viewModelScope.launch {
            when (val r = repo.eliminar(id)) {
                is Resultado.Exito -> {
                    Avisos.mostrar("Asignatura eliminada (y sus tareas)")
                    _ui.update { it.copy(fin = FinClase.ELIMINADA) }
                }
                is Resultado.Fallo -> _ui.update { it.copy(errorGeneral = r.mensaje) }
            }
        }
    }
}