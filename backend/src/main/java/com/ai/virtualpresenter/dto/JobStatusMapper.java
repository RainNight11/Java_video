package com.ai.virtualpresenter.dto;

import com.ai.virtualpresenter.model.GenerationJob;
import com.ai.virtualpresenter.model.JobState;

public final class JobStatusMapper {

    private JobStatusMapper() {
    }

    public static JobStatusResponse fromJob(GenerationJob job) {
        String downloadUrl = job.getDownloadUrl().orElse(null);
        return new JobStatusResponse(
                job.getJobId(),
                job.getState().name(),
                job.getProgress(),
                job.getMessage(),
                downloadUrl
        );
    }

    public static String describe(JobState state) {
        return switch (state) {
            case RECEIVED -> "Job received";
            case PREPROCESSING -> "Preparing assets";
            case CLONING_VOICE -> "Cloning voice timbre";
            case GENERATING_SCRIPT -> "Generating story beats";
            case GENERATING_ASSETS -> "Rendering scenes";
            case RENDERING -> "Compositing final video";
            case COMPLETED -> "Completed";
            case FAILED -> "Failed";
        };
    }
}
