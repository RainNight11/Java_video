package com.example.java_video.data.remote

import com.example.java_video.data.remote.dto.JobStatusDto
import com.example.java_video.data.remote.dto.StorySubmissionRequestDto
import com.example.java_video.data.remote.dto.StorySubmissionResponseDto
import com.example.java_video.data.remote.dto.VoiceCloneResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface VirtualPresenterApi {

    @Multipart
    @POST("api/v1/voices")
    suspend fun cloneVoice(
        @Part file: MultipartBody.Part,
        @Part("label") label: RequestBody?
    ): VoiceCloneResponseDto

    @POST("api/v1/stories")
    suspend fun submitStory(
        @Body request: StorySubmissionRequestDto
    ): StorySubmissionResponseDto

    @GET("api/v1/jobs/{jobId}")
    suspend fun getJob(
        @Path("jobId") jobId: String
    ): JobStatusDto
}
