package com.vocal.app.bookmark.service;

import com.vocal.app.bookmark.dto.ChartLikeRequest;
import com.vocal.app.bookmark.dto.ChartLikeResponse;
import com.vocal.app.bookmark.entity.ChartLike;
import com.vocal.app.bookmark.entity.SongBookmark;
import com.vocal.app.bookmark.repository.ChartLikeRepository;
import com.vocal.app.bookmark.repository.SongBookmarkRepository;
import com.vocal.app.song.dto.SongResponse;
import com.vocal.app.song.entity.Song;
import com.vocal.app.song.repository.SongRepository;
import com.vocal.app.song.service.SongService;
import com.vocal.app.user.entity.User;
import com.vocal.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final SongBookmarkRepository songBookmarkRepository;
    private final ChartLikeRepository chartLikeRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + username));
    }

    @Transactional
    public boolean toggleSongBookmark(String username, Long songId) {
        User user = getUser(username);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 곡: " + songId));

        return songBookmarkRepository.findByUserUserIdAndSongSongId(user.getUserId(), songId)
                .map(existing -> {
                    songBookmarkRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    songBookmarkRepository.save(SongBookmark.builder().user(user).song(song).build());
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getSongBookmarks(String username) {
        User user = getUser(username);
        return songBookmarkRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream().map(b -> SongService.toResponse(b.getSong())).toList();
    }

    @Transactional
    public boolean toggleChartLike(String username, ChartLikeRequest request) {
        User user = getUser(username);
        return chartLikeRepository.findByUserUserIdAndExternalId(user.getUserId(), request.getExternalId())
                .map(existing -> {
                    chartLikeRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    chartLikeRepository.save(ChartLike.builder()
                            .user(user)
                            .externalId(request.getExternalId())
                            .title(request.getTitle())
                            .artist(request.getArtist())
                            .artworkUrl(request.getArtworkUrl())
                            .build());
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public List<ChartLikeResponse> getChartLikes(String username) {
        User user = getUser(username);
        return chartLikeRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream().map(c -> ChartLikeResponse.builder()
                        .externalId(c.getExternalId())
                        .title(c.getTitle())
                        .artist(c.getArtist())
                        .artworkUrl(c.getArtworkUrl())
                        .build()).toList();
    }
}
