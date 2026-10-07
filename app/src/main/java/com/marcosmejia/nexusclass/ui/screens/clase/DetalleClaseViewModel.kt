package com.marcosmejia.nexusclass.ui.screens.clase

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import com.marcosmejia.nexusclass.data.repository.TareaRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleClaseViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val claseRepo = ClaseRepository()
    private val tareaRepo = TareaRepository()
    val id: String = savedStateHandle.get<String>("claseId").orEmpty()

    sealed interface Estado {
        data object Cargando : Estado
        data class Exito(val clase: ClaseDto) : Estado
        data class Error(val mensaje: String) : Estado
    }

    private val _estado = MutableStateFlow<Estado>(Estado.Cargando)
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        _estado.value = Estado.Cargando
        viewModelScope.launch {
            _estado.value = when (val r = claseRepo.obtener(id)) {
                is Resultado.Exito -> Estado.Exito(r.datos)
                is Resultado.Fallo -> Estado.Error(r.mensaje)
            }
        }
    }

    fun silencioso() {
        if (_estado.value is Estado.Cargando) return
        viewModelScope.launch {
            val r = claseRepo.obtener(id)
            if (r is Resultado.Exito) _estado.value = Estado.Exito(r.datos)
        }
    }

    fun alternar(t: TareaDto) {
        viewModelScope.launch {
            when (val r = tareaRepo.completar(t.id, !t.completada)) {
                is Resultado.Exito -> {
                    Avisos.mostrar(if (r.datos.completada) "Tarea completada" else "Tarea marcada como pendiente")
                    silencioso()
                }
                is Resultado.Fallo -> Avisos.mostrar(r.mensaje)
            }
        }
    }
}