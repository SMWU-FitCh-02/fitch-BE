package com.vocal.app.bookmark.entity;

import com.vocal.app.song.entity.Song;
import com.vocal.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "song_bookmarks", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "song_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SongBookmark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
