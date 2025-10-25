package com.example.java_video.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "主页", Icons.Default.Dashboard)
    object Create : Screen("create", "创作", Icons.Default.Create)
    object Voice : Screen("voice", "声音", Icons.Default.GraphicEq)
}
