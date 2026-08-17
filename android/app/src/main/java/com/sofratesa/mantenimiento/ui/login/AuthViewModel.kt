package com.sofratesa.mantenimiento.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofratesa.mantenimiento.data.auth.SesionStore
import com.sofratesa.mantenimiento.data.remote.ApiService
import com.sofratesa.mantenimiento.data.remote.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface EstadoLogin {
    data object Inactivo : EstadoLogin
    data object Cargando : EstadoLogin
    data class Error(val mensaje: String) : EstadoLogin
    data object Exitoso : EstadoLogin
}

class AuthViewModel(
    private val apiService: ApiService,
    private val sesionStore: SesionStore
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoLogin>(EstadoLogin.Inactivo)
    val estado: StateFlow<EstadoLogin> = _estado

    fun login(usuario: String, password: String) {
        if (usuario.isBlank() || password.isBlank()) {
            _estado.value = EstadoLogin.Error("Usuario y contraseña son obligatorios")
            return
        }
        viewModelScope.launch {
            _estado.value = EstadoLogin.Cargando
            _estado.value = try {
                val respuesta = apiService.login(LoginRequest(usuario, password))
                val cuerpo = respuesta.body()
                if (respuesta.isSuccessful && cuerpo != null) {
                    sesionStore.guardarSesion(cuerpo.token, usuario, cuerpo.expira_en)
                    EstadoLogin.Exitoso
                } else {
                    EstadoLogin.Error("Usuario o contraseña incorrectos")
                }
            } catch (e: IOException) {
                EstadoLogin.Error("Sin conexión con el servidor. Se requiere conectividad para iniciar sesión.")
            } catch (e: Exception) {
                EstadoLogin.Error("Error inesperado al iniciar sesión")
            }
        }
    }
}
