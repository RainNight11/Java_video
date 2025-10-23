package com.example.java_video.ui.state

import com.example.java_video.domain.model.VoiceProfile

data class BroadcastUiState(
    val presenterName: String = "",
    val script: String = "",
    val avatarImageUrl: String = "",
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
