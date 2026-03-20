package com.juanpcf.caloriestracker.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    BottomNavItem(Search, R.string.nav_search, Icons.Filled.Search),
    BottomNavItem(Analytics, R.string.nav_analytics, Icons.Filled.BarChart),
    BottomNavItem(Settings, R.string.nav_settings, Icons.Filled.Person),
)

@Composable
fun CaloriesTrackerBottomBar(
    navController: NavController,
    onAddClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(contentAlignment = Alignment.TopCenter) {
        NavigationBar {
            // HOME
            bottomNavItems.forEachIndexed { index, item ->
                // Insert an invisible spacer item at index 2 (center) to reserve space for ADD button
                if (index == 2) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        enabled = false,
                        icon = {},
                        label = { Text("") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
                val selected = currentDestination?.hierarchy?.any {
                    it.hasRoute(item.route::class)
                } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
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

        // Floating ADD circle button — elevated above the nav bar
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_food),
                tint = Color.White
            )
        }
    }
}
