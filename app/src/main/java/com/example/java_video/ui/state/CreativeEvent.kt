package com.example.java_video.ui.state

import com.example.java_video.domain.model.VoiceSample

sealed interface CreativeEvent {
    data class OnTitleChanged(val value: String) : CreativeEvent
    data class OnScriptChanged(val value: String) : CreativeEvent
    data class OnVoiceLabelChanged(val value: String) : CreativeEvent
    data class OnVoiceSampleCaptured(val sample: VoiceSample) : CreativeEvent
    data class OnImportFailed(val message: String) : CreativeEvent
    object Submit : CreativeEvent
    object ClearSubmission : CreativeEvent
    object ClearError : CreativeEvent
}
