package com.example.java_video.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.java_video.data.repository.GenerationRepository
import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.domain.model.StorySubmission
import com.example.java_video.domain.model.VoiceSample
import com.example.java_video.ui.state.BroadcastEvent
import com.example.java_video.ui.state.BroadcastUiState
import com.example.java_video.ui.state.CreativeEvent
import com.example.java_video.ui.state.CreativeUiState
import com.example.java_video.ui.state.JobStatusUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: GenerationRepository
) : ViewModel() {

    private val _creativeState = MutableStateFlow(CreativeUiState())
    val creativeState: StateFlow<CreativeUiState> = _creativeState.asStateFlow()

    private val _broadcastState = MutableStateFlow(BroadcastUiState())
    val broadcastState: StateFlow<BroadcastUiState> = _broadcastState.asStateFlow()

    private val jobStates = mutableMapOf<String, MutableStateFlow<JobStatusUiState>>()
    private val activeJobCollectors = mutableMapOf<String, Job>()

    fun onCreativeEvent(event: CreativeEvent) {
        when (event) {
            is CreativeEvent.OnTitleChanged -> _creativeState.update { it.copy(title = event.value) }
            is CreativeEvent.OnScriptChanged -> _creativeState.update { it.copy(script = event.value) }
            is CreativeEvent.OnVoiceLabelChanged -> _creativeState.update { it.copy(voiceLabel = event.value) }
            is CreativeEvent.OnVoiceSampleCaptured -> handleVoiceClone(event.sample, isFromCreative = true)
            is CreativeEvent.OnImportFailed -> _creativeState.update { it.copy(errorMessage = event.message) }
            CreativeEvent.Submit -> submitCreative()
            CreativeEvent.ClearSubmission -> _creativeState.update { it.copy(pendingJobId = null) }
            CreativeEvent.ClearError -> _creativeState.update { it.copy(errorMessage = null) }
        }
    }

    fun onBroadcastEvent(event: BroadcastEvent) {
        when (event) {
            is BroadcastEvent.OnPresenterNameChanged ->
                _broadcastState.update { it.copy(presenterName = event.value) }
            is BroadcastEvent.OnScriptChanged ->
                _broadcastState.update { it.copy(script = event.value) }
            is BroadcastEvent.OnAvatarUrlChanged ->
                _broadcastState.update { it.copy(avatarImageUrl = event.value) }
            is BroadcastEvent.OnVoiceLabelChanged ->
                _broadcastState.update { it.copy(voiceLabel = event.value) }
            is BroadcastEvent.OnVoiceSampleCaptured ->
                handleVoiceClone(event.sample, isFromCreative = false)
            is BroadcastEvent.OnImportFailed -> _broadcastState.update { it.copy(errorMessage = event.message) }
            BroadcastEvent.Submit -> submitBroadcast()
            BroadcastEvent.ClearSubmission -> _broadcastState.update { it.copy(pendingJobId = null) }
            BroadcastEvent.ClearError -> _broadcastState.update { it.copy(errorMessage = null) }
        }
    }

    fun getJobState(jobId: String): StateFlow<JobStatusUiState> =
        jobStates.getOrPut(jobId) { MutableStateFlow(JobStatusUiState(jobId)) }.asStateFlow()

    private fun handleVoiceClone(sample: VoiceSample, isFromCreative: Boolean) {
        val voiceLabel = if (isFromCreative) {
            _creativeState.value.voiceLabel
        } else {
            _broadcastState.value.voiceLabel
        }

        setVoiceCloneLoading(isLoading = true, forCreative = isFromCreative)

        viewModelScope.launch {
            val enrichedSample = sample.copy(label = voiceLabel.ifBlank { null })
            repository.cloneVoice(enrichedSample)
                .onSuccess { voice ->
                    _creativeState.update {
                        it.copy(
                            voiceProfile = voice,
                            isCloningVoice = false,
                            errorMessage = null
                        )
                    }
                    _broadcastState.update {
                        it.copy(
                            voiceProfile = voice,
                            isCloningVoice = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "音色克隆失败，请重试。"
                    setVoiceCloneError(message, forCreative = isFromCreative)
                }
        }
    }

    private fun submitCreative() {
        val state = _creativeState.value
        when {
            state.script.isBlank() -> {
                _creativeState.update { it.copy(errorMessage = "请先输入故事脚本") }
            }
            state.voiceProfile == null -> {
                _creativeState.update { it.copy(errorMessage = "请先克隆音色") }
            }
            else -> {
                _creativeState.update { it.copy(isSubmitting = true, errorMessage = null) }
                val submission = StorySubmission(
                    voiceId = state.voiceProfile.voiceId,
                    script = state.script,
                    mode = GenerationMode.CREATIVE,
                    title = state.title.takeIf { it.isNotBlank() }
                )
                viewModelScope.launch {
                    repository.submitStory(submission)
                        .onSuccess { jobId ->
                            _creativeState.update {
                                it.copy(isSubmitting = false, pendingJobId = jobId)
                            }
                            startJobTracking(jobId)
                        }
                        .onFailure { throwable ->
                            _creativeState.update {
                                it.copy(
                                    isSubmitting = false,
                                    errorMessage = throwable.message ?: "提交任务失败"
                                )
                            }
                        }
                }
            }
        }
    }

    private fun submitBroadcast() {
        val state = _broadcastState.value
        when {
            state.script.isBlank() -> {
                _broadcastState.update { it.copy(errorMessage = "请填写播报文本") }
            }
            state.voiceProfile == null -> {
                _broadcastState.update { it.copy(errorMessage = "请先克隆音色") }
            }
            state.avatarImageUrl.isBlank() -> {
                _broadcastState.update { it.copy(errorMessage = "请提供虚拟人照片 URL") }
            }
            else -> {
                _broadcastState.update { it.copy(isSubmitting = true, errorMessage = null) }
                val submission = StorySubmission(
                    voiceId = state.voiceProfile.voiceId,
                    script = state.script,
                    mode = GenerationMode.BROADCAST,
                    title = state.presenterName.takeIf { it.isNotBlank() },
                    avatarImageUrl = state.avatarImageUrl
                )
                viewModelScope.launch {
                    repository.submitStory(submission)
                        .onSuccess { jobId ->
                            _broadcastState.update {
                                it.copy(isSubmitting = false, pendingJobId = jobId)
                            }
                            startJobTracking(jobId)
                        }
                        .onFailure { throwable ->
                            _broadcastState.update {
                                it.copy(
                                    isSubmitting = false,
                                    errorMessage = throwable.message ?: "提交任务失败"
                                )
                            }
                        }
                }
            }
        }
    }

    private fun startJobTracking(jobId: String) {
        if (activeJobCollectors.containsKey(jobId)) return
        val state = jobStates.getOrPut(jobId) { MutableStateFlow(JobStatusUiState(jobId)) }
        val job = viewModelScope.launch {
            repository.observeJob(jobId).collect { result ->
                state.update { current ->
                    result.fold(
                        onSuccess = {
                            current.copy(
                                status = it.status,
                                progress = it.progress,
                                message = it.message,
                                downloadUrl = it.downloadUrl,
                                isLoading = false,
                                errorMessage = null
                            )
                        },
                        onFailure = { throwable ->
                            current.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "查询任务状态失败"
                            )
                        }
                    )
                }
            }
        }
        activeJobCollectors[jobId] = job
    }

    private fun setVoiceCloneLoading(isLoading: Boolean, forCreative: Boolean) {
        if (forCreative) {
            _creativeState.update { it.copy(isCloningVoice = isLoading, errorMessage = null) }
        } else {
            _broadcastState.update { it.copy(isCloningVoice = isLoading, errorMessage = null) }
        }
    }

    private fun setVoiceCloneError(message: String, forCreative: Boolean) {
        if (forCreative) {
            _creativeState.update { it.copy(isCloningVoice = false, errorMessage = message) }
        } else {
            _broadcastState.update { it.copy(isCloningVoice = false, errorMessage = message) }
        }
    }
}
