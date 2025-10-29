package com.example.java_video.data.remote

import com.example.java_video.data.api.TTSService
import com.example.java_video.data.api.TTSTaskStatusInfo
import com.example.java_video.domain.model.TTSRequest
import com.example.java_video.domain.model.TTSResponse

/**
 * TTSService的占位符实现
 * 实际使用时需要根据具体的TTS API进行实现
 */
class PlaceholderTTSService : TTSService {
    
    override suspend fun generateVoiceWithCustomAudio(request: TTSRequest): Result<TTSResponse> {
        // TODO: 实现实际的TTS API调用
        // 这里只是一个占位符实现
        return Result.failure(NotImplementedError("TTS API 尚未实现"))
    }
    
    override suspend fun getTTSTaskStatus(taskId: String): Result<TTSTaskStatusInfo> {
        // TODO: 实现实际的状态查询API调用
        return Result.failure(NotImplementedError("TTS API 尚未实现"))
    }
    
    override suspend fun downloadGeneratedVoice(downloadUrl: String): Result<String> {
        // TODO: 实现实际的文件下载
        return Result.failure(NotImplementedError("TTS API 尚未实现"))
    }
}