package com.example.java_video.ui.state

import com.example.java_video.domain.model.VoiceSample

sealed interface BroadcastEvent {
    data class OnPresenterNameChanged(val value: String) : BroadcastEvent
    data class OnScriptChanged(val value: String) : BroadcastEvent
    data class OnAvatarUrlChanged(val value: String) : BroadcastEvent
    data class OnVoiceLabelChanged(val value: String) : BroadcastEvent
    data class OnVoiceSampleCaptured(val sample: VoiceSample) : BroadcastEvent
    data class OnImportFailed(val message: String) : BroadcastEvent
    object Submit : BroadcastEvent
    object ClearSubmission : BroadcastEvent
    object ClearError : BroadcastEvent
}
