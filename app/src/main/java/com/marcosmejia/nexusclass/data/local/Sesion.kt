package com.marcosmejia.nexusclass.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Sesion {
    data class Usuario(val id: String, val nombre: String, val email: String)

    private var sp: SharedPreferences? = null

    @Volatile
    var token: String? = null
        private set

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    @Synchronized
    fun iniciar(contexto: Context) {
        if (sp != null) return
        val p = contexto.applicationContext.getSharedPreferences("nexusclass_sesion", Context.MODE_PRIVATE)
        sp = p
        val t = p.getString("token", null)
        val id = p.getString("id", null)
        if (t != null && id != null) {
            token = t
            _usuario.value = Usuario(id, p.getString("nombre", "") ?: "", p.getString("email", "") ?: "")
        }
    }

    fun guardar(nuevoToken: String, u: Usuario) {
        sp?.edit()
            ?.putString("token", nuevoToken)
            ?.putString("id", u.id)
            ?.putString("nombre", u.nombre)
            ?.putString("email", u.email)
            ?.apply()
        token = nuevoToken
        _usuario.value = u
    }

    fun cerrar() {
        sp?.edit()?.clear()?.apply()
        token = null
        _usuario.value = null
    }
}