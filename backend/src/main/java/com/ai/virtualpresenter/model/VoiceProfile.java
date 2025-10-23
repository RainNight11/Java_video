package com.ai.virtualpresenter.model;

import java.time.Instant;

public record VoiceProfile(
        String voiceId,
        String label,
        String originalFileName,
        Instant createdAt,
        String embeddingReference
) {
}
