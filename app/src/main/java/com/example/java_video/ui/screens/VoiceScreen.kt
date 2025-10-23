package com.example.java_video.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.java_video.viewmodel.VirtualPresenterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(viewModel: VirtualPresenterViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.uploadVoiceFromUri(it) }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Voice Profile", style = MaterialTheme.typography.headlineSmall)

        if (uiState.voiceId != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Voice ID:", style = MaterialTheme.typography.titleMedium)
                    Text(uiState.voiceId!!, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        OutlinedTextField(
            value = uiState.voiceLabel,
            onValueChange = { viewModel.onVoiceLabelChange(it) },
            label = { Text("Voice Label (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { filePickerLauncher.launch("audio/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isUploadingVoice
        ) {
            Text("Choose Audio File")
        }

        if (uiState.isUploadingVoice) {
            CircularProgressIndicator()
        }

        uiState.uploadError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
