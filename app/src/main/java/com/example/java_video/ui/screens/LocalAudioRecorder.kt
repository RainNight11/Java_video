package com.example.java_video.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.java_video.data.local.AudioStorageManager
import java.io.File

class LocalAudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private val storageManager = AudioStorageManager(context)

    @SuppressLint("MissingPermission")
    @Throws(IllegalStateException::class)
    fun startRecording(): File? {
        android.util.Log.d("LocalAudioRecorder", "开始录制录音")
        
        if (!storageManager.isStorageAvailable()) {
            android.util.Log.e("LocalAudioRecorder", "存储不可用")
            throw IllegalStateException("存储不可用")
        }
        
        val file = storageManager.createAudioFile()
        android.util.Log.d("LocalAudioRecorder", "音频文件创建: ${file.absolutePath}")
        
        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(file.absolutePath)
                prepare()
                android.util.Log.d("LocalAudioRecorder", "MediaRecorder 准备完成")
                start()
                android.util.Log.d("LocalAudioRecorder", "MediaRecorder 开始录制")
            }
            mediaRecorder = recorder
            outputFile = file
            return file
        } catch (e: Exception) {
            android.util.Log.e("LocalAudioRecorder", "MediaRecorder 初始化失败", e)
            if (file.exists()) file.delete()
            throw e
        }
    }

    fun stopRecording(): File? {
        val recorder = mediaRecorder ?: return null
        android.util.Log.d("LocalAudioRecorder", "停止录制")
        
        return try {
            recorder.stop()
            android.util.Log.d("LocalAudioRecorder", "MediaRecorder 停止成功")
            recorder.release()
            mediaRecorder = null
            val file = outputFile
            outputFile = null
            
            if (file != null && file.exists()) {
                android.util.Log.d("LocalAudioRecorder", "录制完成，文件: ${file.absolutePath}, 大小: ${file.length()} bytes")
            } else {
                android.util.Log.e("LocalAudioRecorder", "录制文件不存在")
            }
            
            file
        } catch (exception: RuntimeException) {
            android.util.Log.e("LocalAudioRecorder", "停止录制失败", exception)
            recorder.reset()
            recorder.release()
            mediaRecorder = null
            outputFile?.delete()
            outputFile = null
            null
        }
    }

    fun cancelRecording() {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (_: Exception) {
            } finally {
                release()
            }
        }
        mediaRecorder = null
        outputFile?.delete()
        outputFile = null
    }
    
    fun isRecording(): Boolean {
        return mediaRecorder != null
    }
    
    fun getStorageManager(): AudioStorageManager = storageManager
}