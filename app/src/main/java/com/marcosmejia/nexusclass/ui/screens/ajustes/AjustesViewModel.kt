package com.marcosmejia.nexusclass.ui.screens.ajustes


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AjustesViewModel : ViewModel() {
    private val repo = ClaseRepository()

    private val _eliminando = MutableStateFlow(false)
    val eliminando: StateFlow<Boolean> = _eliminando.asStateFlow()

    // Borrar una clase borra también sus tareas (lo hace tu API)
    fun eliminarTodo() {
        viewModelScope.launch {
            _eliminando.value = true
            when (val r = repo.listar()) {
                is Resultado.Fallo -> Avisos.mostrar(r.mensaje)
                is Resultado.Exito -> {
                    var fallo: String? = null
                    for (c in r.datos) {
                        val x = repo.eliminar(c.id)
                        if (x is Resultado.Fallo) { fallo = x.mensaje; break }
                    }
                    Avisos.mostrar(fallo ?: "Se eliminaron todas tus clases y tareas")
                }
            }
            _eliminando.value = false
        }
    }
}