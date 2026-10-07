package com.marcosmejia.nexusclass.ui.screens.carga

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marcosmejia.nexusclass.data.remote.Resultado
import com.marcosmejia.nexusclass.data.repository.ClaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CargaPdfViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ClaseRepository()
    private var bytes: ByteArray? = null
    private var nombre: String = ""

    sealed interface Fase {
        data object Vacio : Fase
        data class Seleccionado(val nombre: String, val kb: Long) : Fase
        data object Procesando : Fase
        data class Exito(val total: Int, val creadas: Int, val omitidas: Int) : Fase
        data class Error(val mensaje: String, val hayArchivo: Boolean) : Fase
    }

    data class Ui(val fase: Fase = Fase.Vacio, val reemplazar: Boolean = false)

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    fun setReemplazar(v: Boolean) = _ui.update { it.copy(reemplazar = v) }

    fun elegir(uri: Uri) {
        viewModelScope.launch {
            val fase = withContext(Dispatchers.IO) { leer(uri) }
            _ui.update { it.copy(fase = fase) }
        }
    }

    private fun leer(uri: Uri): Fase {
        val cr = getApplication<Application>().contentResolver
        val nombreArchivo = cr.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) c.getString(i) else null
        } ?: "horario.pdf"

        val esPdf = cr.getType(uri) == "application/pdf" || nombreArchivo.endsWith(".pdf", ignoreCase = true)
        if (!esPdf) return Fase.Error("Selecciona un archivo PDF (el horario descargado de Moodle).", false)

        val datos = runCatching { cr.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            ?: return Fase.Error("No pudimos abrir el archivo. Intenta con otro.", false)
        if (datos.size > 5 * 1024 * 1024) return Fase.Error("El archivo supera el máximo de 5 MB.", false)

        bytes = datos
        nombre = nombreArchivo
        return Fase.Seleccionado(nombreArchivo, datos.size / 1024L)
    }

    fun subir() {
        val datos = bytes ?: return
        _ui.update { it.copy(fase = Fase.Procesando) }
        viewModelScope.launch {
            val parte = MultipartBody.Part.createFormData(
                "pdf", nombre, datos.toRequestBody("application/pdf".toMediaType())
            )
            val r = repo.subirPdf(parte, if (_ui.value.reemplazar) true else null)
            _ui.update { s ->
                s.copy(
                    fase = when (r) {
                        is Resultado.Exito -> Fase.Exito(r.datos.total, r.datos.creadas ?: 0, r.datos.omitidas ?: 0)
                        is Resultado.Fallo -> Fase.Error(r.mensaje, true)
                    }
                )
            }
        }
    }
}