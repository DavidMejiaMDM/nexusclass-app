package com.marcosmejia.nexusclass.ui.util



import androidx.compose.ui.graphics.Color
import com.marcosmejia.nexusclass.ui.theme.ColorParcial
import com.marcosmejia.nexusclass.ui.theme.ColorTaller
import com.marcosmejia.nexusclass.ui.theme.ColorTarea
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formato {
    private val zona: ZoneId = ZoneId.of("America/Bogota")
    private val es: Locale = Locale.forLanguageTag("es-CO")
    private val fmtHora = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    // "14:00" -> "2:00 PM"
    fun hora12(hhmm: String): String =
        runCatching { LocalTime.parse(hhmm).format(fmtHora) }.getOrDefault(hhmm)

    fun rango(inicio: String, fin: String) = "${hora12(inicio)} - ${hora12(fin)}"

    // 35 -> "35 min", 120 -> "2 h", 90 -> "1 h 30 min"
    fun duracion(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> "${minutos / 60} h"
        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min"
        else -> "$minutos min"
    }

    // "2026-10-05T08:30:00.000-05:00" -> "Lunes, 5 de octubre"
    fun fechaLarga(isoConOffset: String): String = runCatching {
        OffsetDateTime.parse(isoConOffset)
            .format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", es))
            .replaceFirstChar { it.uppercase() }
    }.getOrDefault("")

    // "Mar 6 de octubre de 2026, 8:00 AM" (la fecha de entrega llega en UTC)
    fun fechaCompleta(iso: String): String = runCatching {
        val f = Instant.parse(iso).atZone(zona)
        val dia = f.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", es))
            .replaceFirstChar { it.uppercase() }
        "$dia, ${f.format(fmtHora)}"
    }.getOrDefault(iso)

    // Para el selector de fecha del formulario: "Mar 6 oct 2026"
    fun fechaCorta(f: LocalDate): String =
        f.format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", es)).replaceFirstChar { it.uppercase() }

    // "Hoy 8:00 AM", "Mañana 8:00 AM", "Mar 6 oct, 8:00 AM"
    fun fechaTarea(iso: String): String = runCatching {
        val f = Instant.parse(iso).atZone(zona)
        val hoy = LocalDate.now(zona)
        val hora = f.format(fmtHora)
        when (f.toLocalDate()) {
            hoy -> "Hoy $hora"
            hoy.plusDays(1) -> "Mañana $hora"
            else -> f.format(DateTimeFormatter.ofPattern("EEE d MMM", es))
                .replaceFirstChar { it.uppercase() } + ", $hora"
        }
    }.getOrDefault(iso)

    // Minutos que faltan para la fecha (negativo si ya pasó)
    fun minutosHasta(iso: String): Long? = runCatching {
        Duration.between(Instant.now(), Instant.parse(iso)).toMinutes()
    }.getOrNull()

    // "Quedan 45 min", "Quedan 18 h", "Quedan 5 días", "Vencida"
    fun restante(iso: String): String? {
        val min = minutosHasta(iso) ?: return null
        return when {
            min < 0 -> "Vencida"
            min < 60 -> "Quedan $min min"
            min < 48 * 60 -> "Quedan ${min / 60} h"
            else -> "Quedan ${min / (24 * 60)} días"
        }
    }

    // Algunas materias no traen salón (ej. Herramientas para el Pensamiento)
    fun ubicacion(salon: String?, edificio: String?): String {
        if (salon.isNullOrBlank()) return "Salón por confirmar"
        return if (edificio.isNullOrBlank()) "Salón $salon" else "Salón $salon · $edificio"
    }

    fun colorHex(hex: String?, defecto: Color): Color =
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(defecto)

    fun colorTipo(tipo: String): Color = when (tipo.uppercase()) {
        "PARCIAL" -> ColorParcial
        "TALLER" -> ColorTaller
        else -> ColorTarea
    }
}