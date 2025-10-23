package com.ai.virtualpresenter.controller;

import com.ai.virtualpresenter.dto.JobStatusMapper;
import com.ai.virtualpresenter.dto.JobStatusResponse;
import com.ai.virtualpresenter.model.GenerationJob;
import com.ai.virtualpresenter.service.StoryWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final StoryWorkflowService storyWorkflowService;

    public JobController(StoryWorkflowService storyWorkflowService) {
        this.storyWorkflowService = storyWorkflowService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobStatusResponse> getJob(@PathVariable String jobId) {
        GenerationJob job = storyWorkflowService.getJob(jobId);
        return ResponseEntity.ok(JobStatusMapper.fromJob(job));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
