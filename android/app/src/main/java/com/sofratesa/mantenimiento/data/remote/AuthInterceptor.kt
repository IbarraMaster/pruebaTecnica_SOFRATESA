package com.sofratesa.mantenimiento.data.remote

import com.sofratesa.mantenimiento.data.auth.SesionStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sesionStore: SesionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sesionStore.token() ?: return chain.proceed(original)
        val autenticado = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(autenticado)
    }
}
