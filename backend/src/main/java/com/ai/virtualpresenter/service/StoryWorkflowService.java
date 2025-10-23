package com.ai.virtualpresenter.service;

import com.ai.virtualpresenter.dto.StorySubmissionRequest;
import com.ai.virtualpresenter.model.GenerationJob;

public interface StoryWorkflowService {
    GenerationJob submitStory(StorySubmissionRequest request);

    GenerationJob getJob(String jobId);
}
