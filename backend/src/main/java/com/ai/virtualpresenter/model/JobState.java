package com.ai.virtualpresenter.model;

public enum JobState {
    RECEIVED,
    PREPROCESSING,
    CLONING_VOICE,
    GENERATING_SCRIPT,
    GENERATING_ASSETS,
    RENDERING,
    COMPLETED,
    FAILED
}
