package com.sofratesa.mantenimiento.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sofratesa.mantenimiento.MantenimientoApplication
import com.sofratesa.mantenimiento.data.local.EstadoRegistro
import com.sofratesa.mantenimiento.data.remote.RegistroRequest
import java.io.IOException

/**
 * Envía uno por uno los registros PENDIENTE/ERROR. El servidor es idempotente
 * por id_registro (constraint único + manejo de conflicto -> 200), así que
 * un reintento nunca duplica: como mucho reenvía uno que ya se guardó y el
 * servidor responde 200 en vez de 201.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as MantenimientoApplication
        val dao = app.database.registroDao()
        val api = app.apiService

        val pendientes = dao.obtenerPendientesOError()
        var enviados = 0
        var fallados = 0

        for (registro in pendientes) {
            try {
                val respuesta = api.crearRegistro(
                    RegistroRequest(
                        id_registro = registro.idRegistro,
                        codigo_activo = registro.codigoActivo,
                        tipo_actividad = registro.tipoActividad,
                        observacion = registro.observacion,
                        capturado_en = registro.capturadoEn
                    )
                )
                when (respuesta.code()) {
                    201, 200 -> {
                        dao.actualizarEstado(registro.idRegistro, EstadoRegistro.SINCRONIZADO, null)
                        enviados++
                    }
                    401 -> {
                        dao.actualizarEstado(registro.idRegistro, EstadoRegistro.ERROR, "Sesión inválida o vencida")
                        fallados++
                    }
                    400 -> {
                        dao.actualizarEstado(registro.idRegistro, EstadoRegistro.ERROR, "Datos rechazados por el servidor")
                        fallados++
                    }
                    else -> {
                        dao.actualizarEstado(registro.idRegistro, EstadoRegistro.ERROR, "Error del servidor (${respuesta.code()})")
                        fallados++
                    }
                }
            } catch (e: IOException) {
                // Sin conectividad real a mitad de la ronda: lo que ya se
                // procesó queda como quedó; lo pendiente se reintenta luego.
                return Result.retry()
            }
        }

        return Result.success(workDataOf(KEY_ENVIADOS to enviados, KEY_FALLADOS to fallados))
    }

    companion object {
        const val KEY_ENVIADOS = "enviados"
        const val KEY_FALLADOS = "fallados"
    }
}
