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
                    val targetRoute = if (role.equals("PARENT", ignoreCase = true)) {
                        NavRoutes.ParentFocus.route
                    } else {
                        NavRoutes.Home.route
                    }

                    navController.navigate(targetRoute) {
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(NavRoutes.ChooseRole.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.ChooseRole.route) {
            ChooseRoleScreen(
                onContinueAsStudent = {
                    navController.navigate(NavRoutes.StudentSignUp.route) {
                        launchSingleTop = true
                    }
                },
                onContinueAsParent = {
                    navController.navigate(NavRoutes.ParentSignUp.route) {
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
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onAlreadyHaveAccountClick = {
                    navController.navigate(NavRoutes.Auth.route) {
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
                    navController.navigate(NavRoutes.ParentFocus.route) {
                        popUpTo(NavRoutes.Auth.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onAlreadyHaveAccountClick = {
                    navController.navigate(NavRoutes.Auth.route) {
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
                    navController.navigate(NavRoutes.MaterialsList.route) {
                        launchSingleTop = true
                    }
                },
                onOpenAssignments = {
                    navController.navigate(NavRoutes.AssignmentsList.route) {
                        launchSingleTop = true
                    }
                },
                onOpenQuizzes = {
                    navController.navigate(NavRoutes.QuizzesList.route) {
                        launchSingleTop = true
                    }
                },
                onOpenFocus = {
                    navController.navigate(NavRoutes.Focus.route) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(NavRoutes.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    navController.navigate(NavRoutes.Auth.route) {
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
                    navController.navigate(NavRoutes.Home.route) {
                        launchSingleTop = true
                    }
                },
                onOpenFocus = {
                    navController.navigate(NavRoutes.Focus.route) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(NavRoutes.Profile.route) {
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
                    navController.navigate(NavRoutes.Home.route) {
                        launchSingleTop = true
                    }
                },
                onOpenLibrary = {
                    navController.navigate(NavRoutes.MaterialsList.route) {
                        launchSingleTop = true
                    }
                },
                onOpenFocus = {
                    navController.navigate(NavRoutes.Focus.route) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(NavRoutes.Profile.route) {
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
                    navController.navigate(NavRoutes.Home.route) {
                        launchSingleTop = true
                    }
                },
                onBackToMaterials = {
                    navController.navigate(NavRoutes.MaterialsList.route) {
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
                    navController.navigate(NavRoutes.Auth.route) {
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
                onGeneratePairCodeClick = {
                    navController.navigate(NavRoutes.PairCode.route) {
                        launchSingleTop = true
                    }
                },
                onLogoutClick = {
                    navController.navigate(NavRoutes.Auth.route) {
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