package com.sofratesa.mantenimiento.data.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted

/**
 * Expone si hay una interfaz de red activa con capacidad de salir a internet.
 * No exige NET_CAPABILITY_VALIDATED (esa bandera depende de que Android logre
 * hacer ping a servidores de Google): en un entorno 100% local como este,
 * contra un backend propio en Docker, esa validación puede no completarse
 * nunca aunque la red hacia el backend funcione perfectamente.
 */
class ConnectivityObserver(context: Context, scope: CoroutineScope) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val flujoBruto: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        trySend(hayConexionAhoraMismo())

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    val estaOnline = flujoBruto
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, hayConexionAhoraMismo())

    private fun hayConexionAhoraMismo(): Boolean {
        val red = connectivityManager.activeNetwork ?: return false
        val capacidades = connectivityManager.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
