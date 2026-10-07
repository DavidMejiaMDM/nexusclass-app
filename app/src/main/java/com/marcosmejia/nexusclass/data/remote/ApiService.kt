package com.marcosmejia.nexusclass.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ----- Clases -----
    @GET("clases")
    suspend fun clases(@Query("dia") dia: String? = null): List<ClaseDto>

    @GET("clases/today")
    suspend fun hoy(@Query("simular") simular: String? = null): TodayResponse

    @GET("clases/{id}")
    suspend fun clase(@Path("id") id: String): ClaseDto

    @Multipart
    @POST("clases/upload")
    suspend fun subirHorario(
        @Part pdf: MultipartBody.Part,
        @Query("reemplazar") reemplazar: Boolean? = null
    ): UploadResponse

    @POST("clases")
    suspend fun crearClase(@Body clase: ClaseRequest): ClaseDto

    @PUT("clases/{id}")
    suspend fun actualizarClase(@Path("id") id: String, @Body clase: ClaseRequest): ClaseDto

    @DELETE("clases/{id}")
    suspend fun eliminarClase(@Path("id") id: String): EliminarClaseResponse

    // ----- Tareas -----
    @GET("tasks")
    suspend fun tareas(
        @Query("classId") classId: String? = null,
        @Query("estado") estado: String? = null,
        @Query("tipo") tipo: String? = null,
        @Query("urgentes") urgentes: Boolean? = null
    ): List<TareaDto>

    @GET("tasks/{id}")
    suspend fun tarea(@Path("id") id: String): TareaDto

    @POST("tasks")
    suspend fun crearTarea(@Body tarea: TareaRequest): TareaDto

    @PUT("tasks/{id}")
    suspend fun actualizarTarea(@Path("id") id: String, @Body tarea: TareaRequest): TareaDto

    @PATCH("tasks/{id}/completar")
    suspend fun completar(@Path("id") id: String, @Body cuerpo: CompletarRequest): TareaDto

    @DELETE("tasks/{id}")
    suspend fun eliminarTarea(@Path("id") id: String)
}