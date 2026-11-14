package com.marcosmejia.nexusclass.data.repository

import com.marcosmejia.nexusclass.data.local.CacheLocal
import com.marcosmejia.nexusclass.data.local.HoyLocal
import com.marcosmejia.nexusclass.data.remote.ApiService
import com.marcosmejia.nexusclass.data.remote.ClaseDto
import com.marcosmejia.nexusclass.data.remote.ClaseRequest
import com.marcosmejia.nexusclass.data.remote.CompletarRequest
import com.marcosmejia.nexusclass.data.remote.Red
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.remote.TareaDto
import com.marcosmejia.nexusclass.data.remote.TareaRequest
import com.marcosmejia.nexusclass.data.remote.TodayResponse
import com.marcosmejia.nexusclass.data.remote.llamarApi
import okhttp3.MultipartBody
import java.text.Normalizer
import java.time.Instant

// Regla de este archivo: con internet se usa el servidor y se guarda una copia;
// SIN conexión (Fallo.sinConexion) se responde con la última copia guardada, si existe.

class ClaseRepository(private val api: ApiService = Red.api) {

    suspend fun hoy(simular: String? = null): Resultado<TodayResponse> {
        val r = llamarApi { api.hoy(simular) }
        if (r is Resultado.Exito) { CacheLocal.marcarEnLinea(); return r }
        if (r is Resultado.Fallo && r.sinConexion) {
            val clases = CacheLocal.clases() ?: return r
            val tareas = CacheLocal.tareas()?.datos.orEmpty()
            CacheLocal.marcarSinConexion(clases.guardadoEn)
            return Resultado.Exito(HoyLocal.calcular(clases.datos, tareas))   // se calcula en el teléfono
        }
        return r
    }

    suspend fun listar(dia: String? = null): Resultado<List<ClaseDto>> {
        val r = llamarApi { api.clases(dia) }
        if (r is Resultado.Exito) {
            if (dia == null) CacheLocal.guardarClases(r.datos)
            CacheLocal.marcarEnLinea()
            return r
        }
        if (r is Resultado.Fallo && r.sinConexion) {
            val c = CacheLocal.clases() ?: return r
            CacheLocal.marcarSinConexion(c.guardadoEn)
            return Resultado.Exito(if (dia == null) c.datos else porDia(c.datos, dia))
        }
        return r
    }

    suspend fun obtener(id: String): Resultado<ClaseDto> {
        val r = llamarApi { api.clase(id) }
        if (r is Resultado.Exito) { CacheLocal.marcarEnLinea(); return r }
        if (r is Resultado.Fallo && r.sinConexion) {
            val c = CacheLocal.clases() ?: return r
            val clase = c.datos.firstOrNull { it.id == id } ?: return r
            val tareas = CacheLocal.tareas()?.datos.orEmpty()
                .filter { it.claseId == id }
                .sortedBy { it.fechaEntrega }
            CacheLocal.marcarSinConexion(c.guardadoEn)
            return Resultado.Exito(
                clase.copy(
                    tareas = tareas,
                    totalTareas = tareas.size,
                    tareasPendientes = tareas.count { !it.completada }
                )
            )
        }
        return r
    }

