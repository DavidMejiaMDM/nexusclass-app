package com.marcosmejia.nexusclass.data.remote

import com.marcosmejia.nexusclass.data.local.Sesion
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Red {

    const val BASE_URL = "https://organizador-api-g3vc.onrender.com/api/v1/"

    // Agrega "Authorization: Bearer <token>" a cada petición
    private val autenticacion = Interceptor { chain ->
        val original = chain.request()
        val token = Sesion.token
        val peticion = if (token != null) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else original

        val respuesta = chain.proceed(peticion)

        // 401 con sesión abierta = token vencido o cuenta eliminada -> volver al login
        if (respuesta.code == 401 && token != null && !original.url.encodedPath.endsWith("/auth/login")) {
            Sesion.cerrar()
        }
        respuesta
    }

    private val registro = HttpLoggingInterceptor().apply {
        // BASIC no imprime los cuerpos, así las contraseñas no quedan en el Logcat.
        // Para depurar un problema, cámbialo temporalmente a Level.BODY.
        level = HttpLoggingInterceptor.Level.BASIC
        redactHeader("Authorization")
    }

    private val cliente = OkHttpClient.Builder()
        // 60 s: Render gratis tarda en "despertar" tras 15 min sin uso
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(autenticacion)
        .addInterceptor(registro)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
    val auth: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
}