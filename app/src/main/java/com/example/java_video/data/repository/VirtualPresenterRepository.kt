package com.example.java_video.data.repository

import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.domain.model.JobStatus
import com.example.java_video.domain.model.VoiceClone
import com.example.java_video.domain.model.VoiceUploadPayload

interface VirtualPresenterRepository {
    suspend fun uploadVoice(payload: VoiceUploadPayload): VoiceClone
    suspend fun submitStory(
        mode: GenerationMode,
        script: String,
        voiceId: String,
        title: String?,
        avatarImageUrl: String?
    ): String

    suspend fun fetchJob(jobId: String): JobStatus
}
