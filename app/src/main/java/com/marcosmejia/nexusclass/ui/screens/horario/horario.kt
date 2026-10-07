package com.marcosmejia.nexusclass.ui.screens.horario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HorarioViewModel(private val repo: ClaseRepository = ClaseRepository()) : ViewModel() {

    sealed interface Estado {
        data object Cargando : Estado
        data class Exito(val clases: List<ClaseDto>) : Estado
        data class Error(val mensaje: String) : Estado
    }

    private val _estado = MutableStateFlow<Estado>(Estado.Cargando)
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    init { cargar() }

    private fun aplicar(r: Resultado<List<ClaseDto>>) {
        _estado.value = when (r) {
            is Resultado.Exito -> Estado.Exito(r.datos)
            is Resultado.Fallo -> Estado.Error(r.mensaje)
        }
    }

    fun cargar() {
        _estado.value = Estado.Cargando
        viewModelScope.launch { aplicar(repo.listar()) }
    }

    fun refrescar() {
        viewModelScope.launch {
            _refrescando.value = true
            val r = repo.listar()
            if (r is Resultado.Exito || _estado.value !is Estado.Exito) aplicar(r)
            _refrescando.value = false
        }
    }

    // Al volver de otra pantalla: actualiza sin mostrar el spinner
    fun silencioso() {
        if (_estado.value is Estado.Cargando) return
        viewModelScope.launch {
            val r = repo.listar()
            if (r is Resultado.Exito) _estado.value = Estado.Exito(r.datos)
        }
    }
}