package com.vocal.app.voice.controller;

import com.vocal.app.voice.dto.VocalRangeResponse;
import com.vocal.app.voice.service.VoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequiredArgsConstructor
public class VoiceController {
    private final VoiceService voiceService;

    @PostMapping("/voice/upload")
    public ResponseEntity<VocalRangeResponse> upload(
            @RequestParam Long userId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(voiceService.uploadAndAnalyze(userId, file));
    }

    @GetMapping("/user/{id}/vocal-range")
    public ResponseEntity<VocalRangeResponse> getVocalRange(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(voiceService.getVocalRange(userId));
    }
}
