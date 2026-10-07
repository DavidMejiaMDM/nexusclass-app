package com.marcosmejia.nexusclass.ui.screens.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import com.marcosmejia.nexusclass.data.repository.TareaRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TareasViewModel(
    private val tareaRepo: TareaRepository = TareaRepository(),
    private val claseRepo: ClaseRepository = ClaseRepository()
) : ViewModel() {

    data class Datos(val tareas: List<TareaDto>, val clases: List<ClaseDto>)

    sealed interface Estado {
        data object Cargando : Estado
        data class Exito(val datos: Datos) : Estado
        data class Error(val mensaje: String) : Estado
    }

    data class Filtros(
        val estado: String = "PENDIENTES",   // PENDIENTES | COMPLETADAS | TODAS
        val tipo: String? = null,            // TAREA | PARCIAL | TALLER
        val claseId: String? = null,
        val texto: String = ""
    )

    private val _estado = MutableStateFlow<Estado>(Estado.Cargando)
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private val _filtros = MutableStateFlow(Filtros())
    val filtros: StateFlow<Filtros> = _filtros.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    init { cargar() }

    private suspend fun consultar(): Resultado<Datos> = coroutineScope {
        val t = async { tareaRepo.listar() }
        val c = async { claseRepo.listar() }
        val rt = t.await()
        val rc = c.await()
        when (rt) {
            is Resultado.Fallo -> rt
            is Resultado.Exito -> {
                val clases = when (rc) {
                    is Resultado.Exito -> rc.datos
                    is Resultado.Fallo -> emptyList()
                }
                Resultado.Exito(Datos(rt.datos, clases))
            }
        }
    }

    private fun aplicar(r: Resultado<Datos>) {
        _estado.value = when (r) {
            is Resultado.Exito -> Estado.Exito(r.datos)
            is Resultado.Fallo -> Estado.Error(r.mensaje)
        }
    }

    fun cargar() {
        _estado.value = Estado.Cargando
        viewModelScope.launch { aplicar(consultar()) }
    }

    fun refrescar() {
        viewModelScope.launch {
            _refrescando.value = true
            val r = consultar()
            if (r is Resultado.Exito || _estado.value !is Estado.Exito) aplicar(r)
            _refrescando.value = false
        }
    }

    // Al volver de otra pantalla: actualiza sin mostrar el spinner
    fun silencioso() {
        if (_estado.value is Estado.Cargando) return
        viewModelScope.launch {
            val r = consultar()
            if (r is Resultado.Exito) _estado.value = Estado.Exito(r.datos)
        }
    }

    // ----- Filtros (se aplican en el teléfono, sobre la lista ya descargada) -----
    fun setEstado(v: String) = _filtros.update { it.copy(estado = v) }
    fun setTipo(v: String?) = _filtros.update { it.copy(tipo = v) }
    fun setClase(v: String?) = _filtros.update { it.copy(claseId = v) }
    fun setTexto(v: String) = _filtros.update { it.copy(texto = v) }

    fun filtrar(tareas: List<TareaDto>, f: Filtros): List<TareaDto> {
        val texto = f.texto.trim()
        return tareas.filter { t ->
            val porEstado = when (f.estado) {
                "PENDIENTES" -> !t.completada
                "COMPLETADAS" -> t.completada
                else -> true
            }
            val porTipo = f.tipo == null || t.tipo.equals(f.tipo, ignoreCase = true)
            val porClase = f.claseId == null || t.claseId == f.claseId
            val porTexto = texto.isEmpty() ||
                    t.titulo.contains(texto, ignoreCase = true) ||
                    (t.clase?.nombreMateria?.contains(texto, ignoreCase = true) == true)
            porEstado && porTipo && porClase && porTexto
        }
    }

    // ----- Completar / descompletar con un toque -----
    fun alternar(t: TareaDto) {
        viewModelScope.launch {
            when (val r = tareaRepo.completar(t.id, !t.completada)) {
                is Resultado.Exito -> {
                    val e = _estado.value
                    if (e is Estado.Exito) {
                        val nuevas = e.datos.tareas.map { if (it.id == r.datos.id) r.datos else it }
                        _estado.value = Estado.Exito(e.datos.copy(tareas = nuevas))
                    }
                    Avisos.mostrar(if (r.datos.completada) "Tarea completada" else "Tarea marcada como pendiente")
                }
                is Resultado.Fallo -> Avisos.mostrar(r.mensaje)
            }
        }
    }
}