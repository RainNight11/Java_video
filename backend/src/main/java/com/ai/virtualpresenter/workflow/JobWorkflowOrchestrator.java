package com.ai.virtualpresenter.workflow;

import com.ai.virtualpresenter.dto.JobStatusMapper;
import com.ai.virtualpresenter.model.GenerationJob;
import com.ai.virtualpresenter.model.JobState;
import com.ai.virtualpresenter.store.JobStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Component
public class JobWorkflowOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(JobWorkflowOrchestrator.class);

    private final JobStore jobStore;
    private final SimpMessagingTemplate messagingTemplate;
    private final ThreadPoolTaskExecutor executor;

    public JobWorkflowOrchestrator(JobStore jobStore, SimpMessagingTemplate messagingTemplate) {
        this.jobStore = jobStore;
        this.messagingTemplate = messagingTemplate;
        this.executor = new ThreadPoolTaskExecutor();
        this.executor.setCorePoolSize(2);
        this.executor.setMaxPoolSize(4);
        this.executor.setThreadNamePrefix("job-sim-");
    }

    @PostConstruct
    public void init() {
        executor.initialize();
    }

    public void startSyntheticRun(String jobId) {
        GenerationJob job = jobStore.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown job: " + jobId));
        broadcast(job);
        simulateJob(job);
    }

    protected void simulateJob(GenerationJob job) {
        CompletableFuture.runAsync(() -> {
            List<JobStep> steps = List.of(
                    new JobStep(JobState.PREPROCESSING, 10, Duration.ofSeconds(2)),
                    new JobStep(JobState.CLONING_VOICE, 25, Duration.ofSeconds(2)),
                    new JobStep(JobState.GENERATING_SCRIPT, 45, Duration.ofSeconds(3)),
                    new JobStep(JobState.GENERATING_ASSETS, 70, Duration.ofSeconds(4)),
                    new JobStep(JobState.RENDERING, 95, Duration.ofSeconds(3))
            );
            for (JobStep step : steps) {
                updateJob(job, step);
            }
            job.markCompleted("/media/jobs/" + job.getJobId() + "/render.mp4");
            jobStore.save(job);
            broadcast(job);
        }, executor).exceptionally(error -> {
            log.error("Job simulation failed for {}", job.getJobId(), error);
            job.markFailed("Internal processing error");
            jobStore.save(job);
            broadcast(job);
            return null;
        });
    }

    private void updateJob(GenerationJob job, JobStep step) {
        try {
            TimeUnit.MILLISECONDS.sleep(step.delay().toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        job.update(step.state(), step.progress(), JobStatusMapper.describe(step.state()));
        jobStore.save(job);
        broadcast(job);
    }

    private void broadcast(GenerationJob job) {
        messagingTemplate.convertAndSend("/topic/jobs/" + job.getJobId(), JobStatusMapper.fromJob(job));
    }

    private record JobStep(JobState state, int progress, Duration delay) {
    }
}
