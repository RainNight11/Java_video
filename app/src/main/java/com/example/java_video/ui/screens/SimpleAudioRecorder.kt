package com.example.java_video.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class SimpleAudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    @SuppressLint("MissingPermission")
    @Throws(IllegalStateException::class)
    fun startRecording(): File {
        val file = File.createTempFile("vp_record_", ".m4a", context.cacheDir)
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
            start()
        }
        mediaRecorder = recorder
        outputFile = file
        return file
    }

    fun stopRecording(): File? {
        val recorder = mediaRecorder ?: return null
        return try {
            recorder.stop()
            recorder.release()
            mediaRecorder = null
            val file = outputFile
            outputFile = null
            file
        } catch (exception: RuntimeException) {
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
}
