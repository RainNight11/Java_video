package com.example.java_video.ui.state

import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.domain.model.JobStatus
import com.example.java_video.domain.usecase.TTSGenerationState

data class VirtualPresenterUiState(
    val voiceLabel: String = "",
    val voiceId: String? = null,
    val isUploadingVoice: Boolean = false,
    val uploadError: String? = null,
    val mode: GenerationMode = GenerationMode.CREATIVE,
    val title: String = "",
    val script: String = "",
    val avatarImageUrl: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val jobs: List<JobStatus> = emptyList(),
    
    // TTS相关状态
    val isGeneratingVoice: Boolean = false,
    val voiceGenerationError: String? = null,
    val voiceGenerationState: TTSGenerationState? = null,
    val generatedVoiceUrl: String? = null,
    
    // 语音生成参数
    val selectedEmotion: com.example.java_video.domain.model.Emotion = com.example.java_video.domain.model.Emotion.NEUTRAL,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val useCustomVoice: Boolean = true
)
