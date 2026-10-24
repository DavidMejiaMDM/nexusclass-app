package com.marcosmejia.nexusclass.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.local.Sesion
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.AuthRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel : ViewModel() {

    private val repo = AuthRepository()

    data class Ui(
        // Nombre
        val guardandoNombre: Boolean = false,
        val errorNombre: String? = null,
        // Contraseña
        val cambiandoClave: Boolean = false,
        val erroresClave: Map<String, String> = emptyMap(),   // campo -> mensaje
        val errorGeneralClave: String? = null
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    fun limpiarErrorNombre() = _ui.update { it.copy(errorNombre = null) }

    fun quitarErrorClave(campo: String) =
        _ui.update { it.copy(erroresClave = it.erroresClave - campo, errorGeneralClave = null) }

    // ---------- Nombre ----------
    fun guardarNombre(texto: String) {
        if (_ui.value.guardandoNombre) return
        val nombre = texto.trim().replace(Regex("\\s+"), " ")
        val actual = Sesion.usuario.value ?: return

        if (nombre.length < 2) {
            _ui.update { it.copy(errorNombre = "Escribe tu nombre") }
            return
        }
        if (nombre.length > 60) {
            _ui.update { it.copy(errorNombre = "Máximo 60 caracteres") }
            return
        }
        if (nombre == actual.nombre) {
            Avisos.mostrar("No hay cambios que guardar")
            return
        }

        _ui.update { it.copy(guardandoNombre = true, errorNombre = null) }
        viewModelScope.launch {
            when (val r = repo.actualizarNombre(nombre)) {
                is Resultado.Exito -> {
                    val u = r.datos.usuario
                    Sesion.actualizarUsuario(Sesion.Usuario(u.id, u.nombre, u.email))
                    Avisos.mostrar("Nombre actualizado")
                    _ui.update { it.copy(guardandoNombre = false) }
                }
                is Resultado.Fallo -> _ui.update { it.copy(guardandoNombre = false, errorNombre = r.mensaje) }
            }
        }
    }

    // ---------- Contraseña ----------
    fun cambiarClave(actual: String, nueva: String, confirmar: String, alExito: () -> Unit) {
        if (_ui.value.cambiandoClave) return

        val errores = buildMap {
            if (actual.isEmpty()) put("passwordActual", "Escribe tu contraseña actual")
            if (nueva.length < 8) put("passwordNueva", "Debe tener al menos 8 caracteres")
            else if (nueva == actual) put("passwordNueva", "Debe ser distinta a la actual")
            if (confirmar != nueva) put("confirmar", "Las contraseñas no coinciden")
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(erroresClave = errores, errorGeneralClave = null) }
            return
        }

        _ui.update { it.copy(cambiandoClave = true, erroresClave = emptyMap(), errorGeneralClave = null) }
        viewModelScope.launch {
            when (val r = repo.cambiarPassword(actual, nueva)) {
                is Resultado.Exito -> {
                    val u = r.datos.usuario
                    // El servidor cierra las demás sesiones y entrega un token nuevo para este teléfono
                    Sesion.guardar(r.datos.token, Sesion.Usuario(u.id, u.nombre, u.email))
                    Avisos.mostrar("Contraseña actualizada. Se cerraron tus otras sesiones.")
                    _ui.update { it.copy(cambiandoClave = false) }
                    alExito()
                }
                is Resultado.Fallo -> _ui.update {
                    val campo = r.campo
                    if (campo != null && campo in CAMPOS_CLAVE) {
                        it.copy(cambiandoClave = false, erroresClave = mapOf(campo to r.mensaje))
                    } else {
                        it.copy(cambiandoClave = false, errorGeneralClave = r.mensaje)
                    }
                }
            }
        }
    }

    private companion object {
        val CAMPOS_CLAVE = setOf("passwordActual", "passwordNueva")
    }
}