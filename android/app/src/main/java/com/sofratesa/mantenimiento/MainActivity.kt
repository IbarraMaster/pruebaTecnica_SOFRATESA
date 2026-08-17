package com.sofratesa.mantenimiento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sofratesa.mantenimiento.ui.nav.AppNav
import com.sofratesa.mantenimiento.ui.theme.MantenimientoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MantenimientoApplication
        setContent {
            MantenimientoAppTheme {
                AppNav(app)
            }
        }
    }
}
