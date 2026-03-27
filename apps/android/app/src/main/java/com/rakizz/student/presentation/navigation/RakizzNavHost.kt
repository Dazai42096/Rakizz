package com.rakizz.student.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rakizz.student.presentation.auth.LoginScreen
import com.rakizz.student.presentation.home.HomeScreen
import com.rakizz.student.presentation.quizzes.detail.QuizDetailScreen
import com.rakizz.student.presentation.quizzes.list.QuizzesScreen

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
            HomeScreen(
                onOpenMaterials = { navController.navigate(NavRoutes.MaterialsList.route) },
                onOpenAssignments = { navController.navigate(NavRoutes.AssignmentsList.route) },
                onOpenQuizzes = { navController.navigate(NavRoutes.QuizzesList.route) }
            )
        }

        composable(NavRoutes.MaterialsList.route) {
            SimpleSectionScreen(
                title = "Materials Screen",
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.AssignmentsList.route) {
            SimpleSectionScreen(
                title = "Assignments Screen",
                onBack = { navController.navigateUp() }
            )
        }

        composable(NavRoutes.QuizzesList.route) {
            QuizzesScreen(
                onNavigateToDetail = { quizId ->
                    navController.navigate(NavRoutes.QuizDetail.createRoute(quizId))
                }
            )
        }

        composable(NavRoutes.QuizDetail.route) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("id").orEmpty()
            QuizDetailScreen(quizId = quizId)
        }
    }
}

@Composable
private fun SimpleSectionScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}