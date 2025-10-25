package com.example.java_video.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.java_video.ui.components.InstructionCard
import com.example.java_video.viewmodel.VirtualPresenterViewModel

@Composable
fun DashboardScreen(viewModel: VirtualPresenterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val jobs = uiState.jobs
    val runningCount = jobs.count { it.status == "RUNNING" }
    val completedCount = jobs.count { it.status == "COMPLETED" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HighlightPanel(
                runningCount = runningCount,
                completedCount = completedCount
            )
        }

        item {
            InstructionCard(
                title = "使用说明",
                description = "3 步骤完成你的虚拟主持人视频。",
                steps = listOf(
                    "在「声音」页上传你的音色样本，打造专属声线。",
                    "回到「创作」页编写脚本，并选择创作模式。",
                    "点击「生成视频」，在「主页」跟踪进度并下载成品。"
                )
            )
        }

        when {
            uiState.isSubmitting && jobs.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            jobs.isEmpty() -> {
                item {
                    EmptyStateCard()
                }
            }

            else -> {
                item {
                    Text(
                        text = "任务进度",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(jobs, key = { it.jobId }) {
                    JobListItem(job = it)
                }
            }
        }
    }
}

@Composable
private fun HighlightPanel(
    runningCount: Int,
    completedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "保持灵感在线",
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "随时开跑，掌控每一次内容输出。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Text(
                text = "运行中：$runningCount  ·  已完成：$completedCount",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "还没有任务",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "在「创作」页撰写故事，点下生成，即刻体验疾速内容生产。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
