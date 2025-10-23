package com.ai.virtualpresenter.dto;

public record JobStatusResponse(
        String jobId,
        String status,
        int progress,
        String message,
        String downloadUrl
) {
}
