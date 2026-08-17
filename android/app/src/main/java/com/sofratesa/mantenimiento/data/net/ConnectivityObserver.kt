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
 * Expone conectividad "de verdad" (con salida a internet validada), no solo
 * "hay una interfaz de red". Usado para el indicador visible y para disparar
 * la sincronización automática al recuperar señal.
 */
class ConnectivityObserver(context: Context, scope: CoroutineScope) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val flujoBruto: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val validado = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(validado)
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
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
