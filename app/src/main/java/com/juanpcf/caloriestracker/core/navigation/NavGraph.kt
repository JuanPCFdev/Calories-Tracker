package com.juanpcf.caloriestracker.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.juanpcf.caloriestracker.feature.analytics.AnalyticsScreen
import com.juanpcf.caloriestracker.feature.auth.LoginScreen
import com.juanpcf.caloriestracker.feature.auth.RegisterScreen
import com.juanpcf.caloriestracker.feature.camera_ai.AiResultScreen
import com.juanpcf.caloriestracker.feature.camera_ai.CameraAiScreen
import com.juanpcf.caloriestracker.feature.diary.DiaryScreen
import com.juanpcf.caloriestracker.feature.diary.edit.DiaryEntryEditScreen
import com.juanpcf.caloriestracker.feature.scanner.ScannerScreen
import com.juanpcf.caloriestracker.feature.search.SearchScreen
import com.juanpcf.caloriestracker.feature.settings.GoalsScreen
import com.juanpcf.caloriestracker.feature.settings.SettingsScreen

@Composable
fun CaloriesTrackerNavHost(
    navController: NavHostController,
    startDestination: Any = AuthGraph,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        navigation<AuthGraph>(startDestination = Login) {
            composable<Login>(
                enterTransition = { slideInHorizontally { -it } + fadeIn() },
                exitTransition  = { slideOutHorizontally { -it } + fadeOut() }
            ) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Register) }
                )
            }
            composable<Register>(
                enterTransition   = { slideInHorizontally { it } + fadeIn() },
                popExitTransition = { slideOutHorizontally { it } + fadeOut() }
            ) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
        }

        navigation<MainGraph>(startDestination = Home) {
            composable<Home> {
                DiaryScreen(
                    onNavigateToEditEntry = { entryId ->
                        navController.navigate(DiaryEntryEdit(entryId))
                    }
                )
            }
            composable<Search> {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScanner = { navController.navigate(Scanner) },
                    onNavigateToCameraAi = { navController.navigate(CameraAi) }
                )
            }
            composable<Scanner> {
                @androidx.camera.core.ExperimentalGetImage
                @Composable
                fun Content() {
                    ScannerScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onFoodAdded = { navController.popBackStack() }
                    )
                }
                Content()
            }
            composable<Analytics> {
                AnalyticsScreen()
            }
            composable<Settings> {
                SettingsScreen(
                    onNavigateToGoals = { navController.navigate(Goals) },
                    onSignOut = {
                        navController.navigate(AuthGraph) {
                            popUpTo(MainGraph) { inclusive = true }
                        }
                    }
                )
            }
            composable<Goals> {
                GoalsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<CameraAi>(
                enterTransition   = { slideInVertically { it } + fadeIn() },
                popExitTransition = { slideOutVertically { it } + fadeOut() }
            ) {
                @androidx.camera.core.ExperimentalGetImage
                @Composable
                fun Content() {
                    CameraAiScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAiResult = { aiResultRoute ->
                            navController.navigate(aiResultRoute)
                        }
                    )
                }
                Content()
            }
            composable<AiResult>(
                enterTransition = { fadeIn() }
            ) {
                AiResultScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDiary = {
                        navController.navigate(Home) {
                            popUpTo(MainGraph) { inclusive = false }
                        }
                    }
                )
            }
            composable<FoodDetail>(
                enterTransition   = { slideInVertically { it } + fadeIn() },
                popExitTransition = { slideOutVertically { it } + fadeOut() }
            ) { backStackEntry ->
                val args = backStackEntry.toRoute<FoodDetail>()
                // Stub route — FoodDetail is kept for forward compatibility.
                // SearchScreen no longer navigates here (replaced by multi-select + bulk save flow).
                androidx.compose.material3.Text("Food Detail: ${args.foodId}")
            }
            composable<DiaryEntryEdit>(
                enterTransition = { slideInVertically { it } + fadeIn() },
                popExitTransition = { slideOutVertically { it } + fadeOut() }
            ) {
                DiaryEntryEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
