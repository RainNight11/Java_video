package com.ai.virtualpresenter.service.impl;

import com.ai.virtualpresenter.dto.JobStatusMapper;
import com.ai.virtualpresenter.dto.StorySubmissionRequest;
import com.ai.virtualpresenter.model.GenerationJob;
import com.ai.virtualpresenter.model.JobState;
import com.ai.virtualpresenter.model.VoiceProfile;
import com.ai.virtualpresenter.service.StoryWorkflowService;
import com.ai.virtualpresenter.service.VoiceCloneService;
import com.ai.virtualpresenter.store.JobStore;
import com.ai.virtualpresenter.workflow.JobWorkflowOrchestrator;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StoryWorkflowServiceImpl implements StoryWorkflowService {

    private final VoiceCloneService voiceCloneService;
    private final JobStore jobStore;
    private final JobWorkflowOrchestrator orchestrator;
    private final SimpMessagingTemplate messagingTemplate;

    public StoryWorkflowServiceImpl(
            VoiceCloneService voiceCloneService,
            JobStore jobStore,
            JobWorkflowOrchestrator orchestrator,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.voiceCloneService = voiceCloneService;
        this.jobStore = jobStore;
        this.orchestrator = orchestrator;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public GenerationJob submitStory(StorySubmissionRequest request) {
        VoiceProfile profile = voiceCloneService.getVoice(request.voiceId());
        String jobId = "job-" + UUID.randomUUID();
        GenerationJob job = new GenerationJob(jobId, request.mode(), profile.voiceId(), request.script(), JobState.RECEIVED);
        job.update(JobState.RECEIVED, 0, JobStatusMapper.describe(JobState.RECEIVED));
        jobStore.save(job);
        messagingTemplate.convertAndSend("/topic/jobs/" + job.getJobId(), JobStatusMapper.fromJob(job));
        orchestrator.startSyntheticRun(jobId);
        return job;
    }

    @Override
    public GenerationJob getJob(String jobId) {
        return jobStore.findById(jobId).orElseThrow(() -> new IllegalArgumentException("Unknown job: " + jobId));
    }
}
