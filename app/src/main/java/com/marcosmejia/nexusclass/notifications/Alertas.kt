package com.marcosmejia.nexusclass.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object Alertas {
    private const val NOMBRE = "alertas_entregas"

    private val conRed = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    // Revisa cada hora (el mínimo que permite Android es 15 min)
    fun programar(contexto: Context) {
        val peticion = PeriodicWorkRequestBuilder<AlertaWorker>(1, TimeUnit.HOURS)
            .setConstraints(conRed)
            .build()
        WorkManager.getInstance(contexto)
            .enqueueUniquePeriodicWork(NOMBRE, ExistingPeriodicWorkPolicy.UPDATE, peticion)
    }

    fun cancelar(contexto: Context) {
        WorkManager.getInstance(contexto).cancelUniqueWork(NOMBRE)
    }

    // Para probar o mostrar en la sustentación sin esperar una hora
    fun revisarAhora(contexto: Context) {
        val peticion = OneTimeWorkRequestBuilder<AlertaWorker>()
            .setConstraints(conRed)
            .setInputData(workDataOf("forzar" to true))
            .build()
        WorkManager.getInstance(contexto).enqueue(peticion)
    }
}