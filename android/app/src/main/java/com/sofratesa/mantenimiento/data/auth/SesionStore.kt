package com.sofratesa.mantenimiento.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * El token se cifra con una clave que vive en el Android Keystore
 * (nunca sale del hardware/StrongBox del dispositivo). No se guarda
 * texto plano en ningún archivo.
 */
class SesionStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "sesion_segura",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun guardarSesion(token: String, usuario: String, expiraEn: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USUARIO, usuario)
            .putString(KEY_EXPIRA_EN, expiraEn)
            .apply()
    }

    fun token(): String? = prefs.getString(KEY_TOKEN, null)

    fun usuario(): String? = prefs.getString(KEY_USUARIO, null)

    fun haySesion(): Boolean = token() != null

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USUARIO = "usuario"
        private const val KEY_EXPIRA_EN = "expira_en"
    }
}
