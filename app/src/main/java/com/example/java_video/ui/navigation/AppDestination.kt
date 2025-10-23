package com.example.java_video.ui.navigation

sealed class AppDestination(val route: String) {
    data object Landing : AppDestination("landing")
    data object Creative : AppDestination("creative")
    data object Broadcast : AppDestination("broadcast")
    data object JobStatus : AppDestination("job-status/{jobId}") {
        const val JOB_ID_ARG = "jobId"
        fun route(jobId: String) = "job-status/$jobId"
    }
}
