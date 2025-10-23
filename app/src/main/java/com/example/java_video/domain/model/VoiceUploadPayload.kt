package com.example.java_video.domain.model

data class VoiceUploadPayload(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
    val label: String?
)
