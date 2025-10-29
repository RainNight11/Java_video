package com.example.java_video.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.java_video.ui.components.ErrorCard
import com.example.java_video.ui.components.InstructionCard
import com.example.java_video.viewmodel.VirtualPresenterViewModel

/**
 * 视频生成页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoGenerationScreen(viewModel: VirtualPresenterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        VideoGenerationHeroCard()

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.onTitleChange(it) },
            label = { Text("视频标题") },
            placeholder = { Text("给您的视频起一个吸引人的标题") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = brandedTextFieldColors()
        )

        OutlinedTextField(
            value = uiState.script,
            onValueChange = { viewModel.onScriptChange(it) },
            label = { Text("视频脚本") },
            placeholder = { Text("撰写完整的视频脚本，包括开场、主体内容和结尾...") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            colors = brandedTextFieldColors(),
            isError = uiState.submitError?.contains("script") == true
        )

        // 头像设置（可选）
        AvatarSettingsCard(
            avatarImageUrl = uiState.avatarImageUrl,
            onAvatarChange = { /* TODO: 实现头像选择 */ }
        )

        Text(
            text = "创作风格",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        ModeSelector(
            selectedMode = uiState.mode,
            onModeSelected = viewModel::onModeSelected
        )

        // 生成按钮
        if (uiState.isSubmitting) {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("正在生成视频…")
            }
        } else {
            Button(
                onClick = { viewModel.submitStory() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.voiceId != null && uiState.script.isNotBlank(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("生成视频")
            }
        }

        // 错误提示
        uiState.submitError?.let {
            ErrorCard(
                error = it,
                onDismiss = { viewModel.clearError() }
            )
        }

        // 任务列表
        if (uiState.jobs.isNotEmpty()) {
            JobsListCard(
                jobs = uiState.jobs
            )
        }

        // 声音提示
        if (uiState.voiceId == null) {
            Text(
                text = "⚠️ 请先到「语音生成」页面上传音色样本，才能生成完整的虚拟主播视频。",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        InstructionCard(
            title = "视频生成指南",
            description = "生成包含虚拟主播的完整视频内容，适合各类创作场景。",
            steps = listOf(
                "准备吸引人的标题和完整脚本",
                "选择合适的创作风格（正式或创意）",
                "上传虚拟主播头像（可选）",
                "确认音色样本已准备好",
                "点击生成并等待处理完成"
            )
        )
    }
}

@Composable
private fun brandedTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    cursorColor = MaterialTheme.colorScheme.tertiary,
    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
)

@Composable
private fun VideoGenerationHeroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9C27B0).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF9C27B0)
                    )
                }
                
                Column {
                    Text(
                        text = "AI视频创作",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                    Text(
                        text = "完整视频内容创作",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "结合文字内容、虚拟主播和AI音色，生成专业的视频作品。支持多种创作风格，让您的创意生动呈现。",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 特性标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("虚拟主播", "视频合成", "多种模式").forEach { feature ->
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFF9C27B0).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = feature,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9C27B0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarSettingsCard(
    avatarImageUrl: String,
    onAvatarChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "虚拟主播头像",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (avatarImageUrl.isNotBlank()) "已设置头像" else "使用默认头像",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onAvatarChange) {
                    Text(if (avatarImageUrl.isNotBlank()) "更换" else "选择")
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: com.example.java_video.domain.model.GenerationMode,
    onModeSelected: (com.example.java_video.domain.model.GenerationMode) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModeChip(
            label = "创意模式",
            description = "动感活力",
            selected = selectedMode == com.example.java_video.domain.model.GenerationMode.CREATIVE,
            onClick = { onModeSelected(com.example.java_video.domain.model.GenerationMode.CREATIVE) }
        )
        ModeChip(
            label = "正式模式",
            description = "专业稳重",
            selected = selectedMode == com.example.java_video.domain.model.GenerationMode.BROADCAST,
            onClick = { onModeSelected(com.example.java_video.domain.model.GenerationMode.BROADCAST) }
        )
    }
}

@Composable
private fun ModeChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Column {
                Text(text = label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            selectedBorderColor = MaterialTheme.colorScheme.tertiary
        )
    )
}

@Composable
private fun JobsListCard(
    jobs: List<com.example.java_video.domain.model.JobStatus>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "生成任务",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            jobs.take(3).forEach { job ->
                JobItem(
                    title = "视频任务 ${job.jobId.take(8)}",
                    status = job.status,
                    jobId = job.jobId
                )
                if (job != jobs.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            if (jobs.size > 3) {
                Text(
                    text = "还有 ${jobs.size - 3} 个任务...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun JobItem(
    title: String,
    status: String,
    jobId: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VideoFile,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "任务ID: $jobId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 状态标签
        val (statusText, statusColor) = when (status) {
            "COMPLETED" -> "已完成" to MaterialTheme.colorScheme.primary
            "FAILED" -> "失败" to MaterialTheme.colorScheme.error
            "PROCESSING" -> "处理中" to MaterialTheme.colorScheme.secondary
            else -> "等待中" to MaterialTheme.colorScheme.onSurfaceVariant
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
            modifier = Modifier
                .background(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}