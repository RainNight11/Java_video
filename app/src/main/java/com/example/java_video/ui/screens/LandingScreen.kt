package com.example.java_video.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LandingScreen(
    onCreative: () -> Unit,
    onBroadcast: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(24.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI 虚拟口播",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "选择创意动画或虚拟播报模式开始创作。",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onCreative
        ) {
            Text("自由创作动画")
        }
        Button(
            modifier = Modifier.padding(top = 12.dp),
            onClick = onBroadcast
        ) {
            Text("虚拟人播报")
        }
    }
}
