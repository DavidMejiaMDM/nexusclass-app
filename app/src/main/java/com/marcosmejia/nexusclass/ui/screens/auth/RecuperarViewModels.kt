package com.marcosmejia.nexusclass.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.local.Sesion
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.AuthRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Paso 1: la persona escribe su correo y pide el código. */
class RecuperarViewModel : ViewModel() {

    private val repo = AuthRepository()

    data class Ui(
        val enviando: Boolean = false,
        val errorEmail: String? = null,
        val errorGeneral: String? = null
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    fun quitarError() = _ui.update { it.copy(errorEmail = null, errorGeneral = null) }

    fun enviar(email: String, alExito: (String) -> Unit) {
        if (_ui.value.enviando) return
        val correo = email.trim().lowercase()

        // Solo se revisa la forma del correo: el servidor decide si existe y responde igual en ambos casos
        if (!correo.contains("@") || !correo.substringAfter("@").contains(".")) {
            _ui.update { it.copy(errorEmail = "Escribe un correo válido") }
            return
        }

        _ui.update { Ui(enviando = true) }
        viewModelScope.launch {
            when (val r = repo.olvidePassword(correo)) {
                is Resultado.Exito -> {
                    _ui.update { it.copy(enviando = false) }
                    alExito(correo)
                }
                is Resultado.Fallo -> _ui.update {
                    if (r.campo == "email") it.copy(enviando = false, errorEmail = r.mensaje)
                    else it.copy(enviando = false, errorGeneral = r.mensaje)   // incluye "demasiadas solicitudes" (429)
                }
            }
        }
    }
}

/** Paso 2: código recibido + contraseña nueva. Si todo sale bien, la sesión queda iniciada. */
class RestablecerViewModel : ViewModel() {

    private val repo = AuthRepository()

    data class Ui(
        val cargando: Boolean = false,
        val errores: Map<String, String> = emptyMap(),   // campo -> mensaje
        val errorGeneral: String? = null,
        val segundos: Int = 60,                           // cuenta regresiva para poder reenviar el código
        val reenviando: Boolean = false
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    private var cuenta: Job? = null

    init { iniciarCuenta(60) }   // el código acaba de enviarse en el paso anterior

    private fun iniciarCuenta(desde: Int) {
        cuenta?.cancel()
        cuenta = viewModelScope.launch {
            for (s in desde downTo 1) {
                _ui.update { it.copy(segundos = s) }
                delay(1000)
            }
            _ui.update { it.copy(segundos = 0) }
        }
    }

    fun quitarError(campo: String) =
        _ui.update { it.copy(errores = it.errores - campo, errorGeneral = null) }

    fun reenviar(email: String) {
        val s = _ui.value
        if (s.segundos > 0 || s.reenviando) return

        _ui.update { it.copy(reenviando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = repo.olvidePassword(email)) {
                is Resultado.Exito -> {
                    Avisos.mostrar("Te enviamos un código nuevo")
                    _ui.update { it.copy(reenviando = false) }
                    iniciarCuenta(r.datos.reenviarEnSegundos)
                }
                is Resultado.Fallo -> _ui.update { it.copy(reenviando = false, errorGeneral = r.mensaje) }
            }
        }
    }

    fun restablecer(email: String, codigo: String, nueva: String, confirmar: String) {
        if (_ui.value.cargando) return

        val errores = buildMap {
            if (codigo.length != 6) put("codigo", "Escribe el código de 6 dígitos")
            if (nueva.length < 8) put("passwordNueva", "Debe tener al menos 8 caracteres")
            if (confirmar != nueva) put("confirmar", "Las contraseñas no coinciden")
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }

        _ui.update { it.copy(cargando = true, errores = emptyMap(), errorGeneral = null) }
        viewModelScope.launch {
            when (val r = repo.restablecerPassword(email, codigo, nueva)) {
                is Resultado.Exito -> {
                    val u = r.datos.usuario
                    Avisos.mostrar("Contraseña restablecida. ¡Bienvenido de nuevo!")
                    _ui.update { it.copy(cargando = false) }
                    // Al guardar la sesión, la app entera se reinicia en la pantalla correcta
                    Sesion.guardar(r.datos.token, Sesion.Usuario(u.id, u.nombre, u.email))
                }
                is Resultado.Fallo -> _ui.update {
                    val campo = r.campo
                    if (campo != null && campo in CAMPOS) {
                        it.copy(cargando = false, errores = mapOf(campo to r.mensaje))
                    } else {
                        it.copy(cargando = false, errorGeneral = r.mensaje)
                    }
                }
            }
        }
    }

    private companion object {
        val CAMPOS = setOf("codigo", "passwordNueva")
    }
}