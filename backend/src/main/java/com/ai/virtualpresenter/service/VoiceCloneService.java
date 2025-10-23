package com.ai.virtualpresenter.service;

import com.ai.virtualpresenter.model.VoiceProfile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VoiceCloneService {
    VoiceProfile cloneVoice(MultipartFile file, String label) throws IOException;

    VoiceProfile getVoice(String voiceId);
}
