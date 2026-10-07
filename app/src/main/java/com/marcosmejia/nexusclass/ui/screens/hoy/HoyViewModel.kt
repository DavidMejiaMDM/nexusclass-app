package com.marcosmejia.nexusclass.ui.screens.hoy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.data.remote.TodayResponse
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import com.marcosmejia.nexusclass.data.repository.TareaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HoyViewModel(
    private val claseRepo: ClaseRepository = ClaseRepository(),
    private val tareaRepo: TareaRepository = TareaRepository()
) : ViewModel() {

    // PARA PRUEBAS: pon una fecha como "2026-10-05T08:30:00" para simular una hora.
    // Déjalo en null para usar la hora real.
    private val simular: String? = null

    data class Datos(val hoy: TodayResponse, val urgentes: List<TareaDto>)

    sealed interface Estado {
        data object Cargando : Estado
        data class Exito(val datos: Datos) : Estado
        data class Error(val mensaje: String, val sinConexion: Boolean) : Estado
    }

    private val _estado = MutableStateFlow<Estado>(Estado.Cargando)
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    init {
        cargar()
        // Cada minuto se actualiza solo: avanzan la cuenta regresiva y la barra de progreso
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                silencioso()
            }
        }
    }

    // Pide la clase de hoy y las tareas urgentes (24 h) al mismo tiempo
    private suspend fun consultar(): Resultado<Datos> = coroutineScope {
        val hoy = async { claseRepo.hoy(simular) }
        val urgentes = async { tareaRepo.listar(urgentes = true) }
        val r1 = hoy.await()
        val r2 = urgentes.await()
        val listaUrgentes = when (r2) {
            is Resultado.Exito -> r2.datos
            is Resultado.Fallo -> emptyList()
        }
        when (r1) {
            is Resultado.Fallo -> r1
            is Resultado.Exito -> Resultado.Exito(Datos(r1.datos, listaUrgentes))
        }
    }

    fun cargar() {
        _estado.value = Estado.Cargando
        viewModelScope.launch { aplicar(consultar()) }
    }

    // Pull-to-refresh: si falla, conserva lo que ya se veía
    fun refrescar() {
        viewModelScope.launch {
            _refrescando.value = true
            val r = consultar()
            if (r is Resultado.Exito || _estado.value !is Estado.Exito) aplicar(r)
            _refrescando.value = false
        }
    }

    // Actualiza sin mostrar el spinner (cada minuto y al volver de otra pantalla)
    fun silencioso() {
        if (_estado.value is Estado.Cargando) return
        viewModelScope.launch {
            val r = consultar()
            if (r is Resultado.Exito) _estado.value = Estado.Exito(r.datos)
        }
    }

    fun completar(id: String) {
        viewModelScope.launch {
            tareaRepo.completar(id, true)
            silencioso()
        }
    }

    private fun aplicar(r: Resultado<Datos>) {
        _estado.value = when (r) {
            is Resultado.Exito -> Estado.Exito(r.datos)
            is Resultado.Fallo -> Estado.Error(r.mensaje, r.sinConexion)
        }
    }
}