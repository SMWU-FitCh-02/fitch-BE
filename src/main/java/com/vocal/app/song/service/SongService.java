package com.vocal.app.song.service;

import com.vocal.app.song.dto.*;
import com.vocal.app.song.entity.Song;
import com.vocal.app.song.repository.SongRepository;
import com.vocal.app.global.util.NoteUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class SongService {
    private final SongRepository songRepository;

    @Transactional
    public SongResponse registerSong(SongRequest request) {
        return toResponse(songRepository.save(Song.builder()
                .title(request.getTitle()).artist(request.getArtist())
                .genre(request.getGenre()).key(request.getKey())
                .minNote(request.getMinNote()).maxNote(request.getMaxNote())
                .artistGender(request.getArtistGender()).build()));
    }

    @Transactional(readOnly = true)
    public SongResponse getVocalRange(Long songId) {
        return toResponse(songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 곡: " + songId)));
    }

    public static SongResponse toResponse(Song song) {
        return SongResponse.builder()
                .songId(song.getSongId()).title(song.getTitle()).artist(song.getArtist())
                .genre(song.getGenre()).key(song.getKey())
                .minNote(song.getMinNote()).maxNote(song.getMaxNote())
                .minNoteLabel(NoteUtil.toLabel(song.getMinNote()))
                .maxNoteLabel(NoteUtil.toLabel(song.getMaxNote()))
                .artistGender(song.getArtistGender()).build();
    }

    @Transactional
    public SongResponse updateSong(Long songId, SongRequest request) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 곡: " + songId));
        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());
        song.setGenre(request.getGenre());
        song.setKey(request.getKey());
        song.setMinNote(request.getMinNote());
        song.setMaxNote(request.getMaxNote());
        song.setArtistGender(request.getArtistGender());
        return toResponse(song);
    }

    @Transactional
    public void deleteSong(Long songId) {
        if (!songRepository.existsById(songId))
            throw new IllegalArgumentException("존재하지 않는 곡: " + songId);
        songRepository.deleteById(songId);
    }

    public List<SongResponse> getAllSongs() {
        return songRepository.findAll().stream()
                .map(SongService::toResponse)
                .toList();
    }

    public SongResponse getSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("곡을 찾을 수 없습니다: " + id));
        return toResponse(song);
    }
}