package com.example.java_video.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.java_video.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(
    navController: androidx.navigation.NavHostController,
    navigatorState: MutableState<AppDestination>,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleForDestination(navigatorState.value)) },
                navigationIcon = {
                    if (navigatorState.value != AppDestination.Landing) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun titleForDestination(destination: AppDestination): String =
    when (destination) {
        AppDestination.Landing -> "AI 虚拟口播"
        AppDestination.Creative -> "创意动画模式"
        AppDestination.Broadcast -> "虚拟播报模式"
        AppDestination.JobStatus -> "任务进度"
    }
