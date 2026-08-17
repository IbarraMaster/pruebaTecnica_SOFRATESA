package com.sofratesa.mantenimiento.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sofratesa.mantenimiento.MantenimientoApplication

@Composable
fun LoginScreen(
    app: MantenimientoApplication,
    online: Boolean,
    onLoginExitoso: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AuthViewModel(app.apiService, app.sesionStore) }
        }
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(estado) {
        if (estado is EstadoLogin.Exitoso) onLoginExitoso()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mantenimiento", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (online) "Conectado" else "Sin conexión — se requiere conectividad para iniciar sesión",
            style = MaterialTheme.typography.bodySmall,
            color = if (online) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        if (estado is EstadoLogin.Error) {
            Text(
                (estado as EstadoLogin.Error).mensaje,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.login(usuario.trim(), password) },
            enabled = online && estado != EstadoLogin.Cargando,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            if (estado == EstadoLogin.Cargando) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text("Ingresar")
        }
    }
}
