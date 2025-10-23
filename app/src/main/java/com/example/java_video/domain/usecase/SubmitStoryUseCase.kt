package com.example.java_video.domain.usecase

import com.example.java_video.data.repository.VirtualPresenterRepository
import com.example.java_video.domain.model.GenerationMode

class SubmitStoryUseCase(private val repository: VirtualPresenterRepository) {
    suspend operator fun invoke(
        mode: GenerationMode,
        script: String,
        voiceId: String,
        title: String?,
        avatarImageUrl: String?
    ): String {
        return repository.submitStory(mode, script, voiceId, title, avatarImageUrl)
    }
}
