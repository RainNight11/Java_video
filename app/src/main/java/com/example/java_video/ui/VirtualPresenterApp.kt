package com.example.java_video.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.java_video.ui.navigation.Screen
import com.example.java_video.ui.screens.CreateScreen
import com.example.java_video.ui.screens.DashboardScreen
import com.example.java_video.ui.screens.VoiceScreen
import com.example.java_video.viewmodel.VirtualPresenterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualPresenterApp(presenterViewModel: VirtualPresenterViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val colorScheme = MaterialTheme.colorScheme

    val items = listOf(
        Screen.Dashboard,
        Screen.Create,
        Screen.Voice
    )

    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primary,
                        colorScheme.primary.copy(alpha = 0.9f),
                        colorScheme.background
                    )
                )
            )
            .fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "虚拟主理人",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "体验疾速流畅的创作流程",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = colorScheme.onPrimary,
                        scrolledContainerColor = colorScheme.primary.copy(alpha = 0.95f)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 0.dp
                ) {
                    items.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    screen.title.uppercase(),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = colorScheme.secondary,
                                selectedIconColor = colorScheme.onSecondary,
                                selectedTextColor = colorScheme.onSecondary,
                                unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedIconColor = colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen(presenterViewModel) }
                composable(Screen.Create.route) { CreateScreen(presenterViewModel) }
                composable(Screen.Voice.route) { VoiceScreen(presenterViewModel) }
            }
        }
    }
}
