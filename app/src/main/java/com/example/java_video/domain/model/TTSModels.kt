package com.example.java_video.domain.model

/**
 * TTS (Text-to-Speech) 请求参数
 */
data class TTSRequest(
    val text: String,                    // 要转换的文本内容
    val voiceId: String? = null,         // 用户上传的声音ID（可选）
    val customAudioPath: String? = null,  // 自定义音频文件路径（可选）
    val voicePreset: VoicePreset = VoicePreset.NEUTRAL,  // 声音预设
    val speed: Float = 1.0f,             // 语速 (0.5 - 2.0)
    val pitch: Float = 1.0f,             // 音调 (0.5 - 2.0)
    val emotion: Emotion = Emotion.NEUTRAL, // 情感风格
    val outputFormat: OutputFormat = OutputFormat.MP3, // 输出格式
    val sampleRate: Int = 22050          // 采样率
)

/**
 * TTS 响应结果
 */
data class TTSResponse(
    val taskId: String,                  // 任务ID
    val audioUrl: String?,              // 音频文件URL（完成后）
    val estimatedDuration: Int,         // 预估时长（秒）
    val status: TTSTaskStatus,          // 任务状态
    val message: String? = null         // 状态消息
)

/**
 * 声音预设类型
 */
enum class VoicePreset {
    NEUTRAL,      // 中性
    ENERGETIC,    // 活力
    CALM,         // 平静
    PROFESSIONAL, // 专业
    FRIENDLY      // 友好
}

/**
 * 情感风格
 */
enum class Emotion {
    NEUTRAL,      // 中性
    HAPPY,        // 开心
    EXCITED,      // 兴奋
    SERIOUS,      // 严肃
    GENTLE        // 温柔
}

/**
 * 输出格式
 */
enum class OutputFormat {
    MP3,
    WAV,
    M4A,
    OGG
}

/**
 * TTS任务状态
 */
enum class TTSTaskStatus {
    PENDING,     // 等待处理
    PROCESSING,  // 正在处理
    COMPLETED,   // 处理完成
    FAILED       // 处理失败
}