package com.vocal.app.bookmark.controller;

import com.vocal.app.bookmark.dto.ChartLikeRequest;
import com.vocal.app.bookmark.dto.ChartLikeResponse;
import com.vocal.app.bookmark.service.BookmarkService;
import com.vocal.app.song.dto.SongResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/songs/{songId}")
    public ResponseEntity<?> toggleSongBookmark(@PathVariable Long songId, Authentication authentication) {
        boolean saved = bookmarkService.toggleSongBookmark(authentication.getName(), songId);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @GetMapping("/songs")
    public ResponseEntity<List<SongResponse>> getSongBookmarks(Authentication authentication) {
        return ResponseEntity.ok(bookmarkService.getSongBookmarks(authentication.getName()));
    }

    @PostMapping("/charts")
    public ResponseEntity<?> toggleChartLike(@Valid @RequestBody ChartLikeRequest request, Authentication authentication) {
        boolean saved = bookmarkService.toggleChartLike(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @GetMapping("/charts")
    public ResponseEntity<List<ChartLikeResponse>> getChartLikes(Authentication authentication) {
        return ResponseEntity.ok(bookmarkService.getChartLikes(authentication.getName()));
    }
}
