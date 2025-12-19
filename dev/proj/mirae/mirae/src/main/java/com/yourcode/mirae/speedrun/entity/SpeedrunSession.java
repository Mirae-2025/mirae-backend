package com.yourcode.mirae.speedrun.entity;

import com.yourcode.mirae.speedrun.redis.GameMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "speedrun_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeedrunSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameMode mode;

    @Column(nullable = false)
    private Integer duration;

    @Column(length = 200)
    private String tags;

    @Column(name = "difficulty_range", length = 50)
    private String difficultyRange;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_participants")
    private Integer totalParticipants;

    @Column(name = "max_players")
    private Integer maxPlayers;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
