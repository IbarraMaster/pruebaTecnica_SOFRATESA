package com.sofratesa.mantenimiento.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SyncScheduler {
    private const val TRABAJO_UNICO = "sync-registros"

    fun sincronizarAhora(context: Context) {
        val solicitud = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(TRABAJO_UNICO, ExistingWorkPolicy.REPLACE, solicitud)
    }

    fun observarUltimoResultado(context: Context): Flow<WorkInfo?> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(TRABAJO_UNICO)
            .map { it.firstOrNull() }
}
