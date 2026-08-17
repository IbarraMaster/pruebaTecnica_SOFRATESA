package com.sofratesa.mantenimiento.ui.principal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofratesa.mantenimiento.data.local.EstadoRegistro
import com.sofratesa.mantenimiento.data.local.Registro
import com.sofratesa.mantenimiento.data.local.RegistroDao
import com.sofratesa.mantenimiento.util.TiempoUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RegistrosViewModel(private val dao: RegistroDao) : ViewModel() {

    val registros: StateFlow<List<Registro>> = dao.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorValidacion = MutableStateFlow<String?>(null)
    val errorValidacion: StateFlow<String?> = _errorValidacion

    fun capturar(codigoActivo: String, tipoActividad: String, observacion: String, alGuardar: () -> Unit) {
        val error = validar(codigoActivo, tipoActividad, observacion)
        if (error != null) {
            _errorValidacion.value = error
            return
        }
        _errorValidacion.value = null
        viewModelScope.launch {
            dao.insertar(
                Registro(
                    idRegistro = UUID.randomUUID().toString(),
                    codigoActivo = codigoActivo,
                    tipoActividad = tipoActividad,
                    observacion = observacion,
                    capturadoEn = TiempoUtil.ahoraIso8601Utc(),
                    estado = EstadoRegistro.PENDIENTE,
                    ultimoError = null
                )
            )
            alGuardar()
        }
    }

    companion object {
        val TIPOS_ACTIVIDAD = listOf("PREVENTIVO", "CORRECTIVO", "INSPECCION")
        private val CODIGO_REGEX = Regex("^[A-Za-z0-9_-]{3,30}$")

        /** Mismas reglas que records-service (sección 5.1 de la prueba). */
        fun validar(codigoActivo: String, tipoActividad: String, observacion: String): String? = when {
            !CODIGO_REGEX.matches(codigoActivo) -> "Código de activo: 3 a 30 caracteres alfanuméricos, guiones o guiones bajos"
            tipoActividad !in TIPOS_ACTIVIDAD -> "Tipo de actividad inválido"
            observacion.isBlank() || observacion.length > 500 -> "Observación: entre 1 y 500 caracteres"
            else -> null
        }
    }
}
