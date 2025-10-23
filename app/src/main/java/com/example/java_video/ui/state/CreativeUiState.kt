package com.example.java_video.ui.state

import com.example.java_video.domain.model.VoiceProfile

data class CreativeUiState(
    val title: String = "",
    val script: String = "",
    val voiceLabel: String = "",
    val voiceProfile: VoiceProfile? = null,
    val isCloningVoice: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val pendingJobId: String? = null
) {
    val hasVoiceProfile: Boolean
        get() = voiceProfile != null
}
