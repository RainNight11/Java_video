package com.ai.virtualpresenter.dto;

import com.ai.virtualpresenter.model.GenerationMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StoryRequest(
        @NotBlank(message = "script must not be blank")
        @Size(max = 4000, message = "script must be less than 4000 characters")
        String script,

        @NotNull(message = "mode is required")
        GenerationMode mode,

        @NotBlank(message = "voiceId must not be blank")
        String voiceId,

        String title,
        String avatarImageUrl
) {
}
