package com.juanpcf.caloriestracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.juanpcf.caloriestracker.core.navigation.CaloriesTrackerBottomBar
import com.juanpcf.caloriestracker.core.navigation.CaloriesTrackerNavHost
import com.juanpcf.caloriestracker.core.navigation.MainGraph
import com.juanpcf.caloriestracker.core.navigation.Scanner
import com.juanpcf.caloriestracker.core.navigation.Search
import com.juanpcf.caloriestracker.core.navigation.CameraAi
import com.juanpcf.caloriestracker.data.sync.FirestoreSyncWorker
import com.juanpcf.caloriestracker.domain.model.AppPreferences
import com.juanpcf.caloriestracker.domain.model.Theme
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.feature.diary.components.AddEntryBottomSheet
import com.juanpcf.caloriestracker.ui.theme.CaloriesTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var appPreferencesRepository: AppPreferencesRepository
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefsOrNull by appPreferencesRepository.preferences
                .collectAsStateWithLifecycle(initialValue = null)
            val prefs = prefsOrNull ?: AppPreferences()

            LaunchedEffect(prefsOrNull?.language) {
                val language = prefsOrNull?.language ?: return@LaunchedEffect
                val target = LocaleListCompat.forLanguageTags(language.tag)
                if (AppCompatDelegate.getApplicationLocales() != target) {
                    AppCompatDelegate.setApplicationLocales(target)
                }
            }

            // Schedule or cancel Firestore periodic sync based on auth state
            val workManager = WorkManager.getInstance(applicationContext)
            LaunchedEffect(Unit) {
                authRepository.authState.collect { user ->
                    if (user != null) {
                        FirestoreSyncWorker.schedulePeriodicSync(workManager)
                    } else {
                        FirestoreSyncWorker.cancelAll(workManager)
                    }
                }
            }

            val darkTheme = when (prefs.theme) {
                Theme.DARK   -> true
                Theme.LIGHT  -> false
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            CaloriesTrackerTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val startDestination = if (authRepository.currentUser != null) MainGraph else com.juanpcf.caloriestracker.core.navigation.AuthGraph
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val isInMainGraph = navBackStackEntry?.destination?.hierarchy?.any {
                    it.hasRoute(MainGraph::class)
                } == true

                // AddEntryBottomSheet state hoisted at Scaffold level so the bottom nav ADD button
                // can trigger it from any tab, not just DiaryScreen.
                var showAddSheet by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (isInMainGraph) {
                            CaloriesTrackerBottomBar(
                                navController = navController,
                                onAddClick = { showAddSheet = true }
                            )
                        }
                    }
                ) { innerPadding ->
                    CaloriesTrackerNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (showAddSheet) {
                    AddEntryBottomSheet(
                        onDismiss = { showAddSheet = false },
                        onNavigateToSearch = {
                            showAddSheet = false
                            navController.navigate(Search)
                        },
                        onNavigateToScanner = {
                            showAddSheet = false
                            navController.navigate(Scanner)
                        },
                        onNavigateToCameraAi = {
                            showAddSheet = false
                            navController.navigate(CameraAi)
                        }
                    )
                }
            }
        }
    }
}
