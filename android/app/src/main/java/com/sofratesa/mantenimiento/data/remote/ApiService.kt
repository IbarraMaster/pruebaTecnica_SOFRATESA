package com.sofratesa.mantenimiento.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("registros")
    suspend fun crearRegistro(@Body body: RegistroRequest): Response<MensajeResponse>
}
