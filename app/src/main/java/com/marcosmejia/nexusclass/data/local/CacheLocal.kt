package com.marcosmejia.nexusclass.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.TareaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Type

/**
 * Este archivo contendra una Copia local de lo último que mostró el servidor para poder verlo sin internet.
 * Cada copia pertenece a una cuenta.
 * Es solo de lectura: sin conexión no se puede crear ni editar nada.
 */
object CacheLocal {
    class Guardado<T>(val datos: T, val guardadoEn: Long)

    private val gson = Gson()
    private var sp: SharedPreferences? = null

    // No es null mientras la app esté mostrando datos guardados (sin conexión): lo usa el aviso amarillo
    private val _desde = MutableStateFlow<Long?>(null)
    val datosGuardadosDesde: StateFlow<Long?> = _desde.asStateFlow()

    private val tipoClases: Type = object : TypeToken<List<ClaseDto>>() {}.type
    private val tipoTareas: Type = object : TypeToken<List<TareaDto>>() {}.type

    @Synchronized
    fun iniciar(contexto: Context) {
        if (sp == null) {
            sp = contexto.applicationContext.getSharedPreferences("nexusclass_cache", Context.MODE_PRIVATE)
        }
    }

    private fun clave(nombre: String): String? {
        val id = Sesion.usuario.value?.id ?: return null
        return "$id|$nombre"
    }

    private fun guardar(nombre: String, datos: Any, conservarMarca: Boolean = false) {
        val p = sp ?: return
        val k = clave(nombre) ?: return
        val editor = p.edit().putString(k, gson.toJson(datos))
        if (!conservarMarca) editor.putLong("$k|ts", System.currentTimeMillis())
        editor.apply()
    }

    private fun <T> leer(nombre: String, tipo: Type): Guardado<T>? {
        val p = sp ?: return null
        val k = clave(nombre) ?: return null
        val json = p.getString(k, null) ?: return null
        val datos: T = runCatching { gson.fromJson<T>(json, tipo) }.getOrNull() ?: return null
        return Guardado(datos, p.getLong("$k|ts", 0L))
    }

    fun guardarClases(lista: List<ClaseDto>) = guardar("clases", lista)
    fun clases(): Guardado<List<ClaseDto>>? = leer("clases", tipoClases)

    fun guardarTareas(lista: List<TareaDto>) = guardar("tareas", lista)
    fun tareas(): Guardado<List<TareaDto>>? = leer("tareas", tipoTareas)

    // Mantienen la copia al día cuando la persona crea, edita, completa o elimina una tarea con conexión
    fun actualizarTarea(t: TareaDto) {
        val actual = tareas() ?: return   // si aún no hay copia, no se crea una a medias
        val nueva = if (actual.datos.any { it.id == t.id }) actual.datos.map { if (it.id == t.id) t else it }
        else actual.datos + t
        guardar("tareas", nueva, conservarMarca = true)
    }

    fun quitarTarea(id: String) {
        val actual = tareas() ?: return
        guardar("tareas", actual.datos.filter { it.id != id }, conservarMarca = true)
    }

    fun marcarEnLinea() { _desde.value = null }
    fun marcarSinConexion(guardadoEn: Long) { _desde.value = guardadoEn }

    // Se llama al cerrar sesión (también al eliminar la cuenta o cuando el servidor rechaza la sesión)
    fun limpiar() {
        sp?.edit()?.clear()?.commit()
        _desde.value = null
    }
}