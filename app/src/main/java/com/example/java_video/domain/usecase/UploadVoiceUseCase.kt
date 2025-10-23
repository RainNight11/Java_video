package com.example.java_video.domain.usecase

import com.example.java_video.data.repository.VirtualPresenterRepository
import com.example.java_video.domain.model.VoiceClone
import com.example.java_video.domain.model.VoiceUploadPayload

class UploadVoiceUseCase(private val repository: VirtualPresenterRepository) {
    suspend operator fun invoke(payload: VoiceUploadPayload): VoiceClone {
        return repository.uploadVoice(payload)
    }
}
