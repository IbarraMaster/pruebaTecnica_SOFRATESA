package com.sofratesa.mantenimiento.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sofratesa.mantenimiento.MantenimientoApplication
import com.sofratesa.mantenimiento.ui.login.LoginScreen
import com.sofratesa.mantenimiento.ui.principal.PrincipalScreen

object Rutas {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
}

@Composable
fun AppNav(app: MantenimientoApplication) {
    val navController: NavHostController = rememberNavController()
    val online by app.connectivityObserver.estaOnline.collectAsStateWithLifecycle()
    val inicio = if (app.sesionStore.haySesion()) Rutas.PRINCIPAL else Rutas.LOGIN

    NavHost(navController = navController, startDestination = inicio) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                app = app,
                online = online,
                onLoginExitoso = {
                    navController.navigate(Rutas.PRINCIPAL) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.PRINCIPAL) {
            PrincipalScreen(
                app = app,
                online = online,
                onLogout = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.PRINCIPAL) { inclusive = true }
                    }
                }
            )
        }
    }
}
