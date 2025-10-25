package com.example.java_video.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.java_video.ui.components.ErrorCard
import com.example.java_video.ui.components.InstructionCard
import com.example.java_video.viewmodel.VirtualPresenterViewModel
import kotlinx.coroutines.delay
import java.io.File

private enum class VoiceInputOption {
    Upload, Record
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(viewModel: VirtualPresenterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val recorder = remember { SimpleAudioRecorder(context) }
    var selectedOption by rememberSaveable { mutableStateOf(VoiceInputOption.Upload) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0L) }
    var captureError by remember { mutableStateOf<String?>(null) }
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
            beginRecording(
                recorder = recorder,
                onStarted = {
                    captureError = null
                    isRecording = true
                    recordingSeconds = 0L
                },
                onError = { captureError = it }
            )
        } else {
            captureError = "需要麦克风权限才能录制音频。"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder.cancelRecording()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        VoiceHero()

        if (uiState.voiceId != null) {
            CurrentVoiceCard(voiceId = uiState.voiceId!!)
        }

        OutlinedTextField(
            value = uiState.voiceLabel,
            onValueChange = { viewModel.onVoiceLabelChange(it) },
            label = { Text("声音标签（可选）") },
            placeholder = { Text("例如：运动励志女声 / 沉稳男声") },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = voiceTextFieldColors()
        )

        VoiceInputModeSelector(
            selected = selectedOption,
            onSelect = { option ->
                if (option == VoiceInputOption.Upload && isRecording) {
                    recorder.cancelRecording()
                    isRecording = false
                    recordingSeconds = 0L
                }
                if (option == VoiceInputOption.Upload) {
                    captureError = null
                }
                selectedOption = option
            }
        )

        when (selectedOption) {
            VoiceInputOption.Upload -> UploadVoiceCard(
                isUploading = uiState.isUploadingVoice,
                onFileChosen = { uri ->
                    captureError = null
                    viewModel.uploadVoiceFromUri(uri)
                }
            )

            VoiceInputOption.Record -> RecordVoiceCard(
                isRecording = isRecording,
                durationSeconds = recordingSeconds,
                isUploading = uiState.isUploadingVoice,
                onRecordToggle = {
                    if (uiState.isUploadingVoice) return@RecordVoiceCard
                    if (isRecording) {
                        finishRecording(
                            recorder = recorder,
                            onFileReady = { file ->
                                captureError = null
                                viewModel.uploadVoiceFromFile(file, mimeType = "audio/m4a")
                            },
                            onError = { captureError = it }
                        )
                        isRecording = false
                    } else {
                        if (hasAudioPermission) {
                            beginRecording(
                                recorder = recorder,
                                onStarted = {
                                    captureError = null
                                    isRecording = true
                                    recordingSeconds = 0L
                                },
                                onError = { captureError = it }
                            )
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )
        }

        if (uiState.isUploadingVoice) {
            UploadingIndicator()
        }

        captureError?.let {
            ErrorCard(error = it, onDismiss = { captureError = null })
        }

        uiState.uploadError?.let {
            ErrorCard(error = it, onDismiss = viewModel::clearError)
        }

        InstructionCard(
            title = "声音样本建议",
            description = "稳定的音色会让你的虚拟主持人更像 Nike 的专业解说。",
            steps = listOf(
                "准备 30-60 秒、无背景音乐的清晰语音。",
                "保持节奏明快，避免过多停顿或口头禅。",
                "推荐使用 WAV、M4A 或直接录制，确保音量不过载。"
            )
        )
    }
}

@Composable
private fun CurrentVoiceCard(voiceId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "当前声音 ID",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = voiceId,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun VoiceInputModeSelector(
    selected: VoiceInputOption,
    onSelect: (VoiceInputOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VoiceInputChoiceChip(
                label = "上传样本",
                description = "使用现有音频文件",
                selected = selected == VoiceInputOption.Upload,
                onClick = { onSelect(VoiceInputOption.Upload) }
            )
            VoiceInputChoiceChip(
                label = "即时录制",
                description = "像微信语音一样开讲",
                selected = selected == VoiceInputOption.Record,
                onClick = { onSelect(VoiceInputOption.Record) }
            )
        }
    }
}

@Composable
private fun VoiceInputChoiceChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onSecondary
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UploadVoiceCard(
    isUploading: Boolean,
    onFileChosen: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let(onFileChosen)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "上传音频样本",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "支持 WAV / MP3 / M4A 等常见格式，让虚拟主理人学习你的声线。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    if (!isUploading) {
                        launcher.launch("audio/*")
                    }
                },
                enabled = !isUploading,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("选择文件上传")
            }
        }
    }
}

@Composable
private fun RecordVoiceCard(
    isRecording: Boolean,
    durationSeconds: Long,
    isUploading: Boolean,
    onRecordToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "即时录制",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "像微信语音一样即刻开讲，建议控制在 30-60 秒，让内容紧凑有力。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            FilledIconButton(
                onClick = onRecordToggle,
                enabled = !isUploading,
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.secondary,
                    contentColor = if (isRecording) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(36.dp))
            }

            if (isRecording) {
                Text(
                    text = formatDuration(durationSeconds),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "再次点击结束录制并自动上传",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isUploading) {
                Text(
                    text = "正在上传录音，请稍候…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "点击开始录制",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UploadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "正在分析声线，请稍候…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VoiceHero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "打造专属声线",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "上传或录制你的声音样本，我们会为虚拟主理人训练一条动感声线。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun voiceTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    cursorColor = MaterialTheme.colorScheme.secondary,
    focusedLabelColor = MaterialTheme.colorScheme.secondary
)

private fun beginRecording(
    recorder: SimpleAudioRecorder,
    onStarted: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        recorder.startRecording()
        onStarted()
    } catch (exception: Exception) {
        recorder.cancelRecording()
        onError("无法开始录音，请确认麦克风可用后重试。")
    }
}

private fun finishRecording(
    recorder: SimpleAudioRecorder,
    onFileReady: (File) -> Unit,
    onError: (String) -> Unit
) {
    val file = recorder.stopRecording()
    if (file != null) {
        onFileReady(file)
    } else {
        onError("录音时间过短或文件异常，请重新录制。")
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
