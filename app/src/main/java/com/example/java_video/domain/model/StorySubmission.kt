package com.example.java_video.domain.model

data class StorySubmission(
    val voiceId: String,
    val script: String,
    val mode: GenerationMode,
    val title: String? = null,
    val avatarImageUrl: String? = null
)
