package com.sofratesa.mantenimiento.data.remote

data class LoginRequest(val usuario: String, val password: String)

data class LoginResponse(val token: String, val expira_en: String)

data class RegistroRequest(
    val id_registro: String,
    val codigo_activo: String,
    val tipo_actividad: String,
    val observacion: String,
    val capturado_en: String
)

data class MensajeResponse(val mensaje: String? = null, val error: String? = null)
