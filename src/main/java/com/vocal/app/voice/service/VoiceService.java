package com.vocal.app.voice.service;

import com.vocal.app.voice.dto.*;
import com.vocal.app.voice.entity.VocalRange;
import com.vocal.app.voice.repository.VocalRangeRepository;
import com.vocal.app.user.entity.User;
import com.vocal.app.user.entity.VocalHistory;
import com.vocal.app.user.repository.UserRepository;
import com.vocal.app.user.repository.VocalHistoryRepository;
import com.vocal.app.global.util.NoteUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceService {
    private final WebClient aiWebClient;
    private final UserRepository userRepository;
    private final VocalRangeRepository vocalRangeRepository;
    private final VocalHistoryRepository vocalHistoryRepository;

    @Transactional
    public VocalRangeResponse uploadAndAnalyze(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + userId));
        VoiceAnalysisResult result = forwardToAiServer(file);

        VocalRange saved = vocalRangeRepository.save(VocalRange.builder()
                .user(user).minNote(result.getMinNote())
                .maxNote(result.getMaxNote()).stableScore(result.getStableScore()).build());

        vocalHistoryRepository.save(VocalHistory.builder()
                .user(user).minNote(result.getMinNote())
                .maxNote(result.getMaxNote()).stableScore(result.getStableScore()).build());

        return toResponse(userId, saved);
    }

    @Transactional(readOnly = true)
    public VocalRangeResponse getVocalRange(Long userId) {
        return toResponse(userId, vocalRangeRepository
                .findTopByUserUserIdOrderByMeasuredAtDesc(userId)
                .orElseThrow(() -> new IllegalStateException("음역대 측정 이력이 없습니다.")));
    }

    private VoiceAnalysisResult forwardToAiServer(MultipartFile file) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource());
            return aiWebClient.post().uri("/analyze/vocal-range")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve().bodyToMono(VoiceAnalysisResult.class).block();
        } catch (Exception e) {
            log.error("AI 서버 통신 오류", e);
            throw new RuntimeException("AI 분석 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    private VocalRangeResponse toResponse(Long userId, VocalRange r) {
        return VocalRangeResponse.builder()
                .userId(userId).minNote(r.getMinNote()).maxNote(r.getMaxNote())
                .stableScore(r.getStableScore())
                .minNoteLabel(NoteUtil.toLabel(r.getMinNote()))
                .maxNoteLabel(NoteUtil.toLabel(r.getMaxNote()))
                .measuredAt(r.getMeasuredAt()).build();
    }
}
