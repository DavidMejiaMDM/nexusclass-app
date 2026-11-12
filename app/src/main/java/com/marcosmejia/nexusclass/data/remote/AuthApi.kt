package com.marcosmejia.nexusclass.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface AuthApi {

    @POST("auth/forgot-password")
    suspend fun olvidePassword(@Body cuerpo: OlvidePasswordRequest): OlvideResponse

    @POST("auth/reset-password")
    suspend fun restablecerPassword(@Body cuerpo: RestablecerRequest): AuthResponse
    @POST("auth/register")
    suspend fun registrar(@Body cuerpo: RegistroRequest): AuthResponse

    @POST("auth/login")
    suspend fun iniciarSesion(@Body cuerpo: LoginRequest): AuthResponse

    @DELETE("auth/me")
    suspend fun eliminarCuenta(): MensajeResponse

    //cambiar nombre y contraseña

    @PATCH("auth/me")
    suspend fun actualizarPerfil(@Body cuerpo: PerfilRequest): PerfilResponse

    @PUT("auth/password")
    suspend fun cambiarPassword(@Body cuerpo: CambioPasswordRequest): AuthResponse
}