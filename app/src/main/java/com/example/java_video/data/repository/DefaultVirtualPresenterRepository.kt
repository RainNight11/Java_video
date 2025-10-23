package com.example.java_video.data.repository

import com.example.java_video.data.remote.VirtualPresenterApi
import com.example.java_video.data.remote.dto.StorySubmissionRequestDto
import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.domain.model.JobStatus
import com.example.java_video.domain.model.VoiceClone
import com.example.java_video.domain.model.VoiceUploadPayload
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class DefaultVirtualPresenterRepository(
    private val api: VirtualPresenterApi
) : VirtualPresenterRepository {

    override suspend fun uploadVoice(payload: VoiceUploadPayload): VoiceClone {
        val requestBody = payload.bytes.toRequestBody(payload.mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", payload.fileName, requestBody)
        val labelPart: RequestBody? = payload.label?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
        val response = api.cloneVoice(filePart, labelPart)
        return VoiceClone(response.voiceId, response.label)
    }

    override suspend fun submitStory(
        mode: GenerationMode,
        script: String,
        voiceId: String,
        title: String?,
        avatarImageUrl: String?
    ): String {
        val response = api.submitStory(
            StorySubmissionRequestDto(
                mode = mode.name,
                script = script,
                voiceId = voiceId,
                title = title,
                avatarImageUrl = avatarImageUrl
            )
        )
        return response.jobId
    }

    override suspend fun fetchJob(jobId: String): JobStatus {
        val dto = api.getJob(jobId)
        return JobStatus(
            jobId = dto.jobId,
            status = dto.status,
            progress = dto.progress,
            message = dto.message,
            downloadUrl = dto.downloadUrl
        )
    }
}
