package com.example.java_video.ui.screens.create

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.java_video.ui.components.ErrorCard
import com.example.java_video.ui.components.InstructionCard
import com.example.java_video.viewmodel.VirtualPresenterViewModel

/**
 * AI虚拟人播报页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnchorScreen(viewModel: VirtualPresenterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        EnhancedAIAnchorHeroCard()

        EnhancedInputCard(
            title = "播报标题（可选）",
            value = uiState.title,
            onValueChange = { viewModel.onTitleChange(it) },
            placeholder = "取一个吸引人的标题",
            icon = Icons.Default.PlayCircle
        )

        EnhancedInputCard(
            title = "播报内容",
            value = uiState.script,
            onValueChange = { viewModel.onScriptChange(it) },
            placeholder = "写下新闻、资讯或介绍内容，适合AI主播播报...",
            icon = Icons.Default.Person,
            isLargeField = true,
            isError = uiState.submitError?.contains("script") == true
        )

        EnhancedModeSelector(
            selectedMode = uiState.mode,
            onModeSelected = viewModel::onModeSelected,
            primaryColor = Color(0xFF2196F3)
        )

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
                Text("正在生成播报…")
            }
        } else {
            Button(
                onClick = { viewModel.submitStory() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.voiceId != null && uiState.script.isNotBlank(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("生成播报")
            }
        }

        uiState.submitError?.let {
            ErrorCard(
                error = it,
                onDismiss = { viewModel.clearError() }
            )
        }

        if (uiState.voiceId == null) {
            Text(
                text = "温馨提示：请先到「语音生成」页面上传音色样本，才能激活生成按钮。",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        InstructionCard(
            title = "AI虚拟人播报小贴士",
            description = "适合新闻、资讯、产品介绍等正式内容，保持专业性和可读性。",
            steps = listOf(
                "开场用简洁明了的语句吸引注意力。",
                "中段内容逻辑清晰，分段明确。",
                "结尾总结要点或给出行动指引。",
                "控制整体时长在60-120秒之间效果最佳。"
            )
        )
    }
}

@Composable
private fun brandedTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
)

@Composable
private fun EnhancedAIAnchorHeroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
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
                        .background(Color(0xFF2196F3).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF2196F3)
                    )
                }
                
                Column {
                    Text(
                        text = "AI虚拟人播报",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = "专业主播级播报体验",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "利用先进的AI技术，生成专业级的主播播报内容。完美适配新闻播报、产品介绍、资讯分享等多种场景。",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 特性标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("专业播报", "多种风格", "智能合成").forEach { feature ->
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = feature,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2196F3),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedInputCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isLargeField: Boolean = false,
    isError: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF2196F3),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                isError = isError
            )
        }
    }
}

@Composable
private fun EnhancedModeSelector(
    selectedMode: com.example.java_video.domain.model.GenerationMode,
    onModeSelected: (com.example.java_video.domain.model.GenerationMode) -> Unit,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "播报模式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnhancedModeChip(
                    label = "正式播报",
                    description = "专业稳重",
                    selected = selectedMode == com.example.java_video.domain.model.GenerationMode.BROADCAST,
                    onClick = { onModeSelected(com.example.java_video.domain.model.GenerationMode.BROADCAST) },
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f)
                )
                EnhancedModeChip(
                    label = "活泼播报",
                    description = "生动有趣",
                    selected = selectedMode == com.example.java_video.domain.model.GenerationMode.CREATIVE,
                    onClick = { onModeSelected(com.example.java_video.domain.model.GenerationMode.CREATIVE) },
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedModeChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (selected) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

