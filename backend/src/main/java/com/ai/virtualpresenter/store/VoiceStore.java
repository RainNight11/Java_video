package com.ai.virtualpresenter.store;

import com.ai.virtualpresenter.model.VoiceProfile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VoiceStore {

    private final Map<String, VoiceProfile> voices = new ConcurrentHashMap<>();

    public void save(VoiceProfile profile) {
        voices.put(profile.voiceId(), profile);
    }

    public Optional<VoiceProfile> findById(String voiceId) {
        return Optional.ofNullable(voices.get(voiceId));
    }
}
