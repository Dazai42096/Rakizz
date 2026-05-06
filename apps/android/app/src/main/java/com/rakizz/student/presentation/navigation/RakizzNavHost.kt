package com.rakizz.student.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rakizz.student.presentation.assignments.add.AddAssignmentScreen
import com.rakizz.student.presentation.assignments.list.AssignmentsScreen
import com.rakizz.student.presentation.auth.LoginScreen
import com.rakizz.student.presentation.auth.parentsignup.ParentSignUpScreen
import com.rakizz.student.presentation.auth.role.ChooseRoleScreen
import com.rakizz.student.presentation.auth.signup.StudentSignUpScreen
import com.rakizz.student.presentation.focus.FocusScreen
import com.rakizz.student.presentation.home.HomeScreen
import com.rakizz.student.presentation.materials.detail.MaterialDetailScreen
import com.rakizz.student.presentation.materials.list.MaterialsScreen
import com.rakizz.student.presentation.paircode.PairCodeScreen
import com.rakizz.student.presentation.parentfocus.ParentFocusScreen
import com.rakizz.student.presentation.profile.ProfileScreen
import com.rakizz.student.presentation.progress.StudentProgressScreen
import com.rakizz.student.presentation.quizzes.detail.QuizDetailScreen
import com.rakizz.student.presentation.quizzes.list.QuizzesScreen
import com.rakizz.student.presentation.quizzes.setup.QuizSetupScreen
import com.rakizz.student.presentation.unlock.UnlockQuizScreen

