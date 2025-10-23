package com.example.java_video.data.repository

import com.example.java_video.domain.model.JobStatus
import com.example.java_video.domain.model.StorySubmission
import com.example.java_video.domain.model.VoiceProfile
import com.example.java_video.domain.model.VoiceSample
import kotlinx.coroutines.flow.Flow

interface GenerationRepository {

    suspend fun cloneVoice(sample: VoiceSample): Result<VoiceProfile>

    suspend fun submitStory(submission: StorySubmission): Result<String>

    fun observeJob(jobId: String): Flow<Result<JobStatus>>
}
