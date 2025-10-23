package com.ai.virtualpresenter.controller;

import com.ai.virtualpresenter.dto.VoiceCloneResponse;
import com.ai.virtualpresenter.model.VoiceProfile;
import com.ai.virtualpresenter.service.VoiceCloneService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/voices")
@Validated
public class VoiceController {

    private final VoiceCloneService voiceCloneService;

    public VoiceController(VoiceCloneService voiceCloneService) {
        this.voiceCloneService = voiceCloneService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceCloneResponse> cloneVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "label", required = false) String label
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        VoiceProfile profile = voiceCloneService.cloneVoice(file, label);
        return ResponseEntity.ok(new VoiceCloneResponse(profile.voiceId(), profile.label()));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIo(IOException exception) {
        return ResponseEntity.internalServerError().body("Audio processing failed");
    }
}
