package com.example.java_video.domain.usecase

import com.example.java_video.data.api.TTSService
import com.example.java_video.data.api.TTSTaskStatusInfo
import com.example.java_video.domain.model.TTSRequest
import com.example.java_video.domain.model.TTSResponse
import com.example.java_video.domain.model.TTSTaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 基于自定义声音生成语音的用例
 */
class GenerateVoiceUseCase(
    private val ttsService: TTSService
) {
    
    /**
     * 生成语音
     * @param request TTS请求参数
     * @return 生成过程的Flow，可以实时更新进度
     */
    suspend operator fun invoke(request: TTSRequest): Flow<TTSGenerationState> = flow {
        emit(TTSGenerationState.Loading)
        
        try {
            // 发起TTS请求
            val result = ttsService.generateVoiceWithCustomAudio(request)
            
            result.fold(
                onSuccess = { response ->
                    if (response.status == TTSTaskStatus.COMPLETED && response.audioUrl != null) {
                        emit(TTSGenerationState.Success(response.audioUrl, response.estimatedDuration))
                    } else if (response.status == TTSTaskStatus.FAILED) {
                        emit(TTSGenerationState.Error(response.message ?: "生成失败"))
                    } else {
                        // 需要轮询状态
                        pollTaskStatus(response.taskId)
                    }
                },
                onFailure = { exception ->
                    emit(TTSGenerationState.Error("生成失败: ${exception.message}"))
                }
            )
        } catch (e: Exception) {
            emit(TTSGenerationState.Error("生成失败: ${e.message}"))
        }
    }
    
    /**
     * 轮询任务状态
     */
    private suspend fun pollTaskStatus(taskId: String): Flow<TTSGenerationState> = flow {
        var attempts = 0
        val maxAttempts = 60 // 最多轮询60次
        
        while (attempts < maxAttempts) {
            val statusResult = ttsService.getTTSTaskStatus(taskId)
            
            statusResult.fold(
                onSuccess = { status ->
                    when (status.status) {
                        TTSTaskStatus.PROCESSING -> {
                            emit(TTSGenerationState.Processing(status.progress, status.estimatedTimeRemaining))
                        }
                        TTSTaskStatus.COMPLETED -> {
                            if (status.resultUrl != null) {
                                emit(TTSGenerationState.Success(status.resultUrl, 0))
                                return@flow
                            } else {
                                emit(TTSGenerationState.Error("生成完成但未获取到文件"))
                                return@flow
                            }
                        }
                        TTSTaskStatus.FAILED -> {
                            emit(TTSGenerationState.Error(status.errorMessage ?: "生成失败"))
                            return@flow
                        }
                        else -> {
                            // 继续等待
                        }
                    }
                },
                onFailure = { exception ->
                    emit(TTSGenerationState.Error("获取状态失败: ${exception.message}"))
                    return@flow
                }
            )
            
            attempts++
            if (attempts < maxAttempts) {
                kotlinx.coroutines.delay(2000) // 每2秒轮询一次
            }
        }
        
        if (attempts >= maxAttempts) {
            emit(TTSGenerationState.Error("生成超时，请重试"))
        }
    }
    
    /**
     * 下载生成的语音文件
     */
    suspend fun downloadGeneratedVoice(url: String): Result<String> {
        return try {
            ttsService.downloadGeneratedVoice(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * TTS生成状态
 */
sealed class TTSGenerationState {
    object Loading : TTSGenerationState()
    data class Processing(val progress: Int, val estimatedTimeRemaining: Int?) : TTSGenerationState()
    data class Success(val audioUrl: String, val duration: Int) : TTSGenerationState()
    data class Error(val message: String) : TTSGenerationState()
}