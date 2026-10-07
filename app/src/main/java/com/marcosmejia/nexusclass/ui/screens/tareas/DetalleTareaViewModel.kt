package com.marcosmejia.nexusclass.ui.screens.tareas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.data.repository.TareaRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleTareaViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repo = TareaRepository()
    private val id: String = savedStateHandle.get<String>("tareaId").orEmpty()

    sealed interface Estado {
        data object Cargando : Estado
        data class Exito(val tarea: TareaDto) : Estado
        data class Error(val mensaje: String) : Estado
    }

    private val _estado = MutableStateFlow<Estado>(Estado.Cargando)
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private val _eliminada = MutableStateFlow(false)
    val eliminada: StateFlow<Boolean> = _eliminada.asStateFlow()

    init { cargar() }

    fun cargar() {
        _estado.value = Estado.Cargando
        viewModelScope.launch {
            _estado.value = when (val r = repo.obtener(id)) {
                is Resultado.Exito -> Estado.Exito(r.datos)
                is Resultado.Fallo -> Estado.Error(r.mensaje)
            }
        }
    }

    // Al volver del formulario de edición
    fun silencioso() {
        if (_estado.value is Estado.Cargando || _eliminada.value) return
        viewModelScope.launch {
            val r = repo.obtener(id)
            if (r is Resultado.Exito) _estado.value = Estado.Exito(r.datos)
        }
    }

    fun alternar() {
        val t = (_estado.value as? Estado.Exito)?.tarea ?: return
        viewModelScope.launch {
            when (val r = repo.completar(t.id, !t.completada)) {
                is Resultado.Exito -> {
                    _estado.value = Estado.Exito(r.datos)
                    Avisos.mostrar(if (r.datos.completada) "Tarea completada" else "Tarea marcada como pendiente")
                }
                is Resultado.Fallo -> Avisos.mostrar(r.mensaje)
            }
        }
    }

    fun eliminar() {
        viewModelScope.launch {
            when (val r = repo.eliminar(id)) {
                is Resultado.Exito -> {
                    Avisos.mostrar("Tarea eliminada")
                    _eliminada.value = true
                }
                is Resultado.Fallo -> Avisos.mostrar(r.mensaje)
            }
        }
    }
}