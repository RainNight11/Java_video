package com.example.java_video.data.local

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentFile = MutableStateFlow<File?>(null)
    val currentFile: StateFlow<File?> = _currentFile.asStateFlow()
    
    private val _playbackPosition = MutableStateFlow(0)
    val playbackPosition: StateFlow<Int> = _playbackPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()
    
    private var updateRunnable: Runnable? = null
    
    fun play(file: File) {
        try {
            // 如果正在播放其他文件，先停止
            stop()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    _currentFile.value = file
                    _duration.value = duration
                    startPositionUpdates()
                }
                
                setOnCompletionListener {
                    // 播放完成后自动停止并重置状态
                    try {
                        updateRunnable?.let {
                            handler.removeCallbacks(it)
                            updateRunnable = null
                        }
                        
                        _isPlaying.value = false
                        _playbackPosition.value = 0
                        
                        // 释放当前播放器但保持文件引用
                        mediaPlayer?.release()
                        mediaPlayer = null
                        
                        android.util.Log.d("AudioPlayer", "播放完成")
                    } catch (e: Exception) {
                        android.util.Log.e("AudioPlayer", "处理播放完成事件失败", e)
                    }
                }
                
                setOnErrorListener { _, _, _ ->
                    stop()
                    false
                }
                
                prepareAsync()
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayer", "播放失败", e)
            stop()
        }
    }
    
    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _isPlaying.value = false
                    _playbackPosition.value = player.currentPosition
                    
                    // 暂停时停止位置更新
                    updateRunnable?.let {
                        handler.removeCallbacks(it)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayer", "暂停失败", e)
        }
    }
    
    fun resume() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    _isPlaying.value = true
                    startPositionUpdates()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayer", "恢复播放失败", e)
        }
    }
    
    fun stop() {
        try {
            updateRunnable?.let {
                handler.removeCallbacks(it)
                updateRunnable = null
            }
            
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                mediaPlayer = null
            }
            
            _isPlaying.value = false
            _currentFile.value = null
            _playbackPosition.value = 0
            _duration.value = 0
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayer", "停止播放失败", e)
        }
    }
    
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            _playbackPosition.value = position
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayer", "跳转失败", e)
        }
    }
    
    fun isCurrentFile(file: File): Boolean {
        return _currentFile.value?.absolutePath == file.absolutePath
    }
    
    private fun startPositionUpdates() {
        updateRunnable?.let { return } // 避免重复启动
        
        updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playbackPosition.value = player.currentPosition
                        // 每200ms更新一次
                        handler.postDelayed(this, 200)
                    }
                }
            }
        }
        
        updateRunnable?.run()
    }
    
    fun release() {
        stop()
    }
}