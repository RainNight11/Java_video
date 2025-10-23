package com.example.java_video.data.repository

import com.example.java_video.data.remote.GenerationApiService
import com.example.java_video.data.remote.dto.StoryRequestDto
import com.example.java_video.domain.model.GenerationMode
import com.example.java_video.domain.model.JobStatus
import com.example.java_video.domain.model.StorySubmission
import com.example.java_video.domain.model.VoiceProfile
import com.example.java_video.domain.model.VoiceSample
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GenerationRepositoryImpl(
    private val apiService: GenerationApiService
) : GenerationRepository {

    override suspend fun cloneVoice(sample: VoiceSample): Result<VoiceProfile> = runCatching {
        val mediaType = sample.mimeType.toMediaTypeOrNull() ?: "audio/mpeg".toMediaType()
        val requestBody = sample.bytes.toRequestBody(mediaType)
        val filePart = MultipartBody.Part.createFormData("file", sample.fileName, requestBody)
        val labelPart: RequestBody? = sample.label?.toRequestBody("text/plain".toMediaType())
        val response = apiService.cloneVoice(filePart, labelPart)
        VoiceProfile(
            voiceId = response.voiceId,
            label = response.label
        )
    }

    override suspend fun submitStory(submission: StorySubmission): Result<String> = runCatching {
        val request = StoryRequestDto(
            script = submission.script,
            mode = submission.mode.name,
            voiceId = submission.voiceId,
            title = submission.title,
            avatarImageUrl = submission.avatarImageUrl
        )
        apiService.submitStory(request).jobId
    }

    override fun observeJob(jobId: String): Flow<Result<JobStatus>> = flow {
        var shouldContinue = true
        while (shouldContinue) {
            val result = runCatching {
                val dto = apiService.getJob(jobId)
                JobStatus(
                    jobId = dto.jobId,
                    status = dto.status,
                    progress = dto.progress,
                    message = dto.message,
                    downloadUrl = dto.downloadUrl
                )
            }.onFailure { throwable ->
                if (throwable is IOException) {
                    // transient network error, keep polling
                } else {
                    shouldContinue = false
                }
            }
            emit(result)
            val isTerminal = result.getOrNull()?.status in setOf("COMPLETED", "FAILED")
            shouldContinue = shouldContinue && !isTerminal
            delay(1500)
        }
    }
}