@Composable
fun RakizzNavHost(
    startUnlockPackage: String? = null,
    forceUnlock: Boolean = false,
    onUnlockPackageHandled: () -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(startUnlockPackage, forceUnlock) {
        val blockedPackage = startUnlockPackage

        if (!blockedPackage.isNullOrBlank()) {
            // opened from accessibility service when a blocked app is clicked
            navController.navigate(
                NavRoutes.UnlockQuiz.createRoute(
                    packageName = blockedPackage,
                    forceUnlock = forceUnlock
                )
            ) {
                launchSingleTop = true
            }

            onUnlockPackageHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Auth.route
    ) {
        composable(NavRoutes.Auth.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    // student goes to student dashboard, parent goes to parent page
                    val targetRoute = if (role.equals("PARENT", ignoreCase = true)) {
                        NavRoutes.ParentFocus.route //parent login desti
                    } else {
                        NavRoutes.Home.route //student login desti
                    }

                    navController.navigate(targetRoute) {
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(NavRoutes.ChooseRole.route) { //create-account desti
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.ChooseRole.route) {
            ChooseRoleScreen(
                onContinueAsStudent = {
                    navController.navigate(NavRoutes.StudentSignUp.route) { //student role button desti
                        launchSingleTop = true
                    }
                },
                onContinueAsParent = {
                    navController.navigate(NavRoutes.ParentSignUp.route) { //parent role button desti
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.StudentSignUp.route) {
            StudentSignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateAccountClick = {
                    navController.navigate(NavRoutes.Home.route) { //student signup success desti
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onAlreadyHaveAccountClick = {
                    navController.navigate(NavRoutes.Auth.route) { //already have account desti
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.ParentSignUp.route) {
            ParentSignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateAccountDone = {
                    navController.navigate(NavRoutes.ParentFocus.route) { //\parent signup success desti
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onAlreadyHaveAccountClick = {
                    navController.navigate(NavRoutes.Auth.route) { //already have account desti
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.Home.route) {
            HomeScreen(
                onOpenMaterials = {
                    navController.navigate(NavRoutes.MaterialsList.route) { //Home materials button desti
                        launchSingleTop = true
                    }
                },
                onOpenAssignments = {
                    navController.navigate(NavRoutes.AssignmentsList.route) { //Home assignments button desti
                        launchSingleTop = true
                    }
                },
                onOpenQuizzes = {
                    navController.navigate(NavRoutes.QuizzesList.route) { //Home quizzes button desti
                        launchSingleTop = true
                    }
                },
                onOpenFocus = {
                    navController.navigate(NavRoutes.Focus.route) { // Home focus button dest
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(NavRoutes.Profile.route) { //Home profile button desti
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    navController.navigate(NavRoutes.Auth.route) { //logout desti
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.MaterialsList.route) {
            MaterialsScreen(
                onNavigateToDetail = { materialId ->
                    navController.navigate(
                        NavRoutes.MaterialDetail.createRoute(materialId)
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenHome = {
                    navController.navigate(NavRoutes.Home.route) { //Materials bottom/home desti
                        launchSingleTop = true
                    }
                },
                onOpenFocus = {
                    navController.navigate(NavRoutes.Focus.route) { //Materials focus desti
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(NavRoutes.Profile.route) { //Materials profile desti
                        launchSingleTop = true
                    }
                },
                onAddMaterial = {},
                onTakePhoto = {},
                onWriteTextNotes = {},
                onAddLink = {}
            )
        }

        composable(NavRoutes.MaterialDetail.route) { backStackEntry ->
            val materialId = backStackEntry.arguments
                ?.getString("id")
                .orEmpty()

            MaterialDetailScreen(
                materialId = materialId,
                onBackClick = {
                    navController.popBackStack()
                },
                onGenerateQuizClick = { selectedMaterialId ->
                    navController.navigate(
                        NavRoutes.QuizSetup.createRoute(selectedMaterialId)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.QuizSetup.route) { backStackEntry ->
            val materialId = backStackEntry.arguments
                ?.getString("materialId")
                .orEmpty()

            QuizSetupScreen(
                materialId = materialId,
                onBackClick = {
                    navController.popBackStack()
                },
                onQuizGenerated = { quizId ->
                    navController.navigate(
                        NavRoutes.QuizDetail.createRoute(quizId)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.QuizzesList.route) {
            QuizzesScreen(
                onNavigateToDetail = { quizId ->
                    navController.navigate(
                        NavRoutes.QuizDetail.createRoute(quizId)
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenHome = {
                    navController.navigate(NavRoutes.Home.route) { //Quizzes home desti
                        launchSingleTop = true
                    }
                },
                onOpenLibrary = {
                    navController.navigate(NavRoutes.MaterialsList.route) { //Quizzes library desti
                        launchSingleTop = true
                    }
                },
                onOpenFocus = {
                    navController.navigate(NavRoutes.Focus.route) { //Quizzes focus desti
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(NavRoutes.Profile.route) { //Quizzes profile destin
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.QuizDetail.route) { backStackEntry ->
            val quizId = backStackEntry.arguments
                ?.getString("id")
                .orEmpty()

            QuizDetailScreen(
                quizId = quizId,
                onGoHome = {
                    navController.navigate(NavRoutes.Home.route) { // QuizDetail home destination
                        launchSingleTop = true
                    }
                },
                onBackToMaterials = {
                    navController.navigate(NavRoutes.MaterialsList.route) { //QuizDetail back destination
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.AssignmentsList.route) {
            AssignmentsScreen(
                navController = navController
            )
        }

        composable(NavRoutes.AddAssignment.route) {
            AddAssignmentScreen(
                navController = navController
            )
        }

        composable(NavRoutes.Focus.route) {
            FocusScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTryBlockedAppClick = { packageName ->
                    //manual way to open unlock flow from focus screen
                    navController.navigate(
                        NavRoutes.UnlockQuiz.createRoute(
                            packageName = packageName,
                            forceUnlock = true
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.UnlockQuiz.route) { backStackEntry ->
            val packageName = Uri.decode(
                backStackEntry.arguments
                    ?.getString("packageName")
                    .orEmpty()
            )

            val forceUnlock = backStackEntry.arguments
                ?.getString("forceUnlock")
                ?.toBooleanStrictOrNull()
                ?: false

            UnlockQuizScreen(
                packageName = packageName,
                forceUnlock = forceUnlock,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.ParentFocus.route) {
            ParentFocusScreen(
                onBackClick = {
                    // parent page uses back as sign out for simple navigation
                    navController.navigate(NavRoutes.Auth.route) { //parent back/sign-out destination
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.Profile.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    navController.navigate(NavRoutes.Auth.route) { //profile logout destination
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.Progress.route) {
            StudentProgressScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onDownloadClick = {}
            )
        }

        composable(NavRoutes.PairCode.route) {
            PairCodeScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCopyClick = {},
                onShareClick = {},
                onRegenerateClick = {}
            )
        }
    }
}