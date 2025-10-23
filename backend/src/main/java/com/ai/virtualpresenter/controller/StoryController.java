package com.ai.virtualpresenter.controller;

import com.ai.virtualpresenter.dto.StorySubmissionRequest;
import com.ai.virtualpresenter.dto.StorySubmissionResponse;
import com.ai.virtualpresenter.model.GenerationJob;
import com.ai.virtualpresenter.service.StoryWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stories")
public class StoryController {

    private final StoryWorkflowService storyWorkflowService;

    public StoryController(StoryWorkflowService storyWorkflowService) {
        this.storyWorkflowService = storyWorkflowService;
    }

    @PostMapping
    public ResponseEntity<StorySubmissionResponse> submitStory(@Valid @RequestBody StorySubmissionRequest request) {
        GenerationJob job = storyWorkflowService.submitStory(request);
        return ResponseEntity.ok(new StorySubmissionResponse(job.getJobId()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
