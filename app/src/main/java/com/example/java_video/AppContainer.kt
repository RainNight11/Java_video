package com.example.java_video

import android.content.Context
import com.example.java_video.data.remote.VirtualPresenterApi
import com.example.java_video.data.repository.DefaultVirtualPresenterRepository
import com.example.java_video.data.repository.VirtualPresenterRepository
import com.example.java_video.domain.usecase.GetJobStatusUseCase
import com.example.java_video.domain.usecase.SubmitStoryUseCase
import com.example.java_video.domain.usecase.UploadVoiceUseCase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        // Emulator to backend running on host machine.
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: VirtualPresenterApi = retrofit.create(VirtualPresenterApi::class.java)

    private val repository: VirtualPresenterRepository = DefaultVirtualPresenterRepository(api)

    val uploadVoiceUseCase = UploadVoiceUseCase(repository)
    val submitStoryUseCase = SubmitStoryUseCase(repository)
    val getJobStatusUseCase = GetJobStatusUseCase(repository)
}
