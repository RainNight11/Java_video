package com.ai.virtualpresenter.dto;

import com.ai.virtualpresenter.model.GenerationMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StorySubmissionRequest(
        @NotNull(message = "mode is required")
        GenerationMode mode,
        @NotBlank(message = "script must not be blank")
        @Size(max = 5000, message = "script must be shorter than 5000 characters")
        String script,
        @NotBlank(message = "voiceId must not be blank")
        String voiceId,
        @Size(max = 120, message = "title must be shorter than 120 characters")
        String title,
        String avatarImageUrl
) {
}
