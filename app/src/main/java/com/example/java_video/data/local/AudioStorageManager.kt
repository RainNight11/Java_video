package com.example.java_video.data.local

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioStorageManager(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    fun getRecordingsDir(): File {
        // 使用Download目录，用户可以轻松访问
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val recordingsDir = File(downloadDir, "VirtualPresenter录音")
        
        android.util.Log.d("AudioStorage", "录音目录路径: ${recordingsDir.absolutePath}")
        android.util.Log.d("AudioStorage", "Download目录: ${downloadDir.absolutePath}")
        android.util.Log.d("AudioStorage", "Download目录存在: ${downloadDir.exists()}")
        android.util.Log.d("AudioStorage", "Android版本: ${Build.VERSION.SDK_INT}")
        
        if (!downloadDir.exists()) {
            android.util.Log.e("AudioStorage", "Download目录不存在！")
        }
        
        if (!recordingsDir.exists()) {
            val created = recordingsDir.mkdirs()
            android.util.Log.d("AudioStorage", "录音目录创建结果: $created")
            android.util.Log.d("AudioStorage", "录音目录创建后存在: ${recordingsDir.exists()}")
            
            if (!created) {
                android.util.Log.e("AudioStorage", "无法创建录音目录，尝试使用内部存储")
                // 备选方案：使用应用内部存储
                val fallbackDir = File(context.filesDir, "Recordings")
                if (!fallbackDir.exists()) {
                    fallbackDir.mkdirs()
                }
                return fallbackDir
            }
        }
        
        // 检查目录权限
        android.util.Log.d("AudioStorage", "目录可读: ${recordingsDir.canRead()}, 可写: ${recordingsDir.canWrite()}")
        
        return recordingsDir
    }
    
    fun createAudioFile(): File {
        val recordingsDir = getRecordingsDir()
        
        // 验证目录
        if (!recordingsDir.exists()) {
            val created = recordingsDir.mkdirs()
            if (!created) {
                android.util.Log.e("AudioStorage", "无法创建目录: ${recordingsDir.absolutePath}")
                throw IllegalStateException("无法创建存储目录")
            }
        }
        
        // 检查是否为备选的内部存储
        val isInternalStorage = recordingsDir.absolutePath.contains(context.filesDir.absolutePath)
        android.util.Log.d("AudioStorage", "使用内部存储: $isInternalStorage")
        
        android.util.Log.d("AudioStorage", "录音目录: ${recordingsDir.absolutePath}")
        android.util.Log.d("AudioStorage", "目录可读: ${recordingsDir.canRead()}, 可写: ${recordingsDir.canWrite()}")
        
        val timestamp = dateFormat.format(Date())
        val fileName = "Recording_$timestamp.m4a"
        val file = File(recordingsDir, fileName)
        
        android.util.Log.d("AudioStorage", "创建音频文件: ${file.absolutePath}")
        
        // 确保文件不存在
        if (file.exists()) {
            file.delete()
        }
        
        return file
    }
    
    fun getAllRecordings(): List<File> {
        val recordingsDir = getRecordingsDir()
        if (!recordingsDir.exists()) return emptyList()
        
        return recordingsDir.listFiles { file ->
            file.isFile && (file.extension.equals("m4a", true) || 
                          file.extension.equals("wav", true) ||
                          file.extension.equals("mp3", true))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun deleteRecording(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getRecordingInfo(file: File): RecordingInfo {
        return RecordingInfo(
            file = file,
            name = file.nameWithoutExtension,
            size = file.length(),
            date = Date(file.lastModified()),
            duration = 0L // 需要使用媒体播放器获取实际时长
        )
    }
    
    fun isStorageAvailable(): Boolean {
        return try {
            val state = Environment.getExternalStorageState()
            val available = state == Environment.MEDIA_MOUNTED
            android.util.Log.d("AudioStorage", "外部存储状态: $state, 可用: $available")
            
            if (!available) {
                android.util.Log.e("AudioStorage", "外部存储不可用，状态: $state")
                return false
            }
            
            // 检查Download目录
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val downloadExists = downloadDir.exists()
            val downloadCanWrite = downloadDir.canWrite()
            
            android.util.Log.d("AudioStorage", "Download目录存在: $downloadExists, 可写: $downloadCanWrite")
            android.util.Log.d("AudioStorage", "Download目录路径: ${downloadDir.absolutePath}")
            
            // 如果Download目录不可用，至少内部存储应该可用
            if (!downloadExists || !downloadCanWrite) {
                android.util.Log.w("AudioStorage", "Download目录不可用，将使用内部存储")
                return true // 内部存储总是可用的
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AudioStorage", "检查存储可用性失败", e)
            // 即使外部存储检查失败，内部存储应该可用
            return true
        }
    }
}

data class RecordingInfo(
    val file: File,
    val name: String,
    val size: Long,
    val date: Date,
    val duration: Long
)