package com.example.java_video.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import android.content.Intent
import android.provider.Settings
import com.example.java_video.data.local.AudioFileManager
import com.example.java_video.data.local.AudioPlayer
import com.example.java_video.data.local.AudioStorageManager
import com.example.java_video.data.local.AudioFile
import com.example.java_video.ui.components.VoiceHeroCard
import com.example.java_video.ui.components.RecordingControlCard
import com.example.java_video.ui.components.UploadCard
import com.example.java_video.ui.components.AudioFilesList
import com.example.java_video.ui.components.PermissionErrorCard
import com.example.java_video.ui.components.startRecording
import com.example.java_video.ui.components.stopRecording
import com.example.java_video.ui.components.formatDuration
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen() {
    val context = LocalContext.current
    val audioFileManager = remember { AudioFileManager(context) }
    val audioPlayer = remember { AudioPlayer(context) }
    val recorder = remember { LocalAudioRecorder(context) }
    
    val audioFiles by audioFileManager.audioFiles.collectAsState()
    
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<AudioFile?>(null) }
    var editingFile by remember { mutableStateOf<AudioFile?>(null) }
    var newName by remember { mutableStateOf("") }
    
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 声音标题卡片
        VoiceHeroCard()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 本地录制卡片
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
            
            // 上传文件卡片
            UploadCard(
                modifier = Modifier.weight(1f),
                onUpload = {
                    fileUploadLauncher.launch("audio/*")
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
        
        // 声音列表
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
    }
}