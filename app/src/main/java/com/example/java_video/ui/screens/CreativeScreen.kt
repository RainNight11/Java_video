package com.example.java_video.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.java_video.domain.model.VoiceSample
import com.example.java_video.ui.state.CreativeEvent
import com.example.java_video.ui.state.CreativeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun CreativeScreen(
    state: kotlinx.coroutines.flow.StateFlow<CreativeUiState>,
    onEvent: (CreativeEvent) -> Unit,
    onBack: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val uiState by state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readBytes()
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "voice-sample.wav"
                    val sample = VoiceSample(
                        bytes = bytes,
                        fileName = fileName,
                        label = uiState.voiceLabel.ifBlank { null },
                        mimeType = context.contentResolver.getType(uri) ?: "audio/*"
                    )
                    onEvent(CreativeEvent.OnVoiceSampleCaptured(sample))
                } ?: throw IOException("无法读取音频文件")
            }.onFailure {
                onEvent(CreativeEvent.ClearError)
            }
        }
    }

    LaunchedEffect(uiState.pendingJobId) {
        val jobId = uiState.pendingJobId ?: return@LaunchedEffect
        onSubmit(jobId)
        onEvent(CreativeEvent.ClearSubmission)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "自由创作模式",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.title,
            onValueChange = { onEvent(CreativeEvent.OnTitleChanged(it)) },
            label = { Text("故事标题 (可选)") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.script,
            onValueChange = { onEvent(CreativeEvent.OnScriptChanged(it)) },
            label = { Text("故事脚本") },
            minLines = 6
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.voiceLabel,
            onValueChange = { onEvent(CreativeEvent.OnVoiceLabelChanged(it)) },
            label = { Text("音色备注 (用于区分样本)") }
        )
        uiState.voiceProfile?.let { profile ->
            val display = if (profile.label.isNotBlank()) profile.label else profile.voiceId
            Text("已克隆音色：$display")
        }
        Button(
            onClick = { launcher.launch(arrayOf("audio/*")) },
            enabled = !uiState.isCloningVoice
        ) {
            Text(if (uiState.voiceProfile == null) "导入语音样本" else "重新导入语音样本")
        }

        if (uiState.isCloningVoice) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
        }

        if (!uiState.errorMessage.isNullOrEmpty()) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onEvent(CreativeEvent.Submit) },
            enabled = !uiState.isSubmitting
        ) {
            Text(if (uiState.isSubmitting) "正在提交..." else "提交生成任务")
        }

        TextButton(onClick = onBack) {
            Text("返回")
        }
    }
}
