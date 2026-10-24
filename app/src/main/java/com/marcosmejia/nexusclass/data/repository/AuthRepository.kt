package com.marcosmejia.nexusclass.data.repository

import com.marcosmejia.nexusclass.data.remote.AuthApi
import com.marcosmejia.nexusclass.data.remote.LoginRequest
import com.marcosmejia.nexusclass.data.remote.Red
import com.marcosmejia.nexusclass.data.remote.RegistroRequest
import com.marcosmejia.nexusclass.data.remote.llamarApi

import com.marcosmejia.nexusclass.data.remote.PerfilRequest
import com.marcosmejia.nexusclass.data.remote.CambioPasswordRequest

class AuthRepository(private val api: AuthApi = Red.auth) {
    suspend fun registrar(nombre: String, email: String, password: String) =
        llamarApi { api.registrar(RegistroRequest(nombre, email, password)) }

    suspend fun entrar(email: String, password: String) =
        llamarApi { api.iniciarSesion(LoginRequest(email, password)) }

    suspend fun eliminarCuenta() = llamarApi { api.eliminarCuenta() }

    //cambio de nombre y constraseña

    suspend fun actualizarNombre(nombre: String) = llamarApi { api.actualizarPerfil(PerfilRequest(nombre)) }

    suspend fun cambiarPassword(actual: String, nueva: String) =
        llamarApi { api.cambiarPassword(CambioPasswordRequest(actual, nueva)) }
}