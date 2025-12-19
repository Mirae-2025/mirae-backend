package com.yourcode.mirae.speedrun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "speedrun_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeedrunResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "final_score")
    private Integer finalScore;

    @Column(name = "solved_count")
    private Integer solvedCount;

    @Column(name = "wrong_count")
    private Integer wrongCount;

    @Column(name = "final_rank")
    private Integer finalRank;

    @Column(name = "max_streak")
    private Integer maxStreak;

    @Column(name = "solved_problems", length = 1000)
    private String solvedProblems;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
