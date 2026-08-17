package com.sofratesa.mantenimiento.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromEstado(estado: EstadoRegistro): String = estado.name

    @TypeConverter
    fun toEstado(valor: String): EstadoRegistro = EstadoRegistro.valueOf(valor)
}
