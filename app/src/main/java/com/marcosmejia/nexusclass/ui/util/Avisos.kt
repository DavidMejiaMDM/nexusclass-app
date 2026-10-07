package com.marcosmejia.nexusclass.ui.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object Avisos {
    private val _flujo = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val flujo: SharedFlow<String> = _flujo.asSharedFlow()

    fun mostrar(texto: String) {
        _flujo.tryEmit(texto)
    }
}