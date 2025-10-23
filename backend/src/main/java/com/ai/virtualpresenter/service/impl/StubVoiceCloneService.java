package com.ai.virtualpresenter.service.impl;

import com.ai.virtualpresenter.model.VoiceProfile;
import com.ai.virtualpresenter.service.VoiceCloneService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class StubVoiceCloneService implements VoiceCloneService {

    @Override
    public VoiceProfile cloneVoice(MultipartFile voiceSample, String label) {
        String voiceId = UUID.randomUUID().toString();
        return new VoiceProfile(
                voiceId,
                label,
                voiceSample.getOriginalFilename(),
                Instant.now(),
                computeHashSafe(voiceSample)
        );
    }

    @Override
    public VoiceProfile getVoice(String voiceId) {
        return new VoiceProfile(
                voiceId,
                "some label",
                "some_file.wav",
                Instant.now(),
                "some_hash"
        );
    }

    private String computeHashSafe(MultipartFile voiceSample) {
        try {
            return DigestUtils.sha256Hex(voiceSample.getBytes());
        } catch (IOException e) {
            return DigestUtils.sha256Hex(UUID.randomUUID().toString());
        }
    }
}
