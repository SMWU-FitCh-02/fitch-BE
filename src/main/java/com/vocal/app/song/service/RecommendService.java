package com.vocal.app.song.service;

import com.vocal.app.song.dto.*;
import com.vocal.app.song.entity.Song;
import com.vocal.app.song.repository.SongRepository;
import com.vocal.app.voice.repository.VocalRangeRepository;
import com.vocal.app.voice.entity.VocalRange;
import com.vocal.app.global.util.NoteUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Comparator;
import java.util.List;

@Service @RequiredArgsConstructor
public class RecommendService {
    private final VocalRangeRepository vocalRangeRepository;
    private final SongRepository songRepository;

    @Transactional(readOnly = true)
    public RecommendResponse recommend(Long userId, String artist, String genre) {
        VocalRange range = vocalRangeRepository.findTopByUserUserIdOrderByMeasuredAtDesc(userId)
                .orElseThrow(() -> new IllegalStateException("음역대 측정 이력이 없습니다."));
        int userMaxNote = range.getMaxNote();

        List<Song> matched = StringUtils.hasText(artist)
                ? songRepository.findByMaxNoteLessThanEqualAndArtistContaining(userMaxNote, artist)
                : StringUtils.hasText(genre)
                  ? songRepository.findByMaxNoteLessThanEqualAndGenre(userMaxNote, genre)
                  : songRepository.findByMaxNoteLessThanEqual(userMaxNote);

        // 내 최고음과 가까운 곡일수록 "딱 맞는 키"라고 보고 위로 오도록 정렬.
        // (너무 낮아서 심심한 곡보다, 내 음역대 끝에 딱 붙는 곡을 우선 추천)
        List<SongResponse> songs = matched.stream()
                .sorted(Comparator.comparingInt(s -> Math.abs(s.getMaxNote() - userMaxNote)))
                .map(SongService::toResponse)
                .toList();

        return RecommendResponse.builder()
                .userId(userId).userMaxNote(userMaxNote)
                .userMaxNoteLabel(NoteUtil.toLabel(userMaxNote))
                .recommendedSongs(songs).build();
    }
}