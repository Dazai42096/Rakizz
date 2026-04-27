package com.rakizz.student.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object Auth : NavRoutes("auth_route")
    data object ChooseRole : NavRoutes("choose_role")
    data object StudentSignUp : NavRoutes("student_sign_up")
    data object Home : NavRoutes("home_route")

    data object MaterialsList : NavRoutes("materials_list")
    data object MaterialDetail : NavRoutes("material_detail/{id}") {
        fun createRoute(id: String) = "material_detail/$id"
    }

    data object AssignmentsList : NavRoutes("assignments_list")
    data object AddAssignment : NavRoutes("add_assignment")

    data object QuizzesList : NavRoutes("quizzes_list")
    data object QuizSetup : NavRoutes("quiz_setup/{materialId}") {
        fun createRoute(materialId: String) = "quiz_setup/$materialId"
    }

    data object QuizDetail : NavRoutes("quiz_detail/{id}") {
        fun createRoute(id: String) = "quiz_detail/$id"
    }

    data object Profile : NavRoutes("profile")
    data object Focus : NavRoutes("focus")
    data object Progress : NavRoutes("progress")
    data object PairCode : NavRoutes("pair_code")
}