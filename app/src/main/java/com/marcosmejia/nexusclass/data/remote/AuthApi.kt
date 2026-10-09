package com.marcosmejia.nexusclass.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun registrar(@Body cuerpo: RegistroRequest): AuthResponse

    @POST("auth/login")
    suspend fun iniciarSesion(@Body cuerpo: LoginRequest): AuthResponse

    @DELETE("auth/me")
    suspend fun eliminarCuenta(): MensajeResponse
}