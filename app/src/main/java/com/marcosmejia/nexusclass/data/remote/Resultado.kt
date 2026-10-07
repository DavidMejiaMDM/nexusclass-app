package com.marcosmejia.nexusclass.data.remote


import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed interface Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>
    data class Fallo(
        val mensaje: String,
        val campo: String? = null,      // ej. "fechaEntrega" para marcarlo en rojo
        val codigo: String? = null,     // ej. "FECHA_PASADA"
        val sinConexion: Boolean = false
    ) : Resultado<Nothing>
}

suspend fun <T> llamarApi(bloque: suspend () -> T): Resultado<T> =
    try {
        Resultado.Exito(bloque())
    } catch (e: CancellationException) {
        throw e // no tragarse las cancelaciones de corrutinas
    } catch (e: HttpException) {
        // El servidor respondió con error (400, 404...): leemos su mensaje
        val cuerpo = e.response()?.errorBody()?.string()
        val err = runCatching { Gson().fromJson(cuerpo, ErrorApi::class.java) }.getOrNull()
        Resultado.Fallo(
            mensaje = err?.mensaje ?: "Error del servidor (${e.code()})",
            campo = err?.campo,
            codigo = err?.error
        )
    } catch (e: IOException) {
        Resultado.Fallo(
            mensaje = "No pudimos conectar con el servidor. Revisa tu internet e intenta de nuevo.",
            sinConexion = true
        )
    } catch (e: Exception) {
        Resultado.Fallo(e.message ?: "Ocurrió un error inesperado")
    }