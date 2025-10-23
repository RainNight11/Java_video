package com.example.java_video.domain.usecase

import com.example.java_video.data.repository.VirtualPresenterRepository
import com.example.java_video.domain.model.JobStatus

class GetJobStatusUseCase(private val repository: VirtualPresenterRepository) {
    suspend operator fun invoke(jobId: String): JobStatus {
        return repository.fetchJob(jobId)
    }
}
