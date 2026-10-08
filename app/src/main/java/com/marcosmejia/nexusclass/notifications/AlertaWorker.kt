package com.marcosmejia.nexusclass.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marcosmejia.nexusclass.data.local.Preferencias
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.TareaRepository
import com.marcosmejia.nexusclass.ui.util.Avisos
import com.marcosmejia.nexusclass.ui.util.Formato

class AlertaWorker(contexto: Context, params: WorkerParameters) : CoroutineWorker(contexto, params) {

    override suspend fun doWork(): Result {
        Preferencias.iniciar(applicationContext)
        if (!Preferencias.notificaciones.value) return Result.success()

        val forzar = inputData.getBoolean("forzar", false)   // true = botón «Revisar ahora»
        Notificador.crearCanal(applicationContext)

        return when (val r = TareaRepository().listar(estado = "pendiente")) {
            is Resultado.Fallo -> if (r.sinConexion) Result.retry() else Result.success()
            is Resultado.Exito -> {
                val limiteMin = Preferencias.anticipacionHoras.value * 60L
                var enviadas = 0
                r.datos.forEach { t ->
                    val min = Formato.minutosHasta(t.fechaEntrega) ?: return@forEach
                    if (min < 0 || min > limiteMin) return@forEach

                    val clave = "${t.id}@${t.fechaEntrega}"
                    if (!forzar && Preferencias.yaNotificada(clave)) return@forEach

                    val tipo = t.tipo.lowercase().replaceFirstChar { it.uppercase() }
                    Notificador.enviar(
                        applicationContext,
                        t.id.hashCode(),
                        "$tipo por vencer",
                        "${t.titulo} (${t.clase?.nombreMateria ?: "sin asignatura"}) · ${Formato.restante(t.fechaEntrega)}"
                    )
                    Preferencias.marcarNotificada(clave)
                    enviadas++
                }
                if (forzar && enviadas == 0) {
                    Avisos.mostrar("No tienes entregas en las próximas ${Preferencias.anticipacionHoras.value} horas")
                }
                Result.success()
            }
        }
    }
}