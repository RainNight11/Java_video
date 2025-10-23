package com.example.java_video.data.remote.dto

data class JobStatusDto(
    val jobId: String,
    val status: String,
    val progress: Int,
    val message: String,
    val downloadUrl: String?
)
