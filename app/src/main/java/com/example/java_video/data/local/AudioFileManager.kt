package com.example.java_video.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

data class AudioFile(
    val file: File,
    val name: String,
    val size: Long,
    val date: Date,
    val isLocalRecording: Boolean = true,
    val isUpload: Boolean = false
)

class AudioFileManager(private val context: Context) {
    private val storageManager = AudioStorageManager(context)
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    private val _audioFiles = MutableStateFlow<List<AudioFile>>(emptyList())
    val audioFiles: StateFlow<List<AudioFile>> = _audioFiles.asStateFlow()
    
    init {
        refreshAudioFiles()
    }
    
    fun refreshAudioFiles() {
        val localRecordings = storageManager.getAllRecordings().map { file ->
            AudioFile(
                file = file,
                name = file.nameWithoutExtension,
                size = file.length(),
                date = Date(file.lastModified()),
                isLocalRecording = true,
                isUpload = false
            )
        }
        
        _audioFiles.value = localRecordings.sortedByDescending { it.date }
    }
    
    fun addUploadedFile(uri: Uri): AudioFile? {
        return try {
            // 获取原文件名
            val originalFileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.getString(nameIndex)
            } ?: "未知文件"
            
            // 创建上传文件的目标位置
            val recordingsDir = storageManager.getRecordingsDir()
            
            // 提取文件扩展名
            val originalExtension = if (originalFileName.contains(".")) {
                originalFileName.substringAfterLast(".")
            } else {
                "m4a" // 默认扩展名
            }
            
            // 提取文件名（不含扩展名）
            val baseName = originalFileName.substringBeforeLast(".")
            
            // 检查文件是否已存在，如果存在则添加数字后缀
            var fileName = originalFileName
            var counter = 1
            while (File(recordingsDir, fileName).exists()) {
                fileName = "${baseName}_${counter}.${originalExtension}"
                counter++
            }
            
            val targetFile = File(recordingsDir, fileName)
            
            // 复制文件到应用目录
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            val audioFile = AudioFile(
                file = targetFile,
                name = baseName, // 使用原文件名（不含扩展名）作为显示名称
                size = targetFile.length(),
                date = Date(targetFile.lastModified()),
                isLocalRecording = false,
                isUpload = true
            )
            
            refreshAudioFiles()
            audioFile
        } catch (e: IOException) {
            android.util.Log.e("AudioFileManager", "复制上传文件失败", e)
            null
        }
    }
    
    fun renameAudioFile(audioFile: AudioFile, newName: String): Boolean {
        return try {
            if (newName.isBlank()) return false
            
            // 获取文件扩展名
            val extension = audioFile.file.extension
            val newFileName = if (extension.isNotEmpty()) {
                "${newName}.${extension}"
            } else {
                newName
            }
            
            val newFile = File(audioFile.file.parent, newFileName)
            
            // 重命名文件
            val success = audioFile.file.renameTo(newFile)
            
            if (success) {
                android.util.Log.d("AudioFileManager", "文件重命名成功: ${audioFile.file.name} -> $newFileName")
                refreshAudioFiles()
            } else {
                android.util.Log.e("AudioFileManager", "文件重命名失败: ${audioFile.file.name}")
            }
            
            success
        } catch (e: Exception) {
            android.util.Log.e("AudioFileManager", "重命名音频文件失败", e)
            false
        }
    }
    
    fun deleteAudioFile(audioFile: AudioFile): Boolean {
        return try {
            val success = storageManager.deleteRecording(audioFile.file)
            if (success) {
                refreshAudioFiles()
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("AudioFileManager", "删除音频文件失败", e)
            false
        }
    }
    
    fun createAudioFile(): File {
        return storageManager.createAudioFile()
    }
    
    fun getAllRecordings(): List<File> {
        return storageManager.getAllRecordings()
    }
    
    fun deleteRecording(file: File): Boolean {
        return storageManager.deleteRecording(file)
    }
    
    fun getStorageManager(): AudioStorageManager {
        return storageManager
    }
}