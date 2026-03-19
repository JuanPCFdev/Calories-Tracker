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
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.juanpcf.caloriestracker.core.navigation.CaloriesTrackerBottomBar
import com.juanpcf.caloriestracker.core.navigation.CaloriesTrackerNavHost
import com.juanpcf.caloriestracker.core.navigation.MainGraph
import com.juanpcf.caloriestracker.domain.model.AppPreferences
import com.juanpcf.caloriestracker.domain.model.Theme
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
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
            val prefs by appPreferencesRepository.preferences
                .collectAsStateWithLifecycle(initialValue = AppPreferences())

            LaunchedEffect(prefs.language) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(prefs.language.tag)
                )
            }

            val darkTheme = when (prefs.theme) {
                Theme.DARK   -> true
                Theme.LIGHT  -> false
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            CaloriesTrackerTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val startDestination = if (authRepository.currentUser != null) MainGraph else com.juanpcf.caloriestracker.core.navigation.AuthGraph

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { CaloriesTrackerBottomBar(navController) }
                ) { innerPadding ->
                    CaloriesTrackerNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
