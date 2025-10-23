package com.example.java_video.di

import com.example.java_video.data.remote.NetworkModule
import com.example.java_video.data.repository.GenerationRepository
import com.example.java_video.data.repository.GenerationRepositoryImpl

class AppContainer {
    val repository: GenerationRepository by lazy {
        GenerationRepositoryImpl(NetworkModule.apiService)
    }
}
