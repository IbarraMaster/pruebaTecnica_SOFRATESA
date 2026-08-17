package com.sofratesa.mantenimiento.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import com.sofratesa.mantenimiento.MantenimientoApplication
import com.sofratesa.mantenimiento.data.local.EstadoRegistro
import com.sofratesa.mantenimiento.sync.SyncScheduler

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PrincipalScreen(
    app: MantenimientoApplication,
    online: Boolean,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: RegistrosViewModel = viewModel(
        factory = viewModelFactory {
            initializer { RegistrosViewModel(app.database.registroDao()) }
        }
    )
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val errorValidacion by viewModel.errorValidacion.collectAsStateWithLifecycle()
    val workInfo by SyncScheduler.observarUltimoResultado(context).collectAsStateWithLifecycle(initialValue = null)

    var codigoActivo by remember { mutableStateOf("") }
    var tipoActividad by remember { mutableStateOf(RegistrosViewModel.TIPOS_ACTIVIDAD.first()) }
    var observacion by remember { mutableStateOf("") }

    // Dispara sync automático al abrir la pantalla con conectividad, y cada
    // vez que `online` pasa de false a true (recupera conexión).
    LaunchedEffect(online) {
        if (online) SyncScheduler.sincronizarAhora(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mantenimiento") },
                actions = {
                    Text(
                        if (online) "● En línea" else "● Sin conexión",
                        color = if (online) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nuevo registro", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = codigoActivo,
                        onValueChange = { codigoActivo = it },
                        label = { Text("Código de activo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )

                    Text(
                        "Tipo de actividad",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        RegistrosViewModel.TIPOS_ACTIVIDAD.forEachIndexed { i, opcion ->
                            if (i > 0) Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = tipoActividad == opcion,
                                onClick = { tipoActividad = opcion },
                                label = { Text(opcion) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = observacion,
                        onValueChange = { observacion = it },
                        label = { Text("Observación") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )

                    if (errorValidacion != null) {
                        Text(
                            errorValidacion!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.capturar(codigoActivo.trim(), tipoActividad, observacion.trim()) {
                                codigoActivo = ""
                                observacion = ""
                                if (online) SyncScheduler.sincronizarAhora(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    ) {
                        Text("Guardar registro")
                    }
                }
            }

            ResumenSync(workInfo)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Registros (${registros.size})", style = MaterialTheme.typography.titleMedium)
                Row {
                    OutlinedButton(
                        onClick = { SyncScheduler.sincronizarAhora(context) },
                        enabled = online
                    ) { Text("Sincronizar ahora") }
                    OutlinedButton(
                        onClick = {
                            app.sesionStore.cerrarSesion()
                            onLogout()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) { Text("Cerrar sesión") }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(registros, key = { it.idRegistro }) { registro ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(registro.codigoActivo, fontWeight = FontWeight.Bold)
                                Text(registro.tipoActividad, style = MaterialTheme.typography.bodySmall)
                                Text(registro.observacion, style = MaterialTheme.typography.bodySmall)
                                Text(registro.capturadoEn, style = MaterialTheme.typography.labelSmall)
                                if (registro.estado == EstadoRegistro.ERROR && registro.ultimoError != null) {
                                    Text(
                                        registro.ultimoError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            EstadoBadge(registro.estado)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenSync(workInfo: WorkInfo?) {
    if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
        val enviados = workInfo.outputData.getInt("enviados", 0)
        val fallados = workInfo.outputData.getInt("fallados", 0)
        if (enviados > 0 || fallados > 0) {
            Text(
                "Última sincronización: $enviados enviado(s), $fallados fallido(s)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun EstadoBadge(estado: EstadoRegistro) {
    val (texto, color) = when (estado) {
        EstadoRegistro.PENDIENTE -> "PENDIENTE" to MaterialTheme.colorScheme.tertiary
        EstadoRegistro.SINCRONIZADO -> "SINCRONIZADO" to MaterialTheme.colorScheme.primary
        EstadoRegistro.ERROR -> "ERROR" to MaterialTheme.colorScheme.error
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(texto, color = color, style = MaterialTheme.typography.labelSmall)
    }
}
