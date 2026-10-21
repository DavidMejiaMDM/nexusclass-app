package com.marcosmejia.nexusclass.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import com.marcosmejia.nexusclass.notifications.Alertas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

object Sesion {
    data class Usuario(val id: String, val nombre: String, val email: String)

    private const val ARCHIVO = "nexusclass_sesion"
    private const val CLAVE_CIFRADA = "sesion_cifrada"

    private var sp: SharedPreferences? = null
    private var contexto: Context? = null

    @Volatile
    var token: String? = null
        private set

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    @Synchronized
    fun iniciar(ctx: Context) {
        if (sp != null) return
        val app = ctx.applicationContext
        contexto = app
        val p = app.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        sp = p

        // 1) Migración: las versiones anteriores guardaban el token en texto plano.
        //    Se borra el texto plano primero y se vuelve a guardar cifrado (el usuario no pierde la sesión).
        val tokenViejo = p.getString("token", null)
        val idViejo = p.getString("id", null)
        if (tokenViejo != null && idViejo != null) {
            val u = Usuario(idViejo, p.getString("nombre", "") ?: "", p.getString("email", "") ?: "")
            p.edit().clear().commit()
            guardar(tokenViejo, u)
            return
        }

        // 2) Caso normal: leer la sesión cifrada
        val cifrada = p.getString(CLAVE_CIFRADA, null) ?: return
        try {
            val json = JSONObject(Cifrado.descifrar(cifrada))
            token = json.getString("token")
            _usuario.value = Usuario(json.getString("id"), json.optString("nombre"), json.optString("email"))
        } catch (e: Exception) {
            // Clave perdida (copia de seguridad restaurada, cambio de bloqueo del teléfono…) o dato dañado:
            // no se intenta recuperar nada; la persona simplemente inicia sesión de nuevo.
            p.edit().clear().commit()
            Cifrado.borrarClave()
        }
    }

    @Synchronized
    fun guardar(nuevoToken: String, u: Usuario) {
        sp?.let { p ->
            runCatching {
                val json = JSONObject()
                    .put("token", nuevoToken)
                    .put("id", u.id)
                    .put("nombre", u.nombre)
                    .put("email", u.email)
                p.edit().putString(CLAVE_CIFRADA, Cifrado.cifrar(json.toString())).commit()
            }.onFailure {
                // Si no se puede cifrar, NO se guarda en texto plano: la sesión vive solo en memoria
                p.edit().clear().commit()
            }
        }
        token = nuevoToken
        _usuario.value = u
    }

    /**
     * Cierra la sesión y limpia lo que quedó de esta persona en el teléfono:
     *  - la sesión (memoria y disco) y la clave de cifrado
     *  - sus datos locales (alertas ya enviadas)
     *  - las alertas programadas y las notificaciones que siguen en la bandeja
     * Se conservan las preferencias del dispositivo: tema, alertas, anticipación y onboarding.
     */
    @Synchronized
    fun cerrar() {
        token = null
        _usuario.value = null
        sp?.edit()?.clear()?.commit()
        Cifrado.borrarClave()

        Preferencias.limpiarDatosDeUsuario()

        contexto?.let { app ->
            Alertas.cancelar(app)
            NotificationManagerCompat.from(app).cancelAll()
        }
    }

    /**
     * Para el interceptor de red: cierra la sesión solo si el token que dio 401 sigue siendo el actual.
     * Así una respuesta tardía de la sesión anterior no cierra la sesión de quien acaba de entrar.
     */
    @Synchronized
    fun cerrarSiEs(tokenUsado: String) {
        if (token == tokenUsado) cerrar()
    }
}