package com.example.java_video.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.ui.components.ErrorCard
import com.example.java_video.ui.components.InstructionCard
import com.example.java_video.viewmodel.VirtualPresenterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(viewModel: VirtualPresenterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeroCard()

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.onTitleChange(it) },
            label = { Text("标题（可选）") },
            placeholder = { Text("取一个有爆发力的标题") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = brandedTextFieldColors()
        )

        OutlinedTextField(
            value = uiState.script,
            onValueChange = { viewModel.onScriptChange(it) },
            label = { Text("故事脚本") },
            placeholder = { Text("写下主持词、故事或产品卖点，保持节奏感。") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = brandedTextFieldColors(),
            isError = uiState.submitError?.contains("script") == true
        )

        Text(
            text = "创作模式",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        ModeSelector(
            selectedMode = uiState.mode,
            onModeSelected = viewModel::onModeSelected
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
                Text("正在生成中…")
            }
        } else {
            Button(
                onClick = { viewModel.submitStory() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.voiceId != null && uiState.script.isNotBlank(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("生成视频")
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
                text = "温馨提示：请先到「声音」页上传音色样本，才能激活生成按钮。",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        InstructionCard(
            title = "写好脚本的小贴士",
            description = "控制在 90-120 秒之间，搭配富有动感的动作，更容易产出精彩内容。",
            steps = listOf(
                "开场用一句号召语抓住注意力。",
                "中段拆解关键信息或产品亮点，保持句式有力量感。",
                "结尾用行动号召呼应品牌态度。"
            )
        )
    }
}

@Composable
private fun brandedTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    cursorColor = MaterialTheme.colorScheme.secondary,
    focusedLabelColor = MaterialTheme.colorScheme.secondary,
)

@Composable
private fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "设计你的故事战术板",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "写下你的脚本，剩下的交给虚拟主理人。可自由命名、自由切换节奏，输出具有运动精神的内容。",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: GenerationMode,
    onModeSelected: (GenerationMode) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModeChip(
            label = "灵感模式",
            description = "更具创意表达",
            selected = selectedMode == GenerationMode.CREATIVE,
            onClick = { onModeSelected(GenerationMode.CREATIVE) }
        )
        ModeChip(
            label = "播报模式",
            description = "节奏稳定有条理",
            selected = selectedMode == GenerationMode.BROADCAST,
            onClick = { onModeSelected(GenerationMode.BROADCAST) }
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
            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            selectedBorderColor = MaterialTheme.colorScheme.secondary
        )
    )
}
