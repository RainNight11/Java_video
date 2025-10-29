package com.example.java_video.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.java_video.AppContainer
import com.example.java_video.VirtualPresenterApplication
import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.domain.model.JobStatus
import com.example.java_video.domain.model.VoiceUploadPayload
import com.example.java_video.domain.usecase.GetJobStatusUseCase
import com.example.java_video.domain.usecase.SubmitStoryUseCase
import com.example.java_video.domain.usecase.UploadVoiceUseCase
import com.example.java_video.domain.usecase.GenerateVoiceUseCase
import com.example.java_video.domain.usecase.TTSGenerationState
import com.example.java_video.domain.model.TTSRequest
import com.example.java_video.ui.state.VirtualPresenterUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VirtualPresenterViewModel(
    application: Application,
    private val uploadVoiceUseCase: UploadVoiceUseCase,
    private val submitStoryUseCase: SubmitStoryUseCase,
    private val getJobStatusUseCase: GetJobStatusUseCase,
    private val generateVoiceUseCase: GenerateVoiceUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VirtualPresenterUiState())
    val uiState: StateFlow<VirtualPresenterUiState> = _uiState.asStateFlow()

    fun onVoiceLabelChange(label: String) {
        _uiState.update { it.copy(voiceLabel = label) }
    }

    fun onModeSelected(mode: GenerationMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onScriptChange(script: String) {
        _uiState.update { it.copy(script = script) }
    }

    fun onAvatarUrlChange(url: String) {
        _uiState.update { it.copy(avatarImageUrl = url) }
    }

    fun clearError() {
        _uiState.update { it.copy(submitError = null, uploadError = null) }
    }

    fun uploadVoiceFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingVoice = true, uploadError = null) }
            try {
                val payload = withContext(Dispatchers.IO) {
                    buildVoicePayload(uri)
                }
                val labelledPayload = payload.copy(label = _uiState.value.voiceLabel.takeIf { it.isNotBlank() })
                val voice = uploadVoiceUseCase(labelledPayload)
                _uiState.update {
                    it.copy(
                        isUploadingVoice = false,
                        voiceId = voice.voiceId,
                        uploadError = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingVoice = false,
                        uploadError = "语音上传失败，请稍后重试。"
                    )
                }
            }
        }
    }

    fun uploadVoiceFromFile(file: File, mimeType: String = DEFAULT_AUDIO_MIME) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingVoice = true, uploadError = null) }
            try {
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                val payload = VoiceUploadPayload(
                    bytes = bytes,
                    fileName = file.name,
                    mimeType = mimeType,
                    label = _uiState.value.voiceLabel.takeIf { it.isNotBlank() }
                )
                val voice = uploadVoiceUseCase(payload)
                _uiState.update {
                    it.copy(
                        isUploadingVoice = false,
                        voiceId = voice.voiceId,
                        uploadError = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingVoice = false,
                        uploadError = "语音上传失败，请稍后重试。"
                    )
                }
            } finally {
                withContext(Dispatchers.IO) {
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        }
    }

    fun submitStory() {
        val current = _uiState.value
        val voiceId = current.voiceId ?: return
        if (current.script.isBlank()) {
            _uiState.update { it.copy(submitError = "请输入脚本内容。") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            try {
                val jobId = submitStoryUseCase(
                    mode = current.mode,
                    script = current.script,
                    voiceId = voiceId,
                    title = current.title.ifBlank { null },
                    avatarImageUrl = current.avatarImageUrl.ifBlank { null }
                )
                // Clear script to encourage new entry once submission succeeds.
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        script = ""
                    )
                }
                trackJob(jobId)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = "生成任务提交失败。"
                    )
                }
            }
        }
    }

    private fun trackJob(jobId: String) {
        viewModelScope.launch {
            var continuePolling = true
            while (continuePolling) {
                try {
                    val status = getJobStatusUseCase(jobId)
                    _uiState.update { state ->
                        val updatedJobs = state.jobs.toMutableList().apply {
                            removeAll { it.jobId == status.jobId }
                            add(0, status)
                        }
                        maintainJobLimit(updatedJobs)
                        state.copy(jobs = updatedJobs)
                    }
                    continuePolling = status.status !in terminalStates
                } catch (exception: Exception) {
                    // Stop polling if backend is unreachable to avoid infinite loop.
                    continuePolling = false
                    _uiState.update { state ->
                        state.copy(submitError = "任务状态查询失败。")
                    }
                }
                if (continuePolling) {
                    delay(POLL_INTERVAL_MS)
                }
            }
        }
    }

    private fun buildVoicePayload(uri: Uri): VoiceUploadPayload {
        val resolver = getApplication<Application>().contentResolver
        val mimeType = resolver.getType(uri) ?: DEFAULT_AUDIO_MIME
        val fileName = queryDisplayName(resolver, uri) ?: DEFAULT_AUDIO_NAME
        val bytes = resolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: throw IllegalArgumentException("无法读取音频文件")
        return VoiceUploadPayload(
            bytes = bytes,
            fileName = fileName,
            mimeType = mimeType,
            label = null
        )
    }

    private fun maintainJobLimit(jobs: MutableList<JobStatus>) {
        val maxJobs = 10
        if (jobs.size > maxJobs) {
            val trim = jobs.subList(maxJobs, jobs.size)
            trim.clear()
        }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        } finally {
            cursor?.close()
        }
    }

    // TTS相关方法
    fun generateVoiceFromText(
        text: String,
        customAudioPath: String? = null,
        voiceId: String? = null,
        emotion: com.example.java_video.domain.model.Emotion = com.example.java_video.domain.model.Emotion.NEUTRAL,
        speed: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        if (text.isBlank()) {
            _uiState.update { it.copy(submitError = "请输入要转换的文本内容。") }
            return
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isGeneratingVoice = true,
                    voiceGenerationError = null,
                    voiceGenerationState = TTSGenerationState.Loading
                )
            }

            try {
                val request = TTSRequest(
                    text = text,
                    voiceId = voiceId,
                    customAudioPath = customAudioPath,
                    emotion = emotion,
                    speed = speed,
                    pitch = pitch
                )

                generateVoiceUseCase(request).collect { state ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            voiceGenerationState = state,
                            isGeneratingVoice = state is TTSGenerationState.Loading || state is TTSGenerationState.Processing,
                            voiceGenerationError = if (state is TTSGenerationState.Error) state.message else null,
                            generatedVoiceUrl = if (state is TTSGenerationState.Success) state.audioUrl else currentState.generatedVoiceUrl
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingVoice = false,
                        voiceGenerationError = "语音生成失败: ${exception.message}",
                        voiceGenerationState = TTSGenerationState.Error(exception.message ?: "未知错误")
                    )
                }
            }
        }
    }

    fun clearVoiceGenerationError() {
        _uiState.update { it.copy(voiceGenerationError = null) }
    }

    fun resetVoiceGeneration() {
        _uiState.update { 
            it.copy(
                isGeneratingVoice = false,
                voiceGenerationError = null,
                voiceGenerationState = null,
                generatedVoiceUrl = null
            )
        }
    }

    fun onEmotionSelected(emotion: com.example.java_video.domain.model.Emotion) {
        _uiState.update { it.copy(selectedEmotion = emotion) }
    }

    fun onVoiceSpeedChanged(speed: Float) {
        _uiState.update { it.copy(voiceSpeed = speed.coerceIn(0.5f, 2.0f)) }
    }

    fun onVoicePitchChanged(pitch: Float) {
        _uiState.update { it.copy(voicePitch = pitch.coerceIn(0.5f, 2.0f)) }
    }

    fun onUseCustomVoiceChanged(useCustom: Boolean) {
        _uiState.update { it.copy(useCustomVoice = useCustom) }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 1_500L
        private const val DEFAULT_AUDIO_MIME = "audio/wav"
        private const val DEFAULT_AUDIO_NAME = "voice-sample.wav"

        private val terminalStates = setOf("COMPLETED", "FAILED")

        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as? VirtualPresenterApplication
                        ?: throw IllegalStateException("Application must be VirtualPresenterApplication")
                    val container: AppContainer = app.container
                    @Suppress("UNCHECKED_CAST")
                    return VirtualPresenterViewModel(
                        application = app,
                        uploadVoiceUseCase = container.uploadVoiceUseCase,
                        submitStoryUseCase = container.submitStoryUseCase,
                        getJobStatusUseCase = container.getJobStatusUseCase,
                        generateVoiceUseCase = container.generateVoiceUseCase
                    ) as T
                }
            }
        }
    }
}
