package com.example.java_video.data.remote.dto

data class StorySubmissionRequestDto(
    val mode: String,
    val script: String,
    val voiceId: String,
    val title: String?,
    val avatarImageUrl: String?
)
