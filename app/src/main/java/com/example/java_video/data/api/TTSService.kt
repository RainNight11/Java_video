package com.example.java_video.data.api

import com.example.java_video.domain.model.TTSRequest
import com.example.java_video.domain.model.TTSResponse
import com.example.java_video.domain.model.TTSTaskStatus

/**
 * TTS (Text-to-Speech) API 接口定义
 * 支持基于自定义声音音色生成语音
 */
interface TTSService {
    
    /**
     * 基于自定义声音音色生成语音
     * @param request TTS请求参数
     * @return 生成的语音文件URL或下载链接
     */
    suspend fun generateVoiceWithCustomAudio(request: TTSRequest): Result<TTSResponse>
    
    /**
     * 获取TTS任务状态
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    suspend fun getTTSTaskStatus(taskId: String): Result<TTSTaskStatusInfo>
    
    /**
     * 下载生成的语音文件
     * @param downloadUrl 下载链接
     * @return 文件数据或本地路径
     */
    suspend fun downloadGeneratedVoice(downloadUrl: String): Result<String>
}

/**
 * TTS任务状态信息
 */
data class TTSTaskStatusInfo(
    val taskId: String,
    val status: TTSTaskStatus,
    val progress: Int = 0,
    val estimatedTimeRemaining: Int? = null,
    val errorMessage: String? = null,
    val resultUrl: String? = null
)