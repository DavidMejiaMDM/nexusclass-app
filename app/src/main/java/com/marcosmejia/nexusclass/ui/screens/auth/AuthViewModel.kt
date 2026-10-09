package com.marcosmejia.nexusclass.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.local.Sesion
import com.marcosmejia.nexusclass.data.remote.AuthResponse
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    data class Ui(
        val cargando: Boolean = false,
        val errores: Map<String, String> = emptyMap(),   // campo -> mensaje
        val errorGeneral: String? = null
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    fun quitarError(campo: String) =
        _ui.update { it.copy(errores = it.errores - campo, errorGeneral = null) }

    fun entrar(email: String, password: String) {
        if (_ui.value.cargando) return
        val errores = buildMap {
            if (email.isBlank()) put("email", "Escribe tu correo")
            if (password.isEmpty()) put("password", "Escribe tu contraseña")
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }
        _ui.update { Ui(cargando = true) }
        viewModelScope.launch { resolver(repo.entrar(email.trim(), password)) }
    }

    fun registrar(nombre: String, email: String, password: String, confirmar: String) {
        if (_ui.value.cargando) return
        val errores = buildMap {
            if (nombre.trim().length < 2) put("nombre", "Escribe tu nombre")
            if (!email.trim().contains("@") || !email.trim().contains(".")) put("email", "Escribe un correo válido")
            if (password.length < 8) put("password", "Debe tener al menos 8 caracteres")
            if (confirmar != password) put("confirmar", "Las contraseñas no coinciden")
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }
        _ui.update { Ui(cargando = true) }
        viewModelScope.launch { resolver(repo.registrar(nombre.trim(), email.trim(), password)) }
    }

    private fun resolver(r: Resultado<AuthResponse>) {
        when (r) {
            is Resultado.Exito -> {
                // Al guardar la sesión, la app entera se reinicia en la pantalla correcta
                Sesion.guardar(
                    r.datos.token,
                    Sesion.Usuario(r.datos.usuario.id, r.datos.usuario.nombre, r.datos.usuario.email)
                )
                _ui.update { it.copy(cargando = false) }
            }
            is Resultado.Fallo -> _ui.update {
                val campo = r.campo
                if (campo != null && campo in CAMPOS) it.copy(cargando = false, errores = mapOf(campo to r.mensaje))
                else it.copy(cargando = false, errorGeneral = r.mensaje)
            }
        }
    }

    private companion object {
        val CAMPOS = setOf("nombre", "email", "password")
    }
}