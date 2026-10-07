package com.marcosmejia.nexusclass.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Preferencias {
    private var sp: SharedPreferences? = null

    private val _nombre = MutableStateFlow("")
    private val _tema = MutableStateFlow("SISTEMA")           // SISTEMA | CLARO | OSCURO
    private val _notificaciones = MutableStateFlow(true)
    private val _anticipacion = MutableStateFlow(24)

    val nombre: StateFlow<String> = _nombre.asStateFlow()
    val tema: StateFlow<String> = _tema.asStateFlow()
    val notificaciones: StateFlow<Boolean> = _notificaciones.asStateFlow()
    val anticipacionHoras: StateFlow<Int> = _anticipacion.asStateFlow()

    // Se puede llamar muchas veces: solo la primera hace algo
    fun iniciar(contexto: Context) {
        if (sp != null) return
        val p = contexto.applicationContext.getSharedPreferences("nexusclass", Context.MODE_PRIVATE)
        sp = p
        _nombre.value = p.getString("nombre", "") ?: ""
        _tema.value = p.getString("tema", "SISTEMA") ?: "SISTEMA"
        _notificaciones.value = p.getBoolean("notificaciones", true)
        _anticipacion.value = p.getInt("anticipacion", 24)
    }

    var onboardingVisto: Boolean
        get() = sp?.getBoolean("onboarding", false) ?: false
        set(valor) { sp?.edit()?.putBoolean("onboarding", valor)?.apply() }

    fun setNombre(v: String) { _nombre.value = v; sp?.edit()?.putString("nombre", v)?.apply() }
    fun setTema(v: String) { _tema.value = v; sp?.edit()?.putString("tema", v)?.apply() }
    fun setNotificaciones(v: Boolean) { _notificaciones.value = v; sp?.edit()?.putBoolean("notificaciones", v)?.apply() }
    fun setAnticipacion(v: Int) { _anticipacion.value = v; sp?.edit()?.putInt("anticipacion", v)?.apply() }

    // Alertas ya enviadas (para no repetir la misma notificación)
    fun yaNotificada(clave: String): Boolean =
        sp?.getStringSet("notificadas", emptySet())?.contains(clave) == true

    fun marcarNotificada(clave: String) {
        val actual = sp?.getStringSet("notificadas", emptySet()).orEmpty()
        val nuevo = if (actual.size > 300) setOf(clave) else actual + clave
        sp?.edit()?.putStringSet("notificadas", nuevo)?.apply()
    }
}