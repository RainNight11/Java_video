package com.example.java_video.ui.screens.create

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.Intent
import android.provider.Settings
import com.example.java_video.data.local.AudioFileManager
import com.example.java_video.data.local.AudioPlayer
import com.example.java_video.data.local.AudioStorageManager
import com.example.java_video.data.local.AudioFile
import com.example.java_video.ui.screens.LocalAudioRecorder
import com.example.java_video.ui.components.ErrorCard
import com.example.java_video.ui.components.InstructionCard
import com.example.java_video.ui.components.RecordingControlCard
import com.example.java_video.ui.components.UploadCard
import com.example.java_video.ui.components.AudioFilesList
import com.example.java_video.ui.components.PermissionErrorCard
import com.example.java_video.ui.components.startRecording
import com.example.java_video.ui.components.stopRecording
import com.example.java_video.ui.components.formatDuration
import com.example.java_video.ui.screens.create.VoiceSettingsDialog
import com.example.java_video.viewmodel.VirtualPresenterViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 语音生成页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceGenerationScreen(viewModel: VirtualPresenterViewModel) {
    val context = LocalContext.current
    val audioFileManager = remember { AudioFileManager(context) }
    val audioPlayer = remember { AudioPlayer(context) }
    val recorder = remember { LocalAudioRecorder(context) }
    
    val uiState by viewModel.uiState.collectAsState()
    val audioFiles by audioFileManager.audioFiles.collectAsState()
    val scrollState = rememberScrollState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // 录音相关状态
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<AudioFile?>(null) }
    var editingFile by remember { mutableStateOf<AudioFile?>(null) }
    var newName by remember { mutableStateOf("") }
    var showVoiceManagement by remember { mutableStateOf(false) }
    
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            errorMessage = null
            startRecording(recorder, {
                isRecording = true
                recordingSeconds = 0L
            }, { error ->
                errorMessage = error
            })
        } else {
            errorMessage = "需要麦克风权限才能录制音频。请点击设置手动授权。"
        }
    }
    
    val fileUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val audioFile = audioFileManager.addUploadedFile(it)
            if (audioFile == null) {
                errorMessage = "文件上传失败，请重试"
            }
        }
    }
    
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingSeconds += 1
            }
        } else {
            recordingSeconds = 0L
        }
    }
    
    LaunchedEffect(Unit) {
        audioFileManager.refreshAudioFiles()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            recorder.cancelRecording()
            audioPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        VoiceGenerationHeroCard()
        
        // 声音库管理区域
        SoundLibraryManagementCard(
            audioFiles = audioFiles,
            audioPlayer = audioPlayer,
            onToggleManagement = { showVoiceManagement = !showVoiceManagement },
            showManagement = showVoiceManagement
        )
        
        if (showVoiceManagement) {
            // 录音和上传控制
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RecordingControlCard(
                    modifier = Modifier.weight(1f),
                    isRecording = isRecording,
                    durationSeconds = recordingSeconds,
                    hasAudioPermission = hasAudioPermission,
                    onRecordToggle = {
                        if (isRecording) {
                            errorMessage = null
                            stopRecording(recorder, {
                                audioFileManager.refreshAudioFiles()
                            }, { error ->
                                errorMessage = error
                            })
                            isRecording = false
                        } else {
                            if (!hasAudioPermission) {
                                errorMessage = null
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                errorMessage = null
                                startRecording(recorder, {
                                    isRecording = true
                                    recordingSeconds = 0L
                                }, { error ->
                                    errorMessage = error
                                })
                            }
                        }
                    }
                )
                
                UploadCard(
                    modifier = Modifier.weight(1f),
                    onUpload = {
                        fileUploadLauncher.launch("audio/*")
                    }
                )
            }
            
            // 声音文件列表
            AudioFilesList(
                audioFiles = audioFiles,
                audioPlayer = audioPlayer,
                onDelete = { audioFile ->
                    fileToDelete = audioFile
                    showDeleteDialog = true
                },
                onEditName = { audioFile ->
                    editingFile = audioFile
                    newName = audioFile.name
                }
            )
        }
        
        // 错误提示
        errorMessage?.let { error ->
            PermissionErrorCard(
                errorMessage = error,
                onDismiss = { errorMessage = null },
                onGoToSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        }

        // 文本输入区域
        OutlinedTextField(
            value = uiState.script,
            onValueChange = { viewModel.onScriptChange(it) },
            label = { Text("要转换的文本") },
            placeholder = { Text("输入您想要转换为语音的文本内容...") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = brandedTextFieldColors()
        )

        // 当前设置预览
        CurrentSettingsCard(
            emotion = uiState.selectedEmotion,
            speed = uiState.voiceSpeed,
            pitch = uiState.voicePitch,
            useCustomVoice = uiState.useCustomVoice && uiState.voiceId != null,
            onSettingsClick = { showSettingsDialog = true }
        )

        // 生成按钮
        if (uiState.isGeneratingVoice) {
            VoiceGenerationProgressCard(
                state = uiState.voiceGenerationState
            )
        } else {
            Button(
                onClick = { 
                    showSettingsDialog = true // 先打开设置对话框
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.script.isNotBlank(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("生成语音")
            }
        }

        // 错误提示
        uiState.voiceGenerationError?.let {
            ErrorCard(
                error = it,
                onDismiss = { viewModel.clearVoiceGenerationError() }
            )
        }

        // 生成的语音结果
        uiState.generatedVoiceUrl?.let { url ->
            GeneratedVoiceCard(
                audioUrl = url,
                onReset = { viewModel.resetVoiceGeneration() }
            )
        }

        // 提示信息
        if (uiState.voiceId == null) {
            Text(
                text = "💡 提示：上传自定义声音可以获得更个性化的语音效果",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // 删除确认对话框
        if (showDeleteDialog && fileToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    fileToDelete = null
                },
                title = {
                    Text("确认删除")
                },
                text = {
                    Text("确定要删除「${fileToDelete?.name}」吗？\n此操作无法撤销。")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            fileToDelete?.let { file ->
                                if (audioPlayer.isCurrentFile(file.file)) {
                                    audioPlayer.stop()
                                }
                                audioFileManager.deleteAudioFile(file)
                            }
                            showDeleteDialog = false
                            fileToDelete = null
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            fileToDelete = null
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
        
        // 重命名对话框
        if (editingFile != null) {
            AlertDialog(
                onDismissRequest = {
                    editingFile = null
                    newName = ""
                },
                title = {
                    Text("重命名音频")
                },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("新名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            editingFile?.let { file ->
                                if (audioFileManager.renameAudioFile(file, newName)) {
                                    // 重命名成功，无需额外操作
                                } else {
                                    errorMessage = "重命名失败，请重试"
                                }
                            }
                            editingFile = null
                            newName = ""
                        },
                        enabled = newName.isNotBlank() && newName != editingFile?.name
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            editingFile = null
                            newName = ""
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
        
        InstructionCard(
            title = "语音生成使用指南",
            description = "基于您的文字内容，生成自然流畅的语音文件。",
            steps = listOf(
                "输入您想要转换的文本内容",
                "选择情感风格和语音参数",
                "点击生成按钮开始处理",
                "下载或播放生成的语音文件"
            )
        )
    }

    // 语音设置对话框
    if (showSettingsDialog) {
        VoiceSettingsDialog(
            script = uiState.script,
            voiceId = uiState.voiceId,
            selectedEmotion = uiState.selectedEmotion,
            voiceSpeed = uiState.voiceSpeed,
            voicePitch = uiState.voicePitch,
            useCustomVoice = uiState.useCustomVoice,
            onEmotionSelected = viewModel::onEmotionSelected,
            onVoiceSpeedChanged = viewModel::onVoiceSpeedChanged,
            onVoicePitchChanged = viewModel::onVoicePitchChanged,
            onUseCustomVoiceChanged = viewModel::onUseCustomVoiceChanged,
            onGenerate = { emotion, speed, pitch, useCustom ->
                viewModel.generateVoiceFromText(
                    text = uiState.script,
                    voiceId = if (useCustom) uiState.voiceId else null,
                    emotion = emotion,
                    speed = speed,
                    pitch = pitch
                )
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
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
private fun VoiceGenerationHeroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
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
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF4CAF50)
                    )
                }
                
                Column {
                    Text(
                        text = "智能语音生成",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "文字转自然语音",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "将您的文字转换为自然流畅的语音，支持多种情感风格和音色选择。内置声音管理功能，轻松录制和上传自定义音色。",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 特性标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("声音管理", "情感控制", "自定义音色").forEach { feature ->
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = feature,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentSettingsCard(
    emotion: com.example.java_video.domain.model.Emotion,
    speed: Float,
    pitch: Float,
    useCustomVoice: Boolean,
    onSettingsClick: () -> Unit
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
                Column {
                    Text(
                        text = "当前设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "情感: ${emotionToChinese(emotion)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "语速: ${String.format("%.1f", speed)}x",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (useCustomVoice) {
                            Text(
                                text = "自定义声音",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                TextButton(onClick = onSettingsClick) {
                    Text("调整设置")
                }
            }
        }
    }
}

@Composable
private fun VoiceGenerationProgressCard(
    state: com.example.java_video.domain.usecase.TTSGenerationState?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state) {
                is com.example.java_video.domain.usecase.TTSGenerationState.Loading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "准备生成语音...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                is com.example.java_video.domain.usecase.TTSGenerationState.Processing -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "正在生成语音 (${state.progress}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    state.estimatedTimeRemaining?.let { time ->
                        Text(
                            text = "预计剩余时间: ${time}秒",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun GeneratedVoiceCard(
    audioUrl: String,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "✅ 语音生成完成",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* TODO: 播放音频 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("播放")
                }
                
                OutlinedButton(
                    onClick = { /* TODO: 下载音频 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("下载")
                }
            }
            
            TextButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重新生成")
            }
        }
    }
}

private fun emotionToChinese(emotion: com.example.java_video.domain.model.Emotion): String {
    return when (emotion) {
        com.example.java_video.domain.model.Emotion.NEUTRAL -> "中性"
        com.example.java_video.domain.model.Emotion.HAPPY -> "开心"
        com.example.java_video.domain.model.Emotion.EXCITED -> "兴奋"
        com.example.java_video.domain.model.Emotion.SERIOUS -> "严肃"
        com.example.java_video.domain.model.Emotion.GENTLE -> "温柔"
    }
}

@Composable
private fun SoundLibraryManagementCard(
    audioFiles: List<AudioFile>,
    audioPlayer: AudioPlayer,
    onToggleManagement: () -> Unit,
    showManagement: Boolean
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "声音库管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "已上传 ${audioFiles.size} 个声音文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onToggleManagement) {
                    Icon(
                        imageVector = if (showManagement) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(if (showManagement) "收起" else "展开")
                }
            }
            
            if (!showManagement && audioFiles.isNotEmpty()) {
                // 显示最近的声音文件
                val recentFiles = audioFiles.take(3)
                recentFiles.forEach { audioFile ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = audioFile.name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(audioFile.size / 1024)} KB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (audioFiles.size > 3) {
                    Text(
                        text = "... 还有 ${audioFiles.size - 3} 个文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}



