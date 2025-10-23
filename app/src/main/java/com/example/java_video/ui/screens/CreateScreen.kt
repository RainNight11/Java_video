package com.example.java_video.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.viewmodel.VirtualPresenterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(viewModel: VirtualPresenterViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create New Story", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.script,
            onValueChange = { viewModel.onScriptChange(it) },
            label = { Text("Story Script") },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            isError = uiState.submitError?.contains("script") == true
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.onTitleChange(it) },
            label = { Text("Title (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Text("Generation Mode", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = uiState.mode == GenerationMode.CREATIVE,
                    onClick = { viewModel.onModeSelected(GenerationMode.CREATIVE) }
                )
                Text("Creative")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = uiState.mode == GenerationMode.BROADCAST,
                    onClick = { viewModel.onModeSelected(GenerationMode.BROADCAST) }
                )
                Text("Broadcast")
            }
        }

        if (uiState.isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = { viewModel.submitStory() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.voiceId != null && uiState.script.isNotBlank()
            ) {
                Text("Generate Video")
            }
        }

        uiState.submitError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        if (uiState.voiceId == null) {
            Text(
                "Please upload a voice sample in the 'Voice' tab before generating a story.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
