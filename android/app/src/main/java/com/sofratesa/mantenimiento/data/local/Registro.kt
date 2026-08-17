package com.sofratesa.mantenimiento.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoRegistro { PENDIENTE, SINCRONIZADO, ERROR }

@Entity(tableName = "registros")
data class Registro(
    @PrimaryKey val idRegistro: String,
    val codigoActivo: String,
    val tipoActividad: String,
    val observacion: String,
    val capturadoEn: String,
    val estado: EstadoRegistro,
    val ultimoError: String?
)
