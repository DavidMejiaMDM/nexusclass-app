package com.marcosmejia.nexusclass.ui.util

import java.time.LocalDate
import java.time.ZoneId

object Dias {
    val CODIGOS = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")

    fun nombre(codigo: String): String = when (codigo) {
        "MIERCOLES" -> "Miércoles"
        "SABADO" -> "Sábado"
        else -> codigo.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun corto(codigo: String): String = nombre(codigo).take(3)          // Lun, Mar, Mié...

    fun inicial(codigo: String): String = if (codigo == "MIERCOLES") "X" else codigo.take(1)   // L M X J V S D

    fun hoy(): String = CODIGOS[LocalDate.now(ZoneId.of("America/Bogota")).dayOfWeek.value - 1]
}