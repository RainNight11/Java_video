package com.ai.virtualpresenter.model;

import java.time.Instant;
import java.util.Optional;

public class GenerationJob {

    private final String jobId;
    private final GenerationMode mode;
    private final String voiceId;
    private final String script;
    private final Instant createdAt;
    private JobState state;
    private int progress;
    private String message;
    private String downloadUrl;

    public GenerationJob(String jobId, GenerationMode mode, String voiceId, String script, JobState initialState) {
        this.jobId = jobId;
        this.mode = mode;
        this.voiceId = voiceId;
        this.script = script;
        this.state = initialState;
        this.progress = 0;
        this.message = "Job accepted";
        this.createdAt = Instant.now();
    }

    public String getJobId() {
        return jobId;
    }

    public GenerationMode getMode() {
        return mode;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public String getScript() {
        return script;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public JobState getState() {
        return state;
    }

    public int getProgress() {
        return progress;
    }

    public String getMessage() {
        return message;
    }

    public Optional<String> getDownloadUrl() {
        return Optional.ofNullable(downloadUrl);
    }

    public void update(JobState state, int progress, String message) {
        this.state = state;
        this.progress = progress;
        this.message = message;
    }

    public void markCompleted(String downloadUrl) {
        this.state = JobState.COMPLETED;
        this.progress = 100;
        this.message = "Rendering complete";
        this.downloadUrl = downloadUrl;
    }

    public void markFailed(String errorMessage) {
        this.state = JobState.FAILED;
        this.progress = 100;
        this.message = errorMessage;
    }
}
