package com.ai.virtualpresenter.service;

import com.ai.virtualpresenter.dto.JobStatusResponse;
import com.ai.virtualpresenter.dto.StoryRequest;
import com.ai.virtualpresenter.model.GenerationJob;
import com.ai.virtualpresenter.model.GenerationMode;
import com.ai.virtualpresenter.model.GenerationStatus;
import com.ai.virtualpresenter.model.JobState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class JobOrchestrator {

    private final Map<String, GenerationJob> jobs = new ConcurrentHashMap<>();
    private final Executor jobExecutor;
    private final SimpMessagingTemplate messagingTemplate;

    public JobOrchestrator(@Qualifier("jobExecutor") Executor jobExecutor,
                           SimpMessagingTemplate messagingTemplate) {
        this.jobExecutor = jobExecutor;
        this.messagingTemplate = messagingTemplate;
    }

    public String enqueueStory(StoryRequest request) {
        String jobId = UUID.randomUUID().toString();
        GenerationJob job = new GenerationJob(jobId, request.mode(), request.voiceId(), request.script(), JobState.RECEIVED);
        jobs.put(job.getJobId(), job);

        jobExecutor.execute(() -> simulatePipeline(job.getJobId(), request.mode()));
        publishUpdate(job.getJobId());

        return job.getJobId();
    }

    public void markVoicePreprocessing(String jobId) {
        GenerationJob job = jobs.get(jobId);
        if (job == null) {
            return;
        }
        job.update(JobState.PREPROCESSING, 0, "Voice preprocessing queued");
        publishUpdate(jobId);
    }

    public JobStatusResponse getJobStatus(String jobId) {
        GenerationJob job = jobs.get(jobId);
        if (job == null) {
            return new JobStatusResponse(jobId, GenerationStatus.FAILED.name(), 0, "Job not found", null);
        }
        return toResponse(job);
    }

    private void simulatePipeline(String jobId, GenerationMode mode) {
        GenerationJob job = jobs.get(jobId);
        if (job == null) {
            return;
        }
        try {
            updateJob(job, 10, JobState.GENERATING_ASSETS, "Story segmentation in progress");
            Thread.sleep(1000L);
            updateJob(job, 35, JobState.GENERATING_ASSETS, "Scene layout synthesising");
            Thread.sleep(1200L);
            updateJob(job, 60, JobState.RENDERING,
                    mode == GenerationMode.CREATIVE ? "Animating scenes" : "Animating avatar");
            Thread.sleep(1500L);
            updateJob(job, 90, JobState.RENDERING, "Compositing voice and visuals");
            Thread.sleep(800L);
            job.markCompleted("/media/" + jobId + "/output.mp4");
            publishUpdate(jobId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            job.markFailed("Job interrupted");
            publishUpdate(jobId);
        } catch (Exception ex) {
            job.markFailed(ex.getMessage());
            publishUpdate(jobId);
        }
    }

    private void updateJob(GenerationJob job,
                           int progress,
                           JobState state,
                           String message) {
        job.update(state, progress, message);
        publishUpdate(job.getJobId());
    }

    private void publishUpdate(String jobId) {
        messagingTemplate.convertAndSend("/topic/jobs/" + jobId, getJobStatus(jobId));
    }

    private JobStatusResponse toResponse(GenerationJob job) {
        return new JobStatusResponse(job.getJobId(), job.getState().name(), job.getProgress(), job.getMessage(),
                job.getDownloadUrl().orElse(null));
    }

    public @Nullable GenerationJob getJob(String jobId) {
        return jobs.get(jobId);
    }
}
