package com.rakizz.student.presentation.navigation

import androidx.compose.runtime.Composable
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
                    // parent should open parent signup page
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
                    // after parent signup go to home
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
                onOpenParentFocus = {
                    // this opens the parent rules screen
                    navController.navigate(NavRoutes.ParentFocus.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.MaterialsList.route) {
            MaterialsScreen(
                onNavigateToDetail = { materialId ->
                    navController.navigate(NavRoutes.MaterialDetail.createRoute(materialId))
                },
                onOpenHome = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Home.route) {
                            inclusive = false
                        }
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
                onAddMaterial = {
                    // later
                },
                onTakePhoto = {
                    // later
                },
                onWriteTextNotes = {
                    // later
                },
                onAddLink = {
                    // later
                }
            )
        }

        composable(NavRoutes.MaterialDetail.route) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("id").orEmpty()

            MaterialDetailScreen(
                materialId = materialId,
                onBackClick = {
                    navController.popBackStack()
                },
                onGenerateQuizClick = { selectedMaterialId ->
                    navController.navigate(
                        NavRoutes.QuizSetup.createRoute(selectedMaterialId)
                    )
                }
            )
        }

        composable(NavRoutes.QuizSetup.route) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId").orEmpty()

            QuizSetupScreen(
                materialId = materialId,
                onBackClick = {
                    navController.popBackStack()
                },
                onQuizGenerated = { quizId ->
                    navController.navigate(NavRoutes.QuizDetail.createRoute(quizId))
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

        composable(NavRoutes.QuizzesList.route) {
            QuizzesScreen(
                onNavigateToDetail = { quizId ->
                    navController.navigate(NavRoutes.QuizDetail.createRoute(quizId))
                }
            )
        }

        composable(NavRoutes.QuizDetail.route) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("id").orEmpty()

            QuizDetailScreen(
                quizId = quizId,
                onStartQuiz = {
                    navController.popBackStack()
                },
                onGoHome = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onTryAnotherQuiz = {
                    navController.popBackStack()
                },
                onBackToMaterials = {
                    navController.popBackStack(NavRoutes.MaterialsList.route, false)
                }
            )
        }

        composable(NavRoutes.Profile.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSettingsClick = {
                    // later
                },
                onGeneratePairCodeClick = {
                    navController.navigate(NavRoutes.PairCode.route) {
                        launchSingleTop = true
                    }
                },
                onNotificationsClick = {
                    // later
                },
                onPrivacyClick = {
                    // later
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

        composable(NavRoutes.Focus.route) {
            FocusScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTakeUnlockQuizClick = {
                    navController.navigate(NavRoutes.QuizzesList.route) {
                        launchSingleTop = true
                    }
                },
                onViewProgressClick = {
                    navController.navigate(NavRoutes.Progress.route) {
                        launchSingleTop = true
                    }
                },
                onGoToMaterialsClick = {
                    navController.navigate(NavRoutes.MaterialsList.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.ParentFocus.route) {
            ParentFocusScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.Progress.route) {
            StudentProgressScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onDownloadClick = {
                    // later
                }
            )
        }

        composable(NavRoutes.PairCode.route) {
            PairCodeScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCopyClick = {
                    // later
                },
                onShareClick = {
                    // later
                },
                onRegenerateClick = {
                    // later
                }
            )
        }
    }
}