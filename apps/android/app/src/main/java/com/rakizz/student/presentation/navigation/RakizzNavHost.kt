package com.rakizz.student.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rakizz.student.presentation.auth.LoginScreen
import com.rakizz.student.presentation.home.HomeScreen

@Composable
fun RakizzNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Auth.route
    ) {
        composable(NavRoutes.Auth.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavRoutes.Home.route) {
            HomeScreen(rootNavController = navController)
        }
        
        // Detail screens would be hosted either within the Home graph or here
        // depending on whether the bottom bar should obscure them.
        // For foundation, we map them directly on the root.
    }
}
