package com.sofratesa.mantenimiento.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    @Insert
    suspend fun insertar(registro: Registro)

    @Query("SELECT * FROM registros ORDER BY capturadoEn DESC")
    fun observarTodos(): Flow<List<Registro>>

    @Query("SELECT * FROM registros WHERE estado != 'SINCRONIZADO'")
    suspend fun obtenerPendientesOError(): List<Registro>

    @Query("UPDATE registros SET estado = :estado, ultimoError = :error WHERE idRegistro = :id")
    suspend fun actualizarEstado(id: String, estado: EstadoRegistro, error: String?)
}
