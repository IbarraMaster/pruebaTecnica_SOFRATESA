package com.sofratesa.mantenimiento

import android.app.Application
import com.sofratesa.mantenimiento.data.auth.SesionStore
import com.sofratesa.mantenimiento.data.local.AppDatabase
import com.sofratesa.mantenimiento.data.net.ConnectivityObserver
import com.sofratesa.mantenimiento.data.remote.ApiService
import com.sofratesa.mantenimiento.data.remote.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/** Contenedor manual de dependencias: sin librería de DI, para mantenerlo simple y explicable. */
class MantenimientoApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob())

    val database: AppDatabase by lazy { AppDatabase.obtener(this) }
    val sesionStore: SesionStore by lazy { SesionStore(this) }
    val apiService: ApiService by lazy { NetworkModule.crearApiService(sesionStore) }
    val connectivityObserver: ConnectivityObserver by lazy { ConnectivityObserver(this, appScope) }
}
