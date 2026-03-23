package com.rakizz.student.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object Auth : NavRoutes("auth_route")
    data object Home : NavRoutes("home_route")
    data object MaterialsList : NavRoutes("materials_list")
    data object MaterialDetail : NavRoutes("material_detail/{id}") {
        fun createRoute(id: String) = "material_detail/$id"
    }
    data object AssignmentsList : NavRoutes("assignments_list")
    data object QuizzesList : NavRoutes("quizzes_list")
    data object QuizDetail : NavRoutes("quiz_detail/{id}") {
        fun createRoute(id: String) = "quiz_detail/$id"
    }
}
