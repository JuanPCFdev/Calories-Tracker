package com.juanpcf.caloriestracker.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.juanpcf.caloriestracker.R

private data class BottomNavItem(
    val route: Any,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Home, R.string.nav_diary, Icons.Filled.Home),
    BottomNavItem(AddFood, R.string.add_food, Icons.Outlined.AddCircleOutline),
    BottomNavItem(Analytics, R.string.nav_analytics, Icons.Filled.BarChart),
    BottomNavItem(Settings, R.string.nav_settings, Icons.Filled.Person),
)

@Composable
fun CaloriesTrackerBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Popear hasta el inicio del grafo de tabs (Home), NO hasta la raíz del
                        // NavHost: tras el login, AuthGraph se elimina del back stack y popUpTo a un
                        // destino ausente no popea nada, dejando el stack y el saveState/restoreState
                        // inconsistentes (la tab cambia pero la pantalla anterior se queda).
                        popUpTo(Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(item.icon, contentDescription = stringResource(item.labelRes))
                },
                label = { Text(stringResource(item.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
