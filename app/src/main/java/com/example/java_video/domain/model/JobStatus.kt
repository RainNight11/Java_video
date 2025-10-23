package com.example.java_video.domain.model

data class JobStatus(
    val jobId: String,
    val status: String,
    val progress: Int,
    val message: String,
    val downloadUrl: String?
)
