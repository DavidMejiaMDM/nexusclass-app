package com.marcosmejia.nexusclass.data.repository

import com.marcosmejia.nexusclass.data.remote.AuthApi
import com.marcosmejia.nexusclass.data.remote.LoginRequest
import com.marcosmejia.nexusclass.data.remote.Red
import com.marcosmejia.nexusclass.data.remote.RegistroRequest
import com.marcosmejia.nexusclass.data.remote.llamarApi

class AuthRepository(private val api: AuthApi = Red.auth) {
    suspend fun registrar(nombre: String, email: String, password: String) =
        llamarApi { api.registrar(RegistroRequest(nombre, email, password)) }

    suspend fun entrar(email: String, password: String) =
        llamarApi { api.iniciarSesion(LoginRequest(email, password)) }

    suspend fun eliminarCuenta() = llamarApi { api.eliminarCuenta() }
}