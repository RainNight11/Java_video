package com.example.java_video.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.java_video.domain.model.JobStatus

@Composable
fun JobListItem(job: JobStatus) {
    val statusColor = when (job.status) {
        "RUNNING" -> MaterialTheme.colorScheme.secondary
        "COMPLETED" -> Color(0xFF6BFF4D)
        "FAILED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusLabel = when (job.status) {
        "RUNNING" -> "生成中"
        "COMPLETED" -> "已完成"
        "FAILED" -> "失败"
        else -> job.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "任务 #${job.jobId.take(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(label = statusLabel, color = statusColor)
            }

            if (job.message.isNotBlank()) {
                Text(
                    text = job.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (job.status) {
                "RUNNING" -> {
                    LinearProgressIndicator(
                        progress = (job.progress.coerceIn(0, 100)) / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "进度 ${job.progress}% · 请稍候",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }

                "COMPLETED" -> {
                    if (job.downloadUrl != null) {
                        Text(
                            text = "成品已就绪，可前往后台下载链接。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor
                        )
                    } else {
                        Text(
                            text = "成品已完成。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor
                        )
                    }
                }

                "FAILED" -> {
                    Text(
                        text = "生成失败，请检查素材后重试。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    color: Color
) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge
    )
}
