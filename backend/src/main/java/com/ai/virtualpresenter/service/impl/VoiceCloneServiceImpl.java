package com.ai.virtualpresenter.service.impl;

import com.ai.virtualpresenter.model.VoiceProfile;
import com.ai.virtualpresenter.service.VoiceCloneService;
import com.ai.virtualpresenter.store.VoiceStore;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class VoiceCloneServiceImpl implements VoiceCloneService {

    private final VoiceStore voiceStore;

    public VoiceCloneServiceImpl(VoiceStore voiceStore) {
        this.voiceStore = voiceStore;
    }

    @Override
    public VoiceProfile cloneVoice(MultipartFile file, String label) throws IOException {
        byte[] bytes = file.getInputStream().readAllBytes();
        String embeddingReference = DigestUtils.sha256Hex(bytes);
        String voiceId = "voice-" + UUID.randomUUID();

        VoiceProfile profile = new VoiceProfile(
                voiceId,
                label == null || label.isBlank() ? "Untitled Voice" : label,
                file.getOriginalFilename(),
                Instant.now(),
                embeddingReference
        );

        voiceStore.save(profile);
        return profile;
    }

    @Override
    public VoiceProfile getVoice(String voiceId) {
        return voiceStore.findById(voiceId)
                .orElseThrow(() -> new IllegalArgumentException("Voice not found: " + voiceId));
    }
}
