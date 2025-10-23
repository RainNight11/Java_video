package com.example.java_video.ui.state

data class JobStatusUiState(
    val jobId: String,
    val status: String = "RECEIVED",
    val progress: Int = 0,
    val message: String = "排队中...",
    val downloadUrl: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isTerminal: Boolean
        get() = status == "COMPLETED" || status == "FAILED"
}
