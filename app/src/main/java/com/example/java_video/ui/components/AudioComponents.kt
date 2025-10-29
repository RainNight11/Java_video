package com.example.java_video.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.java_video.data.local.AudioFile
import com.example.java_video.data.local.AudioFileManager
import com.example.java_video.data.local.AudioPlayer
import com.example.java_video.ui.screens.LocalAudioRecorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VoiceHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "声音管理中心",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "录制或上传音频文件，构建你的声音库",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RecordingControlCard(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    durationSeconds: Long,
    hasAudioPermission: Boolean,
    onRecordToggle: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "录音",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            FilledIconButton(
                onClick = onRecordToggle,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRecording) Color.Red
                    else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isRecording) {
                Text(
                    text = formatDuration(durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "点击录制",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UploadCard(
    modifier: Modifier = Modifier,
    onUpload: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "上传文件",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            FilledIconButton(
                onClick = onUpload,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = "选择音频",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AudioFilesList(
    audioFiles: List<AudioFile>,
    audioPlayer: AudioPlayer,
    onDelete: (AudioFile) -> Unit,
    onEditName: (AudioFile) -> Unit
) {
    if (audioFiles.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "暂无音频文件\n录制或上传声音文件",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "声音库",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                audioFiles.forEach { audioFile ->
                    AudioFileItem(
                        audioFile = audioFile,
                        audioPlayer = audioPlayer,
                        onDelete = { onDelete(audioFile) },
                        onEditName = { onEditName(audioFile) }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioFileItem(
    audioFile: AudioFile,
    audioPlayer: AudioPlayer,
    onDelete: () -> Unit,
    onEditName: () -> Unit
) {
    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val currentFile by audioPlayer.currentFile.collectAsState()
    val isThisFilePlaying = isPlaying && currentFile?.absolutePath == audioFile.file.absolutePath
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioFile.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onEditName() },
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${(audioFile.size / 1024)} KB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (audioFile.isLocalRecording) {
                        Text(
                            text = "• 录制",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (audioFile.isUpload) {
                        Text(
                            text = "• 上传",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            
            Row {
                IconButton(
                    onClick = {
                        if (isThisFilePlaying) {
                            audioPlayer.pause()
                        } else {
                            audioPlayer.play(audioFile.file)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isThisFilePlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isThisFilePlaying) "停止" else "播放",
                        tint = if (isThisFilePlaying) Color.Red else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionErrorCard(
    errorMessage: String,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "⚠️ 权限需要",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                
                TextButton(onClick = onDismiss) {
                    Text("忽略", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Button(
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("前往设置")
            }
        }
    }
}

fun startRecording(
    recorder: LocalAudioRecorder,
    onStarted: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val file = recorder.startRecording()
        if (file != null) {
            android.util.Log.d("AudioComponents", "开始录制，文件路径: ${file.absolutePath}")
            onStarted()
        } else {
            onError("无法创建录音文件，请检查存储空间")
        }
    } catch (exception: Exception) {
        android.util.Log.e("AudioComponents", "录制开始失败", exception)
        recorder.cancelRecording()
        onError("录制失败：${exception.message}")
    }
}

fun stopRecording(
    recorder: LocalAudioRecorder,
    onCompleted: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val file = recorder.stopRecording()
        if (file != null && file.exists()) {
            android.util.Log.d("AudioComponents", "录制完成，文件: ${file.absolutePath}, 大小: ${file.length()} bytes")
            
            if (file.canRead()) {
                android.util.Log.d("AudioComponents", "文件可读，录制成功")
                onCompleted()
            } else {
                android.util.Log.e("AudioComponents", "文件不可读")
                onError("录音文件无法访问")
            }
        } else {
            android.util.Log.e("AudioComponents", "录音文件不存在或为null")
            onError("录音保存失败，请重试")
        }
    } catch (exception: Exception) {
        android.util.Log.e("AudioComponents", "录制停止失败", exception)
        onError("保存录音时出错：${exception.message}")
    }
}

fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}