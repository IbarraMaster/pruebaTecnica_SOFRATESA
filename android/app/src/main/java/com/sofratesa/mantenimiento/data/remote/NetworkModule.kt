package com.sofratesa.mantenimiento.data.remote

import com.sofratesa.mantenimiento.BuildConfig
import com.sofratesa.mantenimiento.data.auth.SesionStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    fun crearApiService(sesionStore: SesionStore): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            // BASIC (método + URL + código + duración) y nunca HEADERS/BODY:
            // el header Authorization y el password del login no deben
            // terminar en el log del dispositivo.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sesionStore))
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
