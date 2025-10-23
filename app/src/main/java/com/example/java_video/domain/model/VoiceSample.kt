package com.example.java_video.domain.model

data class VoiceSample(
    val bytes: ByteArray,
    val fileName: String,
    val label: String? = null,
    val mimeType: String = "audio/mpeg"
)
