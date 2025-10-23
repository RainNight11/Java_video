package com.example.java_video.ui.state

import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.domain.model.JobStatus

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
    val jobs: List<JobStatus> = emptyList()
)
