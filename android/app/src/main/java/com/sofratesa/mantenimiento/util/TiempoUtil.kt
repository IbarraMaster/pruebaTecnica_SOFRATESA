package com.sofratesa.mantenimiento.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** minSdk 24 no trae java.time; se evita desugaring con SimpleDateFormat en UTC. */
object TiempoUtil {
    fun ahoraIso8601Utc(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formato.timeZone = TimeZone.getTimeZone("UTC")
        return formato.format(Date())
    }
}
