package com.example.java_video.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 语音设置对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsDialog(
    script: String,
    voiceId: String?,
    selectedEmotion: com.example.java_video.domain.model.Emotion,
    voiceSpeed: Float,
    voicePitch: Float,
    useCustomVoice: Boolean,
    onEmotionSelected: (com.example.java_video.domain.model.Emotion) -> Unit,
    onVoiceSpeedChanged: (Float) -> Unit,
    onVoicePitchChanged: (Float) -> Unit,
    onUseCustomVoiceChanged: (Boolean) -> Unit,
    onGenerate: (com.example.java_video.domain.model.Emotion, Float, Float, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("语音生成设置")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "预览文本：\n${script.take(100)}${if (script.length > 100) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 是否使用自定义声音
                if (voiceId != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "使用自定义声音",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = useCustomVoice,
                            onCheckedChange = onUseCustomVoiceChanged
                        )
                    }
                }

                // 情感选择
                Text(
                    text = "情感风格",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                EmotionSelector(
                    selectedEmotion = selectedEmotion,
                    onEmotionSelected = onEmotionSelected
                )

                // 语速调节
                Text(
                    text = "语速: ${String.format("%.1f", voiceSpeed)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = voiceSpeed,
                    onValueChange = onVoiceSpeedChanged,
                    valueRange = 0.5f..2.0f,
                    steps = 14
                )

                // 音调调节
                Text(
                    text = "音调: ${String.format("%.1f", voicePitch)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = voicePitch,
                    onValueChange = onVoicePitchChanged,
                    valueRange = 0.5f..2.0f,
                    steps = 14
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onGenerate(selectedEmotion, voiceSpeed, voicePitch, useCustomVoice)
                }
            ) {
                Text("开始生成")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EmotionSelector(
    selectedEmotion: com.example.java_video.domain.model.Emotion,
    onEmotionSelected: (com.example.java_video.domain.model.Emotion) -> Unit
) {
    val emotions = listOf(
        com.example.java_video.domain.model.Emotion.NEUTRAL to "中性",
        com.example.java_video.domain.model.Emotion.HAPPY to "开心",
        com.example.java_video.domain.model.Emotion.EXCITED to "兴奋",
        com.example.java_video.domain.model.Emotion.SERIOUS to "严肃",
        com.example.java_video.domain.model.Emotion.GENTLE to "温柔"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        emotions.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (emotion, label) ->
                    FilterChip(
                        selected = selectedEmotion == emotion,
                        onClick = { onEmotionSelected(emotion) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}