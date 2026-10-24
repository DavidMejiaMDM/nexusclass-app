package com.marcosmejia.nexusclass.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.marcosmejia.nexusclass.MainActivity
import com.marcosmejia.nexusclass.data.local.Preferencias

object Notificador {
    private const val PREFIJO_CANAL = "entregas"

    // En Android 8+ el sonido y la vibración pertenecen al CANAL y no se pueden cambiar una vez creado.
    // Por eso cada combinación de sonido + vibración usa su propio canal: el id lleva una "huella".
    private fun idCanal(): String {
        val huella = Integer.toHexString("${Preferencias.sonido.value}|${Preferencias.vibracion.value}".hashCode())
        return "${PREFIJO_CANAL}_$huella"
    }

    fun crearCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = contexto.getSystemService(NotificationManager::class.java)
        val id = idCanal()

        // Quita los canales de configuraciones anteriores (incluido el canal "entregas" de versiones viejas)
        manager.notificationChannels
            .filter { it.id.startsWith(PREFIJO_CANAL) && it.id != id }
            .forEach { manager.deleteNotificationChannel(it.id) }

        if (manager.getNotificationChannel(id) != null) return

        val atributos = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val canal = NotificationChannel(id, "Alertas de entregas", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Avisos de tareas y parciales próximos a vencer"
            enableVibration(Preferencias.vibracion.value)
            when (val sonido = Preferencias.sonido.value) {
                Preferencias.SONIDO_PREDETERMINADO -> setSound(Settings.System.DEFAULT_NOTIFICATION_URI, atributos)
                Preferencias.SONIDO_SILENCIO -> setSound(null, null)
                else -> setSound(Uri.parse(sonido), atributos)
            }
        }
        manager.createNotificationChannel(canal)
    }

    // Nombre legible del sonido elegido, para mostrarlo en Ajustes
    fun nombreDelSonido(contexto: Context, sonido: String): String = when (sonido) {
        Preferencias.SONIDO_PREDETERMINADO -> "Predeterminado del sistema"
        Preferencias.SONIDO_SILENCIO -> "Silencio"
        else -> runCatching {
            RingtoneManager.getRingtone(contexto, Uri.parse(sonido))?.getTitle(contexto)
        }.getOrNull() ?: "Personalizado"
    }

    @SuppressLint("MissingPermission")
    fun enviar(contexto: Context, id: Int, titulo: String, texto: String) {
        val manager = NotificationManagerCompat.from(contexto)
        if (!manager.areNotificationsEnabled()) return
        crearCanal(contexto)

        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val abrir = PendingIntent.getActivity(
            contexto, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(contexto, idCanal())
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notificacion)
    }
}