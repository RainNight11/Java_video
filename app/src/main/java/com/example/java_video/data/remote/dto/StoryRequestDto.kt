package com.example.java_video.data.remote.dto

data class StoryRequestDto(
    val script: String,
    val mode: String,
    val voiceId: String,
    val title: String?,
    val avatarImageUrl: String?
)
