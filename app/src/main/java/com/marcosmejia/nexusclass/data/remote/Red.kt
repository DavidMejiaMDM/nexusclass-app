package com.marcosmejia.nexusclass.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Red {

    const val BASE_URL = "https://organizador-api-g3vc.onrender.com/api/v1/"

    // Para la API local del PC (emulador): "http://10.0.2.2:3000/api/v1/"

    private val cliente = OkHttpClient.Builder()
        // 60 s: Render gratis tarda en "despertar" tras 15 min sin uso
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.NONE })
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}