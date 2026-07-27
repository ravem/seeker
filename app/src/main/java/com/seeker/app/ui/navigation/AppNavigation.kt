package com.seeker.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seeker.app.ui.currentconnection.CurrentConnectionScreen
import com.seeker.app.ui.scanner.WifiScannerScreen
import com.seeker.app.ui.discovery.LanDiscoveryScreen
import com.seeker.app.ui.settings.SettingsScreen
import com.seeker.app.ui.about.AboutScreen
import com.seeker.app.ui.integrations.IntegrationsScreen
import com.seeker.app.ui.controller.ControllerDashboardScreen

/**
 * Navigazione principale dell'app con bottom navigation bar.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val tabs = Tab.entries

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Nascondi bottom bar nelle schermate secondarie
    val showBottomBar = currentRoute in tabs.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val currentDestination = navBackStackEntry?.destination

                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == tab.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.description
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.CurrentConnection.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(route = Tab.CurrentConnection.route) {
                CurrentConnectionScreen(
                    onSettings = { navController.navigate("settings") }
                )
            }
            composable(route = Tab.WifiScanner.route) {
                WifiScannerScreen(
                    onSettings = { navController.navigate("settings") }
                )
            }
            composable(route = Tab.LanDiscovery.route) {
                LanDiscoveryScreen(
                    onSettings = { navController.navigate("settings") }
                )
            }
            composable(route = "settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onAbout = { navController.navigate("about") }
                )
            }
            composable(route = "about") {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(route = Tab.Integrations.route) {
                ControllerDashboardScreen(
                    onSettings = { navController.navigate("integrations_config") }
                )
            }
            composable(route = "integrations_config") {
                IntegrationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