    // Clases de un día (sin acentos ni mayúsculas, como en el servidor)
    private fun porDia(clases: List<ClaseDto>, dia: String): List<ClaseDto> {
        val d = Normalizer.normalize(dia.trim(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").uppercase()
        return clases
            .mapNotNull { c -> c.horarios.orEmpty().firstOrNull { it.diaSemana == d }?.let { c.copy(sesion = it) } }
            .sortedBy { it.sesion?.horaInicio }
    }

    // Escrituras: necesitan conexión
    suspend fun subirPdf(pdf: MultipartBody.Part, reemplazar: Boolean? = null) =
        llamarApi { api.subirHorario(pdf, reemplazar) }
    suspend fun crear(clase: ClaseRequest) = llamarApi { api.crearClase(clase) }
    suspend fun actualizar(id: String, clase: ClaseRequest) = llamarApi { api.actualizarClase(id, clase) }
    suspend fun eliminar(id: String) = llamarApi { api.eliminarClase(id) }
}

class TareaRepository(private val api: ApiService = Red.api) {

    suspend fun listar(
        classId: String? = null,
        estado: String? = null,
        tipo: String? = null,
        urgentes: Boolean? = null
    ): Resultado<List<TareaDto>> {
        val r = llamarApi { api.tareas(classId, estado, tipo, urgentes) }
        if (r is Resultado.Exito) {
            // Solo la lista completa (sin filtros) se guarda como copia
            if (classId == null && estado == null && tipo == null && urgentes == null) CacheLocal.guardarTareas(r.datos)
            CacheLocal.marcarEnLinea()
            return r
        }
        if (r is Resultado.Fallo && r.sinConexion) {
            val c = CacheLocal.tareas() ?: return r
            CacheLocal.marcarSinConexion(c.guardadoEn)
            return Resultado.Exito(filtrar(c.datos, classId, estado, tipo, urgentes))
        }
        return r
    }

    suspend fun obtener(id: String): Resultado<TareaDto> {
        val r = llamarApi { api.tarea(id) }
        if (r is Resultado.Exito) { CacheLocal.marcarEnLinea(); return r }
        if (r is Resultado.Fallo && r.sinConexion) {
            val c = CacheLocal.tareas() ?: return r
            val t = c.datos.firstOrNull { it.id == id } ?: return r
            CacheLocal.marcarSinConexion(c.guardadoEn)
            return Resultado.Exito(conTiempoActual(t, System.currentTimeMillis()))
        }
        return r
    }

    // Los mismos filtros que aplica el servidor
    private fun filtrar(
        todas: List<TareaDto>, classId: String?, estado: String?, tipo: String?, urgentes: Boolean?
    ): List<TareaDto> {
        val ahora = System.currentTimeMillis()
        return todas.map { conTiempoActual(it, ahora) }
            .filter { t ->
                (classId == null || t.claseId == classId) &&
                        (estado != "pendiente" || !t.completada) &&
                        (estado != "completada" || t.completada) &&
                        (tipo == null || t.tipo.equals(tipo, ignoreCase = true)) &&
                        (urgentes != true || t.urgente == true)
            }
            .sortedBy { it.fechaEntrega }
    }

    // "urgente" y "horasRestantes" se calcularon cuando se guardó la copia: se recalculan con la hora de ahora
    private fun conTiempoActual(t: TareaDto, ahoraMs: Long): TareaDto {
        val fin = runCatching { Instant.parse(t.fechaEntrega).toEpochMilli() }.getOrNull() ?: return t
        val ms = fin - ahoraMs
        return t.copy(
            horasRestantes = Math.round(ms / 3_600_000.0 * 10) / 10.0,
            urgente = !t.completada && ms > 0 && ms <= 24 * 3_600_000L
        )
    }

    // Escrituras: necesitan conexión; al terminar bien, la copia local se mantiene al día
    suspend fun crear(tarea: TareaRequest): Resultado<TareaDto> {
        val r = llamarApi { api.crearTarea(tarea) }
        if (r is Resultado.Exito) CacheLocal.actualizarTarea(r.datos)
        return r
    }

    suspend fun actualizar(id: String, tarea: TareaRequest): Resultado<TareaDto> {
        val r = llamarApi { api.actualizarTarea(id, tarea) }
        if (r is Resultado.Exito) CacheLocal.actualizarTarea(r.datos)
        return r
    }

    suspend fun completar(id: String, completada: Boolean = true): Resultado<TareaDto> {
        val r = llamarApi { api.completar(id, CompletarRequest(completada)) }
        if (r is Resultado.Exito) CacheLocal.actualizarTarea(r.datos)
        return r
    }

    suspend fun eliminar(id: String): Resultado<Unit> {
        val r = llamarApi { api.eliminarTarea(id) }
        if (r is Resultado.Exito) CacheLocal.quitarTarea(id)
        return r
    }
}