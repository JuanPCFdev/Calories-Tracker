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
import com.juanpcf.caloriestracker.feature.auth.LoginScreen
import com.juanpcf.caloriestracker.feature.auth.RegisterScreen
import com.juanpcf.caloriestracker.feature.diary.DiaryScreen
import com.juanpcf.caloriestracker.feature.scanner.ScannerScreen
import com.juanpcf.caloriestracker.feature.search.SearchScreen

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
                    onNavigateToSearch = { selectedDate ->
                        navController.navigate(Search)
                    },
                    onNavigateToScanner = { navController.navigate(Scanner) },
                    onNavigateToCameraAi = { navController.navigate(CameraAi) }
                )
            }
            composable<Search> {
                SearchScreen(
                    onNavigateToFoodDetail = { foodId, date ->
                        navController.navigate(FoodDetail(foodId = foodId, selectedDate = date))
                    },
                    onNavigateBack = { navController.popBackStack() }
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
                // TODO Phase 9: AnalyticsScreen(navController)
                androidx.compose.material3.Text("Analytics Screen — coming soon")
            }
            composable<Settings> {
                // TODO Phase 10: SettingsScreen(navController)
                androidx.compose.material3.Text("Settings Screen — coming soon")
            }
            composable<Goals> {
                // TODO Phase 10: GoalsScreen(navController)
                androidx.compose.material3.Text("Goals Screen — coming soon")
            }
            composable<CameraAi>(
                enterTransition   = { slideInVertically { it } + fadeIn() },
                popExitTransition = { slideOutVertically { it } + fadeOut() }
            ) {
                // TODO Phase 8: CameraAiScreen(navController)
                androidx.compose.material3.Text("AI Camera — coming soon")
            }
            composable<AiResult>(
                enterTransition = { fadeIn() }
            ) {
                // TODO Phase 8: AiResultScreen(navController)
                androidx.compose.material3.Text("AI Result — coming soon")
            }
            composable<FoodDetail>(
                enterTransition   = { slideInVertically { it } + fadeIn() },
                popExitTransition = { slideOutVertically { it } + fadeOut() }
            ) { backStackEntry ->
                val args = backStackEntry.toRoute<FoodDetail>()
                // TODO Phase 6: FoodDetailScreen(navController, args.foodId, args.selectedDate)
                androidx.compose.material3.Text("Food Detail: ${args.foodId}")
            }
        }
    }
}
