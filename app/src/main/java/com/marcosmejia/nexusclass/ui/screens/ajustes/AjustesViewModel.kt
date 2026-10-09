package com.marcosmejia.nexusclass.ui.screens.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.local.Sesion
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.AuthRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AjustesViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _eliminando = MutableStateFlow(false)
    val eliminando: StateFlow<Boolean> = _eliminando.asStateFlow()

    fun cerrarSesion() = Sesion.cerrar()

    // Borra la cuenta y todos sus datos en el servidor; luego cierra la sesión
    fun eliminarCuenta() {
        viewModelScope.launch {
            _eliminando.value = true
            when (val r = repo.eliminarCuenta()) {
                is Resultado.Exito -> Sesion.cerrar()
                is Resultado.Fallo -> Avisos.mostrar(r.mensaje)
            }
            _eliminando.value = false
        }
    }
}